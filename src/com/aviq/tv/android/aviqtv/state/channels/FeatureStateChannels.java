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
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.aviqtv.state.StatusBar;
import com.aviq.tv.android.aviqtv.state.ThumbnailsView;
import com.aviq.tv.android.aviqtv.state.ThumbnailsView.ThumbItemCreater;
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
import com.aviq.tv.android.sdk.feature.player.FeaturePlayer;

/**
 * Watchlist state feature
 */
public class FeatureStateChannels extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateChannels.class.getSimpleName();

	private ViewGroup _viewGroup;
	private FeaturePlayer _featurePlayer;
	private FeatureChannels _featureChannels;
	private EpgData _epgData;
	private boolean _isReorderMode = false;
	private ThumbnailsView _allChannelsGrid;
	private ThumbnailsView _myChannelsGrid;
	private int _lockedItemPosition;

	public FeatureStateChannels()
	{
		_dependencies.Components.add(FeatureName.Component.PLAYER);
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.Components.add(FeatureName.Component.CHANNELS);
		_dependencies.States.add(FeatureName.State.MENU);
	}

	private enum StatusBarState
	{
		ALL_CHANNELS, MY_CHANNELS, MY_CHANNELS_REORDER
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

			_featurePlayer = (FeaturePlayer) Environment.getInstance()
			        .getFeatureComponent(FeatureName.Component.PLAYER);

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
		_featurePlayer.setVideoViewFullScreen();

		_viewGroup = (ViewGroup) inflater.inflate(R.layout.state_channels, container, false);

		_allChannelsGrid = (ThumbnailsView) _viewGroup.findViewById(R.id.allchannels_grid);
		_myChannelsGrid = (ThumbnailsView) _viewGroup.findViewById(R.id.mychannels_grid);
		_allChannelsGrid.setThumbItemCreater(_thumbnailCreater);
		_myChannelsGrid.setThumbItemCreater(_thumbnailCreater);

		_myChannelsGrid.setOnKeyListener(new OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				Log.d(TAG, ".onKey: keyCode = " + keyCode);
				if (!event.isDown())
					return false;
				if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
				{
					switchReorderMode();
					return true;
				}
				else if (_isReorderMode)
				{
					int position1 = _myChannelsGrid.getSelectedItemPosition();
					int position2 = -1;
					switch (keyCode)
					{
						case KeyEvent.KEYCODE_DPAD_LEFT:
							if (position1 % 2 == 1)
								position2 = position1 - 1;
						break;
						case KeyEvent.KEYCODE_DPAD_RIGHT:
							if (position1 % 2 == 0)
								position2 = position1 + 1;
						break;
						case KeyEvent.KEYCODE_DPAD_UP:
							if (position1 - 2 >= 0)
								position2 = position1 - 2;
						break;
						case KeyEvent.KEYCODE_DPAD_DOWN:
							if (position1 + 2 < _myChannelsGrid.getCount())
								position2 = position1 + 2;
						break;
					}
					if (position2 != -1)
					{
						Log.d(TAG, "swap channels from " + position1 + " to " + position2);
						// swap channel positions
						_myChannelsGrid.swapPositions(position1, position2, false);
						_featureChannels.swapChannelPositions(position1, position2);
						_myChannelsGrid.setSelection(position2);
						_lockedItemPosition = position2;
						_myChannelsGrid.notifyDataSetChanged();
						return true;
					}
				}
				return false;
			}
		});

		_allChannelsGrid.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
					setStatusBarState(StatusBarState.ALL_CHANNELS);
			}
		});

		_myChannelsGrid.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
					setStatusBarState(StatusBarState.MY_CHANNELS);
			}
		});

		// Add all non favorite channels
		for (Channel channel : _epgData.getChannels())
		{
			if (!_featureChannels.isChannelFavorite(channel))
				_allChannelsGrid.addThumbItem(channel);
		}

		// Add all favorite channels if they ever changed
		if (_featureChannels.isEverChanged())
		{
			for (Channel channel : _featureChannels.getFavoriteChannels())
			{
				Log.i(TAG, "Add channel " + channel);
				_myChannelsGrid.addThumbItem(channel);
			}
		}

		_allChannelsGrid.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
			{
				Channel channel = (Channel) view.getTag();
				_featureChannels.setChannelFavorite(channel, true);
				_allChannelsGrid.removeThumbAt(position);
				_myChannelsGrid.addThumbItem(channel);
				_myChannelsGrid.setSelection(_myChannelsGrid.getCount() - 1);
				_myChannelsGrid.smoothScrollBy(0, 99999);
			}
		});

		_myChannelsGrid.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
			{
				Channel channel = (Channel) view.getTag();
				_featureChannels.setChannelFavorite(channel, false);
				_myChannelsGrid.removeThumbAt(position);
				_allChannelsGrid.addThumbItem(channel);
				_allChannelsGrid.setSelection(_allChannelsGrid.getCount() - 1);
				_allChannelsGrid.smoothScrollBy(0, 99999);
			}
		});

		_allChannelsGrid.requestFocus();
		return _viewGroup;
	}

	@Override
	public void onShow()
	{
		super.onShow();
		_viewGroup.requestFocus();
	}

	/**
	 * On hiding this FeatureState
	 */
	@Override
	protected void onHide()
	{
		// Save channels list
		if (_featureChannels.isModified())
			_featureChannels.save();
		if (_isReorderMode)
			switchReorderMode();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.i(TAG, ".onKeyDown: keyCode = " + keyCode);
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

	private ThumbItemCreater _thumbnailCreater = new ThumbItemCreater()
	{
		@Override
		public View createView(ThumbnailsView parent, int position, LayoutInflater inflator)
		{
			return inflator.inflate(R.layout.grid_item_channel, null);
		}

		@Override
		public void updateView(ThumbnailsView parent, int position, View view, Object object)
		{
			Channel channel = (Channel) object;
			ImageView thumbView = (ImageView) view.findViewById(R.id.thumbnail);
			TextView titleView = (TextView) view.findViewById(R.id.title);
			thumbView.setImageBitmap(_epgData.getChannelLogoBitmap(channel.getIndex()));
			String title = channel.getTitle();
			if (parent.equals(_myChannelsGrid))
				title = (position + 1) + " " + title;
			titleView.setText(title);

			if (_isReorderMode && position == _lockedItemPosition)
				view.setBackgroundResource(R.drawable.channel_thumbnail_locked);
			else
				view.setBackgroundResource(R.drawable.watchlist_item_selector);
		}
	};

	private void setStatusBarState(StatusBarState statusBarState)
	{
		switch (statusBarState)
		{
			case ALL_CHANNELS:
				new StatusBar(_viewGroup.findViewById(R.id.status_bar)).enable(StatusBar.Element.NAVIGATION).enable(
				        StatusBar.Element.ADD);
			break;
			case MY_CHANNELS:
				new StatusBar(_viewGroup.findViewById(R.id.status_bar)).enable(StatusBar.Element.NAVIGATION)
				        .enable(StatusBar.Element.REMOVE).enable(StatusBar.Element.START_REORDER);
			break;
			case MY_CHANNELS_REORDER:
				new StatusBar(_viewGroup.findViewById(R.id.status_bar)).enable(StatusBar.Element.NAVIGATION)
				        .enable(StatusBar.Element.ADD).enable(StatusBar.Element.STOP_REORDER);
			break;
		}
	}

	private void switchReorderMode()
	{
		_isReorderMode = !_isReorderMode;
		if (_isReorderMode)
		{
			setStatusBarState(StatusBarState.MY_CHANNELS_REORDER);
			_lockedItemPosition = _myChannelsGrid.getSelectedItemPosition();
		}
		else
			setStatusBarState(StatusBarState.MY_CHANNELS);
		_myChannelsGrid.notifyDataSetChanged();
	}
}
