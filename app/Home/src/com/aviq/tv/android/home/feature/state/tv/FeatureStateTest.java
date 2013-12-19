/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureTV.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.home.feature.state.tv;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureState;

/**
 * TV state feature
 */
public class FeatureStateTest extends FeatureState
{
	public static final String TAG = FeatureStateTest.class.getSimpleName();

	private ViewGroup _viewGroup;

	public FeatureStateTest()
	{
		//_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.States.add(FeatureName.State.MESSAGE_BOX);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.TEST;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		_viewGroup = (ViewGroup) inflater.inflate(R.layout.state_program_info, container, false);
		return _viewGroup;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_ENTER:
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				return true;
		}
		return false;
	}
}
