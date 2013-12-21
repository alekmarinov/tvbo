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
import android.app.Application;
import android.util.Log;
import android.view.KeyEvent;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.application.IApplication;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.core.state.StateException;

/**
 * AVIQ TV main application class
 */
public class ApplicationAVIQTV extends Application implements IApplication
{
	public static final String TAG = ApplicationAVIQTV.class.getSimpleName();

	@Override
	public void onActivityCreate(Activity activity)
	{
		activity.setContentView(R.layout.activity_main);
		Log.i(TAG, ".onActivityCreate");

		try
		{
			Environment.getInstance().use(FeatureName.State.LOADING);
			Environment.getInstance().use(FeatureName.State.EPG);
			Environment.getInstance().use(FeatureName.State.CHANNELS);
			Environment.getInstance().use(FeatureName.State.TV);
			Environment.getInstance().use(FeatureName.State.WATCHLIST);
			Environment.getInstance().initialize(activity);
			Environment.getInstance().getStateManager()
			        .setOverlayBackgroundColor(activity.getResources().getColor(R.color.overlay_background));
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

	@Override
	public void onActivityDestroy()
	{
		Log.i(TAG, ".onActivityDestroy");
	}

	@Override
	public void onActivityResume()
	{
		Log.i(TAG, ".onActivityResume");
	}

	@Override
	public void onActivityPause()
	{
		Log.i(TAG, ".onActivityPause");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.i(TAG, ".onKeyDown: keyCode = " + keyCode);
		boolean handled = Environment.getInstance().getStateManager().onKeyDown(keyCode, event);
		if (handled)
			return true;

		switch (keyCode)
		{
		// Menu
			case KeyEvent.KEYCODE_F2:
			{
				FeatureState menuFeatureState;
				try
				{
					menuFeatureState = Environment.getInstance().getFeatureState(FeatureName.State.MENU);
					Environment.getInstance().getStateManager().setStateOverlay(menuFeatureState, null);
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
				return true;
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		Log.i(TAG, ".onKeyUp: keyCode = " + keyCode);
		return Environment.getInstance().getStateManager().onKeyUp(keyCode, event);
	}
}
