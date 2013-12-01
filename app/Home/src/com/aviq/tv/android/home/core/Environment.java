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
import java.util.List;

import android.app.Activity;
import android.content.res.Resources;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.aviq.tv.android.home.MainActivity;
import com.aviq.tv.android.home.MainApplication;
import com.aviq.tv.android.home.service.ServiceController;
import com.aviq.tv.android.home.state.StateManager;
import com.aviq.tv.android.home.utils.HttpServer;
import com.aviq.tv.android.home.utils.Log;
import com.aviq.tv.android.home.utils.Prefs;

/**
 * Defines application environment
 */
public class Environment
{
	public static final String TAG = Environment.class.getSimpleName();
	private MainActivity _mainActivity;
	private MainApplication _mainApplication;
	private StateManager _stateManager;
	private ServiceController _serviceController;
	private HttpServer _httpServer;
	private Prefs _prefs;
	private RequestQueue _requestQueue;
	private List<IFeature> _features = new ArrayList<IFeature>();

	/**
	 * Environment constructor method
	 */
	public Environment(MainActivity mainActivity)
	{
		_mainActivity = mainActivity;
		_mainApplication = (MainApplication) mainActivity.getApplication();
		_stateManager = new StateManager(mainActivity);
		_serviceController = new ServiceController(_mainApplication);
		_prefs = new Prefs(_mainApplication.getSharedPreferences("user", Activity.MODE_PRIVATE),
		        _mainApplication.getSharedPreferences("system", Activity.MODE_PRIVATE));
		_requestQueue = Volley.newRequestQueue(_mainApplication);
	}

	/**
	 * Initialize environment
	 */
	public void initialize()
	{
		_features = topologicalSort(_features);
		if (_features.size() > 0)
			_features.get(0).initialize(onFeatureInitialized);
	}

	/**
	 * Chain based features initializer
	 */
	private IFeature.OnFeatureInitialized onFeatureInitialized = new IFeature.OnFeatureInitialized()
	{
		private int _nFeature = 0;

		@Override
        public void onInitialized(IFeature feature, int resultCode)
        {
			Log.i(TAG, ".initialize " + _nFeature + ": " + feature.getName() + " results " + resultCode);
			if ((_nFeature + 1) < _features.size())
			{
				_nFeature++;
				_features.get(_nFeature).initialize(this);
			}
        }
	};

	/**
	 * @return main application context
	 */
	public MainApplication getMainApplication()
	{
		return _mainApplication;
	}

	/**
	 * @return application resources
	 */
	public Resources getResources()
	{
		return _mainApplication.getResources();
	}

	/**
	 * @return the only application activity
	 */
	public MainActivity getMainActivity()
	{
		return _mainActivity;
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
	 * @param featureName
	 * @throws FeatureNotFoundException
	 */
	public void use(FeatureName.Component featureName) throws FeatureNotFoundException
	{
		IFeature feature = FeatureFactory.getInstance().createComponent(featureName, this);
		_features.add(feature);
	}

	/**
	 * @param featureName
	 * @throws FeatureNotFoundException
	 */
	public void use(FeatureName.Scheduler featureName) throws FeatureNotFoundException
	{
		IFeature feature = FeatureFactory.getInstance().createScheduler(featureName, this);
		_features.add(feature);
	}

	/**
	 * @param featureName
	 * @throws FeatureNotFoundException
	 */
	public void use(FeatureName.State featureName) throws FeatureNotFoundException
	{
		IFeature feature = FeatureFactory.getInstance().createState(featureName, this);
		_features.add(feature);
	}

	private List<IFeature> topologicalSort(List<IFeature> features)
	{
		List<IFeature> sorted = new ArrayList<IFeature>();
		int featureCount = features.size();
		while (sorted.size() < featureCount)
		{
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
							if (component.equals(((FeatureComponent) sortedFeature).getId()))
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
							if (scheduler.equals(((FeatureScheduler) sortedFeature).getId()))
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
							if (state.equals(((FeatureState) sortedFeature).getId()))
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
		}
		return sorted;
	}
}
