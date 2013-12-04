/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    MainActivity.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: The main activity managing all application screens
 */

package com.aviq.tv.android.home;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.ViewGroup;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.FeatureName;
import com.aviq.tv.android.home.core.FeatureNotFoundException;
import com.aviq.tv.android.home.service.InternetCheckService;
import com.aviq.tv.android.home.state.MessageBox;
import com.aviq.tv.android.home.state.StateEnum;
import com.aviq.tv.android.home.state.StateException;
import com.aviq.tv.android.home.state.StateManager;
import com.aviq.tv.android.home.utils.Param;
import com.aviq.tv.android.home.utils.Params;

/**
 * The main activity managing all application screens
 */
public class MainActivity extends Activity
{
	public static final String TAG = MainActivity.class.getSimpleName();

	private MainApplication _mainApplication;
	private StateManager _stateManager;
	private ViewGroup _rootLayout;
	private Handler _handler;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(TAG, ".onCreate");
		setContentView(R.layout.activity_main);

		Environment environment = new Environment(this);
		try
        {
	        environment.use(FeatureName.Component.PLAYER);
			environment.use(FeatureName.Component.EPG);
			environment.use(FeatureName.Component.HTTP_SERVER);
			environment.initialize();
        }
        catch (FeatureNotFoundException e)
        {
        	Log.e(TAG, e.getMessage(), e);
        }

		_rootLayout = (ViewGroup) findViewById(R.id.root_layout);

		_mainApplication = (MainApplication) getApplication();
		_stateManager = new StateManager(this);

/*
		_mainApplication.getServiceController().startService(InternetCheckService.class).then(new ServiceController.OnResultReceived()
		{
			@Override
			public void onReceiveResult(int resultCode, Bundle resultData)
			{
				Log.i(TAG,
				        ".onReceiveResult: resultCode = " + resultCode + ", resultData= "
				                + Strings.implodeBundle(resultData));
			}
		});

		_mainApplication.getServiceController().startService(InternetCheckService.class).every(10, new ServiceController.OnResultReceived()
		{
			@Override
			public void onReceiveResult(int resultCode, Bundle resultData)
			{
				Log.i(TAG,
				        ".onReceiveResult: resultCode = " + resultCode + ", resultData= "
				                + Strings.implodeBundle(resultData));
			}
		});
		*/
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.i(TAG, ".onDestroy");
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.i(TAG, ".onResume");

		// Set TV state as initial state
		try
		{
			_stateManager.setStateMain(StateEnum.TV, null);
		}
		catch (StateException e)
		{
			Log.e(TAG, "Error", e);
		}

		// Add a test overlay state
		_rootLayout.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				_stateManager.showMessage(MessageBox.Type.ERROR, R.string.connection_lost);
			}
		}, 3000);

		// Add a test overlay state
		_rootLayout.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				_stateManager.showMessage(MessageBox.Type.WARN, R.string.contentDescription);
			}
		}, 5000);

		// Hide the test overlay state
		_rootLayout.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				_stateManager.hideMessage();
			}
		}, 10000);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.i(TAG, ".onPause");
	}

	/**
	 * @return the main application owning this activity
	 */
	public MainApplication getApp()
	{
		return _mainApplication;
	}

	public void initAlarms()
	{
		Log.i(TAG, ".initAlarms");

		initInternetCheckAlarm();
	}

	/**
	 * Prepare a repeating alarm that starts InternetCheckService.
	 */
	private void initInternetCheckAlarm()
	{
		Log.i(TAG, ".initInternetCheckAlarm");

		int repeatingInterval = 1000 * Params.getInt(Param.System.INTERNET_CHECK_INTERVAL);

		Calendar cal = Calendar.getInstance();

		Intent intent = new Intent(this, InternetCheckService.class);
		intent.putExtra(Constants.EXTRA_RESULT_RECEIVER, new InternetCheckResultReceiver(_handler));

		PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), repeatingInterval, pintent);
	}

	private class InternetCheckResultReceiver extends ResultReceiver
	{
		public InternetCheckResultReceiver(Handler handler)
		{
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData)
		{
			Log.i(TAG, ".onReceiveResult: resultCode = " + resultCode);
		}
	};
}
