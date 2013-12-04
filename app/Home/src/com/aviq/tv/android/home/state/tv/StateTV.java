/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    StateTV.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: Defines the fullscreen TV state
 */

package com.aviq.tv.android.home.state.tv;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.FeatureName;
import com.aviq.tv.android.home.core.FeatureNotFoundException;
import com.aviq.tv.android.home.feature.FeatureEPG;
import com.aviq.tv.android.home.state.BaseState;
import com.aviq.tv.android.home.state.StateEnum;
import com.aviq.tv.android.home.state.StateException;

/**
 * Defines the fullscreen TV state
 *
 */
public class StateTV extends BaseState
{
	private static final String TAG = StateTV.class.getSimpleName();
	private ZapperListView _zapperListView;

	/**
	 * @param Environment
	 */
	public StateTV(Environment environment)
	{
		super(environment, StateEnum.TV);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_tv, container, false);
		_zapperListView = (ZapperListView)viewGroup.findViewById(R.id.zapperListView);
        try
        {
        	FeatureEPG featureEPG = (FeatureEPG) _environment.getFeatureComponent(FeatureName.Component.EPG);
			for (int i = 0; i < featureEPG.getChannelCount(); i++)
			{
				_zapperListView.addBitmap(featureEPG.getChannelLogoBitmap(i));
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
		return _zapperListView.onKeyDown(keyCode, event);
	}
}
