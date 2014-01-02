/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureStateLoading.java
 * Author:      alek
 * Date:        21 Dec 2013
 * Description: Loading feature state
 */

package com.aviq.tv.android.aviqtv.state;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.Prefs;
import com.aviq.tv.android.sdk.core.ResultCode;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureName.State;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.feature.player.FeaturePlayer;

/**
 * Loading feature state
 */
public class FeatureStateLoading extends FeatureState
{
	public static final String TAG = FeatureStateLoading.class.getSimpleName();
	public enum Param
	{
		/**
		 * The name of the home feature state
		 */
		HOME_FEATURE_STATE(FeatureName.State.TV);

		Param(FeatureName.State value)
		{
			Environment.getInstance().getFeaturePrefs(FeatureName.State.LOADING).put(name(), value.name());
		}
	}

	private ProgressBar _progressBar;
	private ViewGroup _rootView;
	private Prefs _userPrefs;

	public FeatureStateLoading()
	{
		_dependencies.Components.add(FeatureName.Component.PLAYER);

		Environment.getInstance().getEventMessenger().register(this, Environment.ON_LOADING);
		Environment.getInstance().getEventMessenger().register(this, Environment.ON_LOADED);
	}

	@Override
    public void initialize(OnFeatureInitialized onInitialized)
	{
        try
        {
        	FeaturePlayer featurePlayer = (FeaturePlayer)Environment.getInstance().getFeatureComponent(FeatureName.Component.PLAYER);
			subscribe(featurePlayer, FeaturePlayer.ON_PLAY_STARTED);
			subscribe(featurePlayer, FeaturePlayer.ON_PLAY_TIMEOUT);

			_userPrefs = Environment.getInstance().getUserPrefs();
			if (_userPrefs.has(FeaturePlayer.Param.LAST_URL))
			{
				featurePlayer.play(_userPrefs.getString(FeaturePlayer.Param.LAST_URL));
			}

			super.initialize(onInitialized);
        }
        catch (FeatureNotFoundException e)
        {
        	onInitialized.onInitialized(this, ResultCode.GENERAL_FAILURE);
        }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		_rootView = (ViewGroup) inflater.inflate(R.layout.state_loading, container, false);
		_progressBar = (ProgressBar) _rootView.findViewById(R.id.load_progress);
		updateLoadingInfo(null, 0, 0);
		return _rootView;
	}

	@Override
	public void onEvent(int msgId, Bundle bundle)
	{
		Log.i(TAG, ".onEvent: msgId = " + msgId);
		if (msgId == Environment.ON_LOADING)
		{
			float totalProgress = bundle.getFloat("totalProgress");
			float featureProgress = bundle.getFloat("featureProgress");
			String featureName = bundle.getString("featureName");
			updateLoadingInfo(featureName, totalProgress, featureProgress);
		}
		else if (msgId == Environment.ON_LOADED)
		{
			Environment.getInstance().getEventMessenger().unregister(this, Environment.ON_LOADING);
			Environment.getInstance().getEventMessenger().unregister(this, Environment.ON_LOADED);
			showHomeState();
		}
		else if (msgId == FeaturePlayer.ON_PLAY_STARTED || msgId == FeaturePlayer.ON_PLAY_TIMEOUT)
		{
			// fade background out
			_rootView.findViewById(R.id.loading_background).animate().setDuration(2000).alpha(0.0f).start();
		}
	}

	@Override
	public State getStateName()
	{
		return FeatureName.State.LOADING;
	}

	private void updateLoadingInfo(String featureName, float totalProgress, float featureProgress)
	{
		int progress = (int) (100 * totalProgress);
		Log.d(TAG, featureName + " load progress " + (int) (100 * featureProgress) + "%, total " + progress + "%");
		_progressBar.setProgress(progress);
	}

	private void showHomeState()
	{
		String homeStateName = getPrefs().getString(Param.HOME_FEATURE_STATE);
		FeatureName.State homeFeatureState = FeatureName.State.valueOf(homeStateName);
		Log.i(TAG, "Setting home feature state " + homeFeatureState);
		try
		{
			FeatureState featureState = Environment.getInstance().getFeatureState(homeFeatureState);
			Environment.getInstance().getStateManager().setStateMain(featureState, null);
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
