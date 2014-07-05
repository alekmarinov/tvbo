/**
 * Copyright (c) 2007-2014, AVIQ Bulgaria Ltd
 *
 * Project:     AviqStub
 * Filename:    FeatureStateNetworkError.java
 * Author:      zhelyazko
 * Date:        28 Feb 2014
 * Description: State displaying network error
 */

package com.aviq.tv.android.aviqtv.state.wizard;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.Log;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureName.State;
import com.aviq.tv.android.sdk.core.feature.FeatureState;

/**
 * State displaying network error
 */
public class FeatureStateNetworkError extends FeatureState
{
	private static final String TAG = FeatureStateNetworkError.class.getSimpleName();
	private Button _retryButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_network_error, container, false);
		_retryButton = (Button) viewGroup.findViewById(R.id.retry_button);
		_retryButton.setOnClickListener(_retryButtonOnClickListener);
		final Button settingsButton = (Button) viewGroup.findViewById(R.id.settings_button);
		settingsButton.setOnClickListener(_settingsButtonOnClickListener);
		return viewGroup;
	}

	@Override
    public void onShow(boolean isViewUncovered)
	{
		super.onShow(isViewUncovered);
		_retryButton.requestFocusFromTouch();
		_retryButton.requestFocus();
	}

	@Override
	public State getStateName()
	{
		return FeatureName.State.SPECIAL;
	}

	private OnClickListener _retryButtonOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Log.i(TAG, "Closing to retry internet access");
			close();
		}
	};

	private OnClickListener _settingsButtonOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
			startActivity(intent);
		}
	};
}
