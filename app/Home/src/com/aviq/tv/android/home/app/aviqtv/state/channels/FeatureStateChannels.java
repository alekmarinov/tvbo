/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureStateWatchlist.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.home.app.aviqtv.state.channels;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.app.aviqtv.state.menu.FeatureStateMenu;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.core.state.IStateMenuItem;
import com.aviq.tv.android.home.feature.epg.EpgData;
import com.aviq.tv.android.home.feature.epg.FeatureEPG;
import com.aviq.tv.android.home.feature.state.ThumbnailsView;

/**
 * Watchlist state feature
 */
public class FeatureStateChannels extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateChannels.class.getSimpleName();

	private ViewGroup _viewGroup;
	private FeatureEPG _featureEPG;
	private EpgData _epgData;

	public FeatureStateChannels()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.States.add(FeatureName.State.MENU);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.CHANNELS;
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		try
		{
			_featureEPG = (FeatureEPG) Environment.getInstance().getFeatureComponent(FeatureName.Component.EPG);

			// insert in Menu
			FeatureStateMenu featureStateMenu = (FeatureStateMenu) Environment.getInstance().getFeatureState(
			        FeatureName.State.MENU);
			featureStateMenu.addMenuItemState(this);

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
		_viewGroup = (ViewGroup) inflater.inflate(R.layout.state_channels, container, false);

		ThumbnailsView allchannelsGrid = (ThumbnailsView) _viewGroup.findViewById(R.id.allchannels_grid);
		ThumbnailsView mychannelsGrid = (ThumbnailsView) _viewGroup.findViewById(R.id.mychannels_grid);

		_epgData = _featureEPG.getEpgData();

		allchannelsGrid.setGridItemResourceLayout(R.layout.grid_item_channel);
		for (int i = 0; i < _epgData.getChannelCount(); i++)
		{
			allchannelsGrid.addGridItem(_epgData.getChannelLogoBitmap(i), _epgData.getChannel(i).getTitle());
		}

		allchannelsGrid.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id)
			{
				Log.d(TAG, "onItemSelected " + position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// TODO Auto-generated method stub
			}
		});
		allchannelsGrid.requestFocus();

		return _viewGroup;
	}

	@Override
    public void onShow()
	{
		super.onShow();
		_viewGroup.requestFocus();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_ENTER:
				return true;
		}
		return false;
	}

	@Override
	public int getMenuItemResourceId()
	{
		return R.drawable.ic_menu_my_channels;
	}

	@Override
	public String getMenuItemCaption()
	{
		return getStateName().name();
	}
}
