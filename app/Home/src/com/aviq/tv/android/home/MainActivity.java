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

import java.lang.ref.WeakReference;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.aviq.tv.android.home.player.AndroidPlayer;
import com.aviq.tv.android.home.service.InternetCheckService;
import com.aviq.tv.android.home.state.BaseState;
import com.aviq.tv.android.home.state.StateEnum;
import com.aviq.tv.android.home.state.StateException;
import com.aviq.tv.android.home.state.StateManager;
import com.aviq.tv.android.home.state.overlay.NoInternetState;
import com.aviq.tv.android.home.utils.Param;
import com.aviq.tv.android.home.utils.Params;
import com.aviq.tv.android.home.utils.Prefs;

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
	private InternetCheckHandler _internetCheckHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(TAG, ".onCreate");
		
		setContentView(R.layout.activity_main);
		
		_rootLayout = (ViewGroup) findViewById(R.id.root_layout);
		
		_mainApplication = (MainApplication) getApplication();
		_stateManager = new StateManager(this);
		_internetCheckHandler = new InternetCheckHandler(this);
		
		Prefs prefs = _mainApplication.getPrefs();
		
		Bundle bundle = new Bundle();
		bundle.putString("USER", prefs.getString(Param.User.RAYV_USER));
		bundle.putString("PASS", prefs.getString(Param.User.RAYV_PASS));
		bundle.putString("STREAM_ID", "vtx_sf1");
		bundle.putInt("BITRATE", 1200);
		String url = prefs.getString(Param.System.RAYV_STREAM_URL_PATTERN, bundle);
		
		AndroidPlayer androidPlayer = new AndroidPlayer((VideoView) findViewById(R.id.player));
		androidPlayer.play(url);
		
		initAlarms();
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
			_stateManager.setState(StateEnum.TV, null);
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
				try
				{
					Bundle params = new Bundle();
					params.putInt(Constants.PARAM_MESSAGE_RES_ID, R.string.app_name);
					params.putInt(Constants.PARAM_BACKGROUND_RES_ID, R.drawable.problem);
					_stateManager.setState(StateEnum.OVERLAY, params, true);
				}
				catch (StateException e)
				{
					Log.e(TAG, "Error", e);
				}
			}
		}, 3000);
		
		// Hide the test overlay state
		_rootLayout.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				BaseState overlayState = _stateManager.getCurrentOverlay();
				if (overlayState != null)
					overlayState.hide();
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
	 * Handler method when Internet connectivity is OK.
	 */
	private void processInternetOkEvent()
	{
		BaseState overlayState = _stateManager.getCurrentOverlay();
		
		// If this is the same overlay, hide it.
		if (overlayState != null && overlayState.getClass().equals(NoInternetState.class))
			overlayState.hide();
	}
	
	/**
	 * Handler method when Internet connectivity is NOK.
	 */
	private void processInternetNokEvent()
	{
		BaseState overlayState = _stateManager.getCurrentOverlay();

		if (overlayState != null)
		{
			// If this is the same overlay, do nothing; else hide it.
			if (overlayState.getClass().equals(NoInternetState.class))
				return;
			else
				overlayState.hide();
		}
		
		try 
		{
			_stateManager.setState(StateEnum.NO_INTERNET, null, true);
		}
		catch (StateException e)
		{
			Log.e(TAG, "Error", e);
		}
	}
	
	/**
	 * @return the main application owning this activity
	 */
	public MainApplication getApp()
	{
		return _mainApplication;
	}
	
	/**
	 * @return the state manager handling the states of this activity
	 */
	public StateManager getStateManager()
	{
		return _stateManager;
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
		intent.putExtra(Constants.EXTRA_RESULT_RECEIVER, new Messenger(_internetCheckHandler));
		
		PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), repeatingInterval, pintent);
	}
	
	/**
	 * This class receives and processes messages from InternetCheckService. 
	 */
	private static class InternetCheckHandler extends Handler
	{
		private final WeakReference<MainActivity> _activityRef;
		
		InternetCheckHandler(MainActivity mainActivity)
		{
			_activityRef = new WeakReference<MainActivity>(mainActivity);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			Log.v(TAG, "InternetCheckHandler.handleMessage(): msg = " + msg.what);
			
			MainActivity mainActivity = _activityRef.get();
			if (mainActivity == null)
			{
				Log.w(TAG, "InternetCheckHandler.handleMessage(): loungeActivity is null.");
				return;
			}
			
			switch (msg.what)
			{
				case InternetCheckService.INTERNET_OK:
					mainActivity.processInternetOkEvent();
					break;
			
				case InternetCheckService.INTERNET_NOK:
					mainActivity.processInternetNokEvent();
					break;
					
				default:
					Log.e(TAG, "Ignoring unknown result code from " + InternetCheckService.TAG);
					break;
			}
		}
	}
}
