/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    ApplicationAVIQTV.java
 * Author:      alek
 * Date:        4 Dec 2013
 * Description: AVIQ TV main application class
 */

package com.aviq.tv.android.aviqtv;

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.VideoView;

import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.IApplication;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.core.state.StateManager;
import com.aviq.tv.android.sdk.feature.player.rayv.FeaturePlayerRayV;
import com.aviq.tv.android.sdk.utils.TextUtils;

/**
 * AVIQ TV main application class
 */
public class App extends Application implements IApplication
{
	public static final String TAG = App.class.getSimpleName();

	@Override
	public void onActivityCreate(Activity activity)
	{
		activity.setContentView(R.layout.activity_main);
		Log.i(TAG, ".onActivityCreate");

		Environment env = Environment.getInstance();
		try
		{
			// Create StateManager
			StateManager stateManager = new StateManager(activity);
			stateManager.setOverlayBackgroundColor(activity.getResources().getColor(R.color.overlay_background));
			stateManager.setFragmentLayerResources(R.id.main_fragment, R.id.overlay_fragment, R.id.message_fragment);
			env.setStateManager(stateManager);

			// Sets application specific feature factory
			FeatureFactory featureFactory = new FeatureFactory();
			env.setFeatureFactory(featureFactory);

			// Configure RayV player
			FeaturePlayerRayV featurePlayerRayV = (FeaturePlayerRayV)env.use(FeatureName.Component.PLAYER);
			featurePlayerRayV.setVideoView((VideoView)activity.findViewById(R.id.player));
			String streamerIni = TextUtils.inputSteamToString(getResources().openRawResource(R.raw.streamer));
			featurePlayerRayV.setStreamerIni(streamerIni);

			// Use application components
			env.use(FeatureName.State.LOADING);
			env.use(FeatureName.State.EPG);
			env.use(FeatureName.State.CHANNELS);
			env.use(FeatureName.State.TV);
			env.use(FeatureName.State.WATCHLIST);

			// Initialize and start application
			env.initialize(activity);
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
			case KeyEvent.KEYCODE_F2:// Menu
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