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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.core.state.StateException;
import com.aviq.tv.android.home.feature.epg.FeatureEPG;
import com.aviq.tv.android.home.feature.state.MessageBox;

/**
 * TV state feature
 */
public class FeatureTV extends FeatureState
{
	public static final String TAG = FeatureTV.class.getSimpleName();
	private ZapperListView _zapperListView;

	public FeatureTV()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.Components.add(FeatureName.Component.PLAYER);
		_dependencies.States.add(FeatureName.State.MESSAGE_BOX);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		super.initialize(onFeatureInitialized);
		onFeatureInitialized.onInitialized(this, ResultCode.OK);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.TV;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_tv, container, false);
		_zapperListView = (ZapperListView) viewGroup.findViewById(R.id.zapperListView);
		try
		{
			FeatureEPG featureEPG = (FeatureEPG) Environment.getInstance().getFeatureComponent(
			        FeatureName.Component.EPG);
			for (int i = 0; i < featureEPG.getChannelCount(); i++)
			{
				Bitmap bmp = featureEPG.getChannelLogoBitmap(i);
				if (bmp != null)
					_zapperListView.addBitmap(bmp);
				else
					Log.w(TAG, "Channel " + featureEPG.getChannelId(i) + " doesn't have image logo!");
			}
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return viewGroup;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) throws StateException
	{
		if (keyCode == KeyEvent.KEYCODE_ENTER)
		{
			boolean isAdded = false;
			try
			{
				MessageBox messageBox = (MessageBox) Environment.getInstance().getFeatureState(
				        FeatureName.State.MESSAGE_BOX);
				isAdded = messageBox.isAdded();
			}
			catch (FeatureNotFoundException e)
			{
				Log.e(TAG, e.getMessage(), e);
			}

			// show hide message box
			if (isAdded)
			{
				Environment.getInstance().getStateManager().hideMessage();
			}
			else
			{
				Environment.getInstance().getStateManager()
				        .showMessage(MessageBox.Type.ERROR, R.string.connection_lost);
			}
		}
		return _zapperListView.onKeyDown(keyCode, event);
	}
}
