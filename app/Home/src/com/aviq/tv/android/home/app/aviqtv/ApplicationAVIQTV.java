/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    ApplicationAVIQTV.java
 * Author:      alek
 * Date:        4 Dec 2013
 * Description: AVIQ TV main application class
 */

package com.aviq.tv.android.home.app.aviqtv;

import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.FeatureName;
import com.aviq.tv.android.home.core.FeatureNotFoundException;
import com.aviq.tv.android.home.core.IApplication;
import com.aviq.tv.android.home.state.StateException;

/**
 * AVIQ TV main application class
 */
public class ApplicationAVIQTV implements IApplication
{
	public static final String TAG = ApplicationAVIQTV.class.getSimpleName();
	private Environment _environment;

	@Override
	public void onCreate(Activity activity)
	{
		activity.setContentView(R.layout.activity_main);
		Log.i(TAG, ".onCreate");

		_environment = new Environment(activity);
		try
		{
			_environment.use(FeatureName.Component.PLAYER);
			_environment.use(FeatureName.Component.EPG);
			_environment.use(FeatureName.Component.HTTP_SERVER);
			_environment.use(FeatureName.Component.REGISTER);
			_environment.use(FeatureName.Scheduler.INTERNET);
			_environment.use(FeatureName.State.TV);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		_environment.initialize();
	}

	@Override
	public void onDestroy()
	{
		Log.i(TAG, ".onDestroy");
	}

	@Override
	public void onResume()
	{
		Log.i(TAG, ".onResume");

		//
		// // Set TV state as initial state
		// try
		// {
		// _environment.getStateManager().setStateMain(StateEnum.TV, null);
		// }
		// catch (StateException e)
		// {
		// Log.e(TAG, "Error", e);
		// }
		//
		// // Add a test overlay state
		// _environment.getHandler().postDelayed(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// _environment.getStateManager().showMessage(MessageBox.Type.ERROR,
		// R.string.connection_lost);
		// }
		// }, 3000);
		//
		// // Add a test overlay state
		// _environment.getHandler().postDelayed(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// _environment.getStateManager().showMessage(MessageBox.Type.WARN,
		// R.string.contentDescription);
		// }
		// }, 5000);
		//
		// // Hide the test overlay state
		// _environment.getHandler().postDelayed(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// _environment.getStateManager().hideMessage();
		// }
		// }, 10000);
	}

	@Override
	public void onPause()
	{
		Log.i(TAG, ".onPause");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		try
		{
			return _environment.getStateManager().onKeyDown(keyCode, event);
		}
		catch (StateException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		try
		{
			return _environment.getStateManager().onKeyUp(keyCode, event);
		}
		catch (StateException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return false;
	}
}
