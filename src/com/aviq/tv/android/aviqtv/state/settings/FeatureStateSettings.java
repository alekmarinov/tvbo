/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureStateSettings.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Settings state feature
 */

package com.aviq.tv.android.aviqtv.state.settings;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.feature.player.FeaturePlayer;

/**
 * Settings state feature
 */
public class FeatureStateSettings extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateSettings.class.getSimpleName();

	private ViewGroup _viewGroup;
	private ThumbnailsView _settingsGrid;
	private FeaturePlayer _featurePlayer;
	private List<FeatureState> _settingStates = new ArrayList<FeatureState>();

	public FeatureStateSettings()
	{
		_dependencies.Components.add(FeatureName.Component.PLAYER);
		_dependencies.States.add(FeatureName.State.MENU);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.SETTINGS;
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		try
		{
			// insert in Menu
			FeatureStateMenu featureStateMenu = (FeatureStateMenu) Environment.getInstance().getFeatureState(
			        FeatureName.State.MENU);
			featureStateMenu.addMenuItemState(this);

			_featurePlayer = (FeaturePlayer) Environment.getInstance()
			        .getFeatureComponent(FeatureName.Component.PLAYER);

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
		_viewGroup = (ViewGroup) inflater.inflate(R.layout.state_settings, container, false);

		_settingsGrid = (ThumbnailsView) _viewGroup.findViewById(R.id.settings_grid);
		_settingsGrid.setThumbItemCreater(_thumbnailCreater);

		for (FeatureState settingState: _settingStates)
		{
			_settingsGrid.addThumbItem(settingState);
		}

		_settingsGrid.setOnItemSelectedListener(_onItemSelectedListener);
		_settingsGrid.setOnItemClickListener(_onItemClickListener);

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

				        View container = _viewGroup.findViewById(R.id.infoarea_settings);

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

	public void addSettingState(FeatureState settingState)
	{
		_settingStates.add(settingState);
		Log.i(TAG, "Added setting state " + settingState.getName());
	}

	private OnItemSelectedListener _onItemSelectedListener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
		{
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0)
		{
		}
	};

	private OnItemClickListener _onItemClickListener = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			try
			{
				FeatureState settingState = (FeatureState) _settingsGrid.getSelectedItem();
				Environment.getInstance().getStateManager().setStateMain(settingState, null);
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
		return R.drawable.ic_menu_settings;
	}

	@Override
	public String getMenuItemCaption()
	{
		return Environment.getInstance().getResources().getString(R.string.menu_settings);
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
			IStateMenuItem settingState = (IStateMenuItem) object;

			ImageView thumbView = (ImageView) view.findViewById(R.id.thumbnail);
			TextView titleView = (TextView) view.findViewById(R.id.title);

			thumbView.setImageDrawable(Environment.getInstance().getResources()
			        .getDrawable(settingState.getMenuItemResourceId()));
			titleView.setText(settingState.getMenuItemCaption());
		}
	};
}
