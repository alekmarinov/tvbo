/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureStateWatchlist.java
 * Author:      zheliazko
 * Date:        30 Jan 2014
 * Description: WebTV state feature
 */

package com.aviq.tv.android.aviqtv.state.webtv;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.aviqtv.state.StatusBar;
import com.aviq.tv.android.aviqtv.state.ThumbnailsView;
import com.aviq.tv.android.aviqtv.state.epg.EpgProgramInfo;
import com.aviq.tv.android.aviqtv.state.menu.FeatureStateMenu;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.IStateMenuItem;
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.core.state.StateManager;
import com.aviq.tv.android.sdk.feature.channels.FeatureChannels;
import com.aviq.tv.android.sdk.feature.player.FeaturePlayer;
import com.aviq.tv.android.sdk.feature.webtv.FeatureWebTV;
import com.aviq.tv.android.sdk.feature.webtv.WebTVItem;

/**
 * WebTV state feature
 */
public class FeatureStateWebTV extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateWebTV.class.getSimpleName();

	private ViewGroup _viewGroup;
	private FeatureWebTV _featureWebTV;
	private FeatureChannels _featureChannels;
	private EpgProgramInfo _programInfo;
	private ThumbnailsView _webtvGrid;
	private FeaturePlayer _featurePlayer;

	public FeatureStateWebTV() throws FeatureNotFoundException
	{
		require(FeatureName.Component.PLAYER);
		require(FeatureName.Component.CHANNELS);
		require(FeatureName.Component.WEBTV);
		require(FeatureName.State.WEBTV_VIDEO);
		require(FeatureName.State.PROGRAM_INFO);
		require(FeatureName.State.MENU);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.WEBTV;
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		// insert in Menu
		FeatureStateMenu featureStateMenu = (FeatureStateMenu) Environment.getInstance().getFeatureState(
		        FeatureName.State.MENU);
		featureStateMenu.addMenuItemState(this);

		_featurePlayer = (FeaturePlayer) Environment.getInstance()
		        .getFeatureComponent(FeatureName.Component.PLAYER);
		_featureWebTV = (FeatureWebTV) Environment.getInstance().getFeatureComponent(FeatureName.Component.WEBTV);
		_featureChannels = (FeatureChannels) Environment.getInstance().getFeatureComponent(
		        FeatureName.Component.CHANNELS);
		super.initialize(onFeatureInitialized);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		_viewGroup = (ViewGroup) inflater.inflate(R.layout.state_webtv, container, false);

		ViewGroup programInfoContainer = (ViewGroup) _viewGroup.findViewById(R.id.infoarea_program);
		_programInfo = new EpgProgramInfo(getActivity(), programInfoContainer);

		_webtvGrid = (ThumbnailsView) _viewGroup.findViewById(R.id.watchlist_grid);
		_webtvGrid.setThumbItemCreater(_thumbnailCreater);
		_webtvGrid.setThumbItems(_featureWebTV.getVideoStreams());

		_webtvGrid.setOnItemSelectedListener(_onItemSelectedListener);
		_webtvGrid.setOnItemClickListener(_onItemClickListener);

		// No items? Then hide some drawables that come with the TextViews
		if (_featureWebTV.getVideoStreams().size() == 0)
			_programInfo.updateBrief(null, null);

		// Hide player while view re-layouts and show it by the global layout
		// listener
		_featurePlayer.hide();
		final View videoViewPlaceHolder = _viewGroup.findViewById(R.id.videoview_placeholder);

		videoViewPlaceHolder.getViewTreeObserver().addOnGlobalLayoutListener(
		        new ViewTreeObserver.OnGlobalLayoutListener()
		        {
			        @Override
			        public void onGlobalLayout()
			        {
				        videoViewPlaceHolder.getViewTreeObserver().removeGlobalOnLayoutListener(this);

				        View container = _viewGroup.findViewById(R.id.infoarea_program);

				        int x = (int) (_viewGroup.getX() + container.getX() + videoViewPlaceHolder.getX());
				        int y = (int) (_viewGroup.getY() + container.getY() + videoViewPlaceHolder.getY());
				        int w = videoViewPlaceHolder.getWidth();
				        int h = videoViewPlaceHolder.getHeight();
				        _featurePlayer.setPositionAndSize(x, y, w, h);
			        }
		        });

		new StatusBar(_viewGroup.findViewById(R.id.status_bar)).enable(StatusBar.Element.NAVIGATION).enable(
		        StatusBar.Element.DETAILS);
		return _viewGroup;
	}

	private OnItemSelectedListener _onItemSelectedListener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
		{
			WebTVItem item = (WebTVItem) _webtvGrid.getItemAtPosition(position);

			_programInfo.updatePrimaryTitle(item.getName());

			_programInfo.updateDateTime(null, 0, null, 0);

			String genres = TextUtils.join(", ", item.getGenres());
			_programInfo.updateSecondaryTitle(genres);

			// _programInfo.updateSummary(item.getDescription());
			String languages = TextUtils.join(", ", item.getLanguages());
			_programInfo.updateSummary(languages);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0)
		{
			_programInfo.updateBrief(null, null);
		}
	};

	private OnItemClickListener _onItemClickListener = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			WebTVItem item = (WebTVItem) _webtvGrid.getItemAtPosition(position);

			try
			{
				StateManager stateManager = Environment.getInstance().getStateManager();
				FeatureState featureState = Environment.getInstance().getFeatureState(FeatureName.State.WEBTV_VIDEO);

				Bundle featureParams = new Bundle();
				featureParams.putString(FeatureStateWebTVVideo.ARGS_URL, item.getUri());
				featureParams.putString(FeatureStateWebTVVideo.ARGS_CHANNEL_NAME, null);

				stateManager.setStateOverlay(featureState, featureParams);
			}
			catch (StateException e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
		}
	};

	@Override
	protected void onShow(boolean isViewUncovered)
	{
		super.onShow(isViewUncovered);
		_featureChannels.playLast();
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
		return R.drawable.ic_menu_webtv;
	}

	@Override
	public String getMenuItemCaption()
	{
		return Environment.getInstance().getResources().getString(R.string.menu_webtv);
	}

	private ThumbnailsView.ThumbItemCreater _thumbnailCreater = new ThumbnailsView.ThumbItemCreater()
	{
		@Override
		public View createView(ThumbnailsView parent, int position, LayoutInflater inflator)
		{
			return inflator.inflate(R.layout.grid_item_watchlist, null);
		}

		@Override
		public void updateView(ThumbnailsView parent, int position, View view, Object object)
		{
			WebTVItem item = (WebTVItem) object;

			// ImageView thumbView = (ImageView)
			// view.findViewById(R.id.thumbnail);
			// thumbView.setImageBitmap();

			// Environment.getInstance().getImageLoader().getImageListener(thumbView, R.drawable., errorImageResId);
			TextView titleView = (TextView) view.findViewById(R.id.title);
			titleView.setText(item.getName());
		}
	};
}
