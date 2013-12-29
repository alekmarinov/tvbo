/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureStateWatchlist.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.aviqtv.state.channels;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.aviqtv.state.StatusBar;
import com.aviq.tv.android.aviqtv.state.ThumbnailsView;
import com.aviq.tv.android.aviqtv.state.menu.FeatureStateMenu;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.ResultCode;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.IStateMenuItem;
import com.aviq.tv.android.sdk.feature.channels.FeatureChannels;
import com.aviq.tv.android.sdk.feature.epg.Channel;
import com.aviq.tv.android.sdk.feature.epg.EpgData;
import com.aviq.tv.android.sdk.feature.epg.FeatureEPG;

/**
 * Watchlist state feature
 */
public class FeatureStateChannels extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateChannels.class.getSimpleName();

	private ViewGroup _viewGroup;
	private FeatureChannels _featureChannels;
	private EpgData _epgData;

	public FeatureStateChannels()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.Components.add(FeatureName.Component.CHANNELS);
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
			FeatureEPG featureEPG = (FeatureEPG) Environment.getInstance().getFeatureComponent(
			        FeatureName.Component.EPG);
			_epgData = featureEPG.getEpgData();
			_featureChannels = (FeatureChannels) Environment.getInstance().getFeatureComponent(
			        FeatureName.Component.CHANNELS);

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

		final ThumbnailsView allchannelsGrid = (ThumbnailsView) _viewGroup.findViewById(R.id.allchannels_grid);
		final ThumbnailsView mychannelsGrid = (ThumbnailsView) _viewGroup.findViewById(R.id.mychannels_grid);
		allchannelsGrid.setThumbItemCreater(_thumbnailCreater);
		mychannelsGrid.setThumbItemCreater(_thumbnailCreater);

		// Distribute favorite and non favorite channels
		for (int i = 0; i < _epgData.getChannelCount(); i++)
		{
			Channel channel = _epgData.getChannel(i);
			if (_featureChannels.isChannelFavorite(channel))
				mychannelsGrid.addThumbItem(channel);
			else
				allchannelsGrid.addThumbItem(channel);
		}

		allchannelsGrid.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
			{
				Channel channel = (Channel) view.getTag();
				_featureChannels.setChannelFavorite(channel, true);
				allchannelsGrid.removeThumbAt(position);
				mychannelsGrid.addThumbItem(channel, 0);
				mychannelsGrid.smoothScrollBy(0, 0);
			}
		});

		mychannelsGrid.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
			{
				Channel channel = (Channel) view.getTag();
				_featureChannels.setChannelFavorite(channel, false);
				mychannelsGrid.removeThumbAt(position);
				allchannelsGrid.addThumbItem(channel, 0);
				allchannelsGrid.smoothScrollBy(0, 0);
			}
		});

		allchannelsGrid.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id)
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

		new StatusBar(_viewGroup.findViewById(R.id.status_bar)).enable(StatusBar.Element.NAVIGATION).enable(
		        StatusBar.Element.ADD).enable(StatusBar.Element.REORDER);

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

	private ThumbnailsView.ThumbItemCreater _thumbnailCreater = new ThumbnailsView.ThumbItemCreater()
	{
		@Override
		public View createView(LayoutInflater inflator)
		{
			return inflator.inflate(R.layout.grid_item_channel, null);
		}

		@Override
		public void updateView(View view, Object object)
		{
			Channel channel = (Channel) object;
			ImageView thumbView = (ImageView) view.findViewById(R.id.thumbnail);
			TextView titleView = (TextView) view.findViewById(R.id.title);
			thumbView.setImageBitmap(_epgData.getChannelLogoBitmap(_epgData.getChannelIndex(channel)));
			titleView.setText(channel.getTitle());
		}
	};
}
