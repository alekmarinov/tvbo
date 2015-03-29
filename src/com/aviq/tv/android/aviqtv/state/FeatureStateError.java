/**
 * Copyright (c) 2007-2014, AVIQ Bulgaria Ltd
 *
 * Project:     Tvbo
 * Filename:    FeatureStateError.java
 * Author:      Elmira
 * Date:        24.09.2014
 * Description: Error window implementation
 */

package com.aviq.tv.android.aviqtv.state;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.AVKeyEvent;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.Log;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureName.State;
import com.aviq.tv.android.sdk.core.feature.FeatureState;

/**
 * Error window implementation
 */
public class FeatureStateError extends FeatureState
{
	public static final String TAG = FeatureStateError.class.getSimpleName();

	public static String ERROR_MESSAGE = "ErrorMessage";

	/*
	 * (non-Javadoc)
	 * @see com.aviq.tv.android.sdk.core.feature.FeatureState#getStateName()
	 */
	@Override
	public State getStateName()
	{
		// TODO Auto-generated method stub
		return FeatureName.State.ERROR;
	}

	@Override
	public void initialize(OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		super.initialize(onFeatureInitialized);
	}

	@Override
	public boolean onKeyDown(AVKeyEvent event)
	{
		switch (event.Code)
		{
			case OK:
			{
				Environment.getInstance().getStateManager().hideStateOverlay();
				return true;
			}
		}
		return super.onKeyDown(event);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		View rootView = inflater.inflate(R.layout.state_error, container, false);
		TextView errorView = (TextView) rootView.findViewById(R.id.error);
		Bundle bundle = this.getArguments();
		if (bundle != null)
		{
			errorView.setText(bundle.getString(ERROR_MESSAGE));
		}
		return rootView;
	}

}
