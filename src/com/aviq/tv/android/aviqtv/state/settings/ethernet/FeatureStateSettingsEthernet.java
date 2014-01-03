/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureStateSettingsEthernet.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Ethernet settings state feature
 */

package com.aviq.tv.android.aviqtv.state.settings.ethernet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.aviqtv.state.settings.FeatureStateSettings;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.ResultCode;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.IStateMenuItem;

/**
 * Ethernet settings state feature
 */
public class FeatureStateSettingsEthernet extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateSettingsEthernet.class.getSimpleName();
	private ViewGroup _viewGroup;

	public FeatureStateSettingsEthernet()
	{
		_dependencies.Components.add(FeatureName.Component.ETHERNET);
		_dependencies.States.add(FeatureName.State.SETTINGS);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.SETTINGS_ETHERNET;
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		try
		{
			// insert in Settings
			FeatureStateSettings featureStateSettings = (FeatureStateSettings) Environment.getInstance().getFeatureState(
			        FeatureName.State.SETTINGS);
			featureStateSettings.addSettingState(this);

			onFeatureInitialized.onInitialized(this, ResultCode.OK);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
			onFeatureInitialized.onInitialized(this, ResultCode.GENERAL_FAILURE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		_viewGroup = (ViewGroup) inflater.inflate(R.layout.state_settings_ethernet, container, false);
		return _viewGroup;
	}

	@Override
	protected void onShow(boolean isViewUncovered)
	{
		super.onShow(isViewUncovered);
		_viewGroup.requestFocus();
	}

	@Override
	protected void onHide(boolean isViewCovered)
	{
		super.onHide(isViewCovered);
	}

	@Override
	public int getMenuItemResourceId()
	{
		// FIXME: replace with ic_menu_settings_ethernet when ready
		return R.drawable.ic_menu_settings;
	}

	@Override
	public String getMenuItemCaption()
	{
		return Environment.getInstance().getResources().getString(R.string.menu_settings_ethernet);
	}
}
