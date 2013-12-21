/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureStateLoading.java
 * Author:      alek
 * Date:        21 Dec 2013
 * Description: Loading feature state
 */

package com.aviq.tv.android.home.feature.state;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureName.State;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.core.state.StateException;

/**
 * Loading feature state
 */
public class FeatureStateLoading extends FeatureState
{
	private ProgressBar _progressBar;

	public enum Param
	{
		/**
		 * The name of the home feature state
		 */
		HOME_FEATURE_STATE(FeatureName.State.TV.name());

		Param(String value)
		{
			Environment.getInstance().getFeaturePrefs(FeatureName.State.LOADING).put(name(), value);
		}
	}

	public FeatureStateLoading()
	{
		Environment.getInstance().getEventMessenger().register(this, Environment.ON_LOADING);
		Environment.getInstance().getEventMessenger().register(this, Environment.ON_LOADED);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_loading, container, false);
		_progressBar = (ProgressBar) viewGroup.findViewById(R.id.load_progress);
		updateLoadingInfo(null, 0, 0);
		return viewGroup;
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
	}

	@Override
	public State getStateName()
	{
		return FeatureName.State.LOADING;
	}

	private void updateLoadingInfo(String featureName, float totalProgress, float featureProgress)
	{
		int progress = (int) (100 * totalProgress);
		Log.i(TAG, "set progress " + progress);
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
