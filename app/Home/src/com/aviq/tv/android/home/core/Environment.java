/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    Environment.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Defines application environment
 */

package com.aviq.tv.android.home.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.aviq.tv.android.home.core.feature.FeatureComponent;
import com.aviq.tv.android.home.core.feature.FeatureFactory;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureScheduler;
import com.aviq.tv.android.home.core.feature.FeatureSet;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.core.feature.IFeature;
import com.aviq.tv.android.home.core.service.ServiceController;
import com.aviq.tv.android.home.core.state.StateException;
import com.aviq.tv.android.home.core.state.StateManager;
import com.aviq.tv.android.home.utils.BitmapLruCache;
import com.aviq.tv.android.home.utils.HttpServer;
import com.aviq.tv.android.home.utils.Log;
import com.aviq.tv.android.home.utils.Prefs;

/**
 * Defines application environment
 */
public class Environment
{
	public static final String TAG = Environment.class.getSimpleName();

	public enum Param
	{
		/**
		 * Timeout in seconds for feature initialization
		 */
		FEATURE_INITIALIZE_TIMEOUT(55);

		Param(int value)
		{
			Environment.getInstance().getPrefs().put(name(), value);
		}

		Param(String value)
		{
			Environment.getInstance().getPrefs().put(name(), value);
		}
	}

	private static Environment _instance;
	private Activity _activity;
	private Application _context;
	private StateManager _stateManager;
	private ServiceController _serviceController;
	private HttpServer _httpServer;
	private Prefs _prefs;
	private Prefs _userPrefs;
	private RequestQueue _requestQueue;
	private ImageLoader _imageLoader;
	private List<IFeature> _features = new ArrayList<IFeature>();
	private Handler _handler = new Handler();
	private FeatureName.State _homeFeatureState;
	private Map<FeatureName.Component, Prefs> _componentPrefs = new HashMap<FeatureName.Component, Prefs>();
	private Map<FeatureName.Scheduler, Prefs> _schedulerPrefs = new HashMap<FeatureName.Scheduler, Prefs>();
	private Map<FeatureName.State, Prefs> _statePrefs = new HashMap<FeatureName.State, Prefs>();

	/**
	 * Environment constructor method
	 */
	private Environment()
	{
	}

	public static synchronized Environment getInstance()
	{
		if (_instance == null)
			_instance = new Environment();
		return _instance;
	}

	/**
	 * Initialize environment
	 */
	public void initialize(Activity activity)
	{
		// initializes environment context
		_activity = activity;
		_context = activity.getApplication();
		_userPrefs = createUserPrefs();
		_prefs = createPrefs("system");
		_serviceController = new ServiceController(_context);
		_requestQueue = Volley.newRequestQueue(_context);
		_stateManager = new StateManager(activity);

		// Use 1/8th of the available memory for this memory cache.
		int memClass = ((ActivityManager) activity.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;
        _imageLoader = new ImageLoader(_requestQueue, new BitmapLruCache(cacheSize));

		// initializes features
		Log.i(TAG, "Sorting features tolologically based on their declared dependencies");
		_features = topologicalSort(_features);
		for (int i = 0; i < _features.size(); i++)
		{
			Log.i(TAG, i + ". " + _features.get(i).getName());
		}

		Log.i(TAG, "Initializing features");
		onFeatureInitialized.setTimeout(getPrefs().getInt(Param.FEATURE_INITIALIZE_TIMEOUT));
		onFeatureInitialized.initializeNext();
	}

	private class FeatureInitializeTimeout implements Runnable, IFeature.OnFeatureInitialized
	{
		private int _nFeature = -1;
		private long _initStartedTime;
		private int _timeout = 0;

		public void setTimeout(int timeout)
		{
			_timeout = timeout;
		}

		// return true if there are more features to initialize or false
		// otherwise
		public void initializeNext()
		{
			_handler.removeCallbacks(this);
			if ((_nFeature + 1) < _features.size())
			{
				_nFeature++;
				_handler.postDelayed(this, _timeout * 1000);
				_initStartedTime = System.currentTimeMillis();
				IFeature feature = _features.get(_nFeature);
				Log.i(TAG, "Initializing " + feature.getName());
				feature.initialize(this);
			}
			else
			{
				// all features initialized, display home state feature
				if (_homeFeatureState == null)
				{
					Log.e(TAG, "No home state feature defined! First used state feature will be used as home. "
					        + "Check your environment for missing use declarations");
				}
				else
				{
					Log.i(TAG, "Setting main feature state " + _homeFeatureState);
					try
					{
						FeatureState featureState = getFeatureState(_homeFeatureState);
						_stateManager.setStateMain(featureState, null);
					}
					catch (FeatureNotFoundException e)
					{
						Log.e(TAG, e.getMessage(), e);
					}
					catch (StateException e)
					{
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
		}

		@Override
		public void run()
		{
			// Initialization timed out
			Log.e(TAG, _nFeature + ". initialize " + (System.currentTimeMillis() - _initStartedTime) + " ms: "
			        + _features.get(_nFeature).getName() + " timeout!");
			throw new RuntimeException("timeout!");
		}

		@Override
		public void onInitialized(IFeature feature, int resultCode)
		{
			Log.i(TAG, _nFeature + ". initialize " + (System.currentTimeMillis() - _initStartedTime) + " ms: "
			        + feature.getName() + " results " + resultCode);
			initializeNext();
		}
	}

	/**
	 * Chain based features initializer
	 */
	private FeatureInitializeTimeout onFeatureInitialized = new FeatureInitializeTimeout();

	/**
	 * @return main application context
	 */
	public Context getContext()
	{
		return _context;
	}

	/**
	 * @return application resources
	 */
	public Resources getResources()
	{
		return _context.getResources();
	}

	/**
	 * @return the only application activity
	 */
	public Activity getActivity()
	{
		return _activity;
	}

	/**
	 * Returns global initialized HttpServer instance
	 *
	 * @return HttpServer
	 */
	public HttpServer getHttpServer()
	{
		return _httpServer;
	}

	/**
	 * Returns global preferences manager
	 *
	 * @return Prefs
	 */
	public Prefs getPrefs()
	{
		return _prefs;
	}

	/**
	 * Returns global services controller
	 *
	 * @return ServiceController
	 */
	public ServiceController getServiceController()
	{
		return _serviceController;
	}

	/**
	 * Returns global state manager
	 *
	 * @return StateManager
	 */
	public StateManager getStateManager()
	{
		return _stateManager;
	}

	/**
	 * Returns global Volley requests queue
	 *
	 * @return RequestQueue
	 */
	public RequestQueue getRequestQueue()
	{
		return _requestQueue;
	}

	/**
	 * Returns global Volley image loader
	 *
	 * @return ImageLoader
	 */
	public ImageLoader getImageLoader()
	{
		return _imageLoader;
	}

	/**
	 * Returns handler
	 *
	 * @return Handler
	 */
	public Handler getHandler()
	{
		return _handler;
	}

	/**
	 * Declare component feature to be used
	 *
	 * @param featureName
	 * @throws FeatureNotFoundException
	 */
	public void use(FeatureName.Component featureName) throws FeatureNotFoundException
	{
		Log.i(TAG, ".use: Component " + featureName);
		try
		{
			// Check if feature is already used
			getFeatureComponent(featureName);
		}
		catch (FeatureNotFoundException e)
		{
			IFeature feature = FeatureFactory.getInstance().createComponent(featureName);
			useDependencies(feature);
			_features.add(feature);
		}
	}

	/**
	 * Declare scheduler feature to be used
	 *
	 * @param featureName
	 * @throws FeatureNotFoundException
	 */
	public void use(FeatureName.Scheduler featureName) throws FeatureNotFoundException
	{
		Log.i(TAG, ".use: Scheduler " + featureName);

		try
		{
			// Check if feature is already used
			getFeatureScheduler(featureName);
		}
		catch (FeatureNotFoundException e)
		{
			IFeature feature = FeatureFactory.getInstance().createScheduler(featureName);
			useDependencies(feature);
			_features.add(feature);
		}
	}

	/**
	 * Declare state feature to be used. The first used state feature will be
	 * used as home.
	 *
	 * @param featureName
	 * @throws FeatureNotFoundException
	 */
	public void use(FeatureName.State featureName) throws FeatureNotFoundException
	{
		Log.i(TAG, ".use: State " + featureName);

		try
		{
			// Check if feature is already used
			getFeatureState(featureName);
		}
		catch (FeatureNotFoundException e)
		{
			// Use feature
			IFeature feature = FeatureFactory.getInstance().createState(featureName);
			useDependencies(feature);
			_features.add(feature);
		}

		// Sets last used feature state as home state
		_homeFeatureState = featureName;
	}

	/**
	 * @param featureName
	 * @return FeatureComponent
	 * @throws FeatureNotFoundException
	 */
	public FeatureComponent getFeatureComponent(FeatureName.Component featureName) throws FeatureNotFoundException
	{
		for (IFeature feature : _features)
		{
			if (IFeature.Type.COMPONENT.equals(feature.getType()))
			{
				FeatureComponent component = (FeatureComponent) feature;
				if (featureName.equals(component.getComponentName()))
					return component;
			}
		}
		throw new FeatureNotFoundException(featureName);
	}

	/**
	 * @param featureName
	 * @return FeatureScheduler
	 * @throws FeatureNotFoundException
	 */
	public FeatureScheduler getFeatureScheduler(FeatureName.Scheduler featureName) throws FeatureNotFoundException
	{
		for (IFeature feature : _features)
		{
			if (IFeature.Type.SCHEDULER.equals(feature.getType()))
			{
				FeatureScheduler scheduler = (FeatureScheduler) feature;
				if (featureName.equals(scheduler.getSchedulerName()))
					return scheduler;
			}
		}
		throw new FeatureNotFoundException(featureName);
	}

	/**
	 * @param featureName
	 * @return FeatureState
	 * @throws FeatureNotFoundException
	 */
	public FeatureState getFeatureState(FeatureName.State featureName) throws FeatureNotFoundException
	{
		for (IFeature feature : _features)
		{
			if (IFeature.Type.STATE.equals(feature.getType()))
			{
				FeatureState state = (FeatureState) feature;
				if (featureName.equals(state.getStateName()))
					return state;
			}
		}
		throw new FeatureNotFoundException(featureName);
	}

	public Prefs getFeaturePrefs(FeatureName.Component featureName)
	{
		Prefs prefsFile = _componentPrefs.get(featureName);
		if (prefsFile == null)
		{
			prefsFile = createPrefs(featureName.name());
			_componentPrefs.put(featureName, prefsFile);
		}
		return prefsFile;
	}

	public Prefs getFeaturePrefs(FeatureName.Scheduler featureName)
	{
		Prefs prefsFile = _schedulerPrefs.get(featureName);
		if (prefsFile == null)
		{
			prefsFile = createPrefs(featureName.name());
			_schedulerPrefs.put(featureName, prefsFile);
		}
		return prefsFile;
	}

	public Prefs getFeaturePrefs(FeatureName.State featureName)
	{
		Prefs prefsFile = _statePrefs.get(featureName);
		if (prefsFile == null)
		{
			prefsFile = createPrefs(featureName.name());
			_statePrefs.put(featureName, prefsFile);
		}
		return prefsFile;
	}

	public Prefs getUserPrefs()
	{
		return _userPrefs;
	}

	public void setHomeState(FeatureName.State featureName)
	{
		_homeFeatureState = featureName;
	}

	private void useDependencies(IFeature feature) throws FeatureNotFoundException
	{
		FeatureSet deps = feature.dependencies();
		for (FeatureName.Component featureName : deps.Components)
		{
			use(featureName);
		}
		for (FeatureName.Scheduler featureName : deps.Schedulers)
		{
			use(featureName);
		}
		for (FeatureName.State featureName : deps.States)
		{
			use(featureName);
		}
	}

	private List<IFeature> topologicalSort(List<IFeature> features)
	{
		List<IFeature> sorted = new ArrayList<IFeature>();
		int featureCount = features.size();

		Log.v(TAG, ".topologicalSort: " + featureCount + " features");
		while (sorted.size() < featureCount)
		{
			Log.v(TAG, "Sorted " + sorted.size() + " features out of " + featureCount);

			int prevSortedSize = sorted.size();

			// remove all independent features
			for (IFeature feature : features)
			{
				int resolvedCounter;

				// check component dependencies
				resolvedCounter = feature.dependencies().Components.size();
				for (FeatureName.Component component : feature.dependencies().Components)
				{
					for (IFeature sortedFeature : sorted)
					{
						if (IFeature.Type.COMPONENT.equals(sortedFeature.getType()))
							if (component.equals(((FeatureComponent) sortedFeature).getComponentName()))
							{
								resolvedCounter--;
								break;
							}
					}
				}
				if (resolvedCounter > 0) // has unresolved dependencies
					continue;

				// check scheduler dependencies
				resolvedCounter = feature.dependencies().Schedulers.size();
				for (FeatureName.Scheduler scheduler : feature.dependencies().Schedulers)
				{
					for (IFeature sortedFeature : sorted)
					{
						if (IFeature.Type.SCHEDULER.equals(sortedFeature.getType()))
							if (scheduler.equals(((FeatureScheduler) sortedFeature).getSchedulerName()))
							{
								resolvedCounter--;
								break;
							}
					}
				}
				if (resolvedCounter > 0) // has unresolved dependencies
					continue;

				// check state dependencies
				resolvedCounter = feature.dependencies().States.size();
				for (FeatureName.State state : feature.dependencies().States)
				{
					for (IFeature sortedFeature : sorted)
					{
						if (IFeature.Type.STATE.equals(sortedFeature.getType()))
							if (state.equals(((FeatureState) sortedFeature).getStateName()))
							{
								resolvedCounter--;
								break;
							}
					}
				}
				if (resolvedCounter > 0) // has unresolved dependencies
					continue;

				if (sorted.indexOf(feature) < 0)
					sorted.add(feature);
			}
			if (prevSortedSize == sorted.size())
				throw new RuntimeException("Internal error. Unable to sort features!");
		}
		return sorted;
	}

	private Prefs createUserPrefs()
	{
		Log.i(TAG, ".createUserPrefs");
		return new Prefs(_context.getSharedPreferences("user", Activity.MODE_PRIVATE), true);
	}

	private Prefs createPrefs(String name)
	{
		Log.i(TAG, ".createPrefs: name = " + name);
		return new Prefs(_context.getSharedPreferences(name, Activity.MODE_PRIVATE), false);
	}
}
