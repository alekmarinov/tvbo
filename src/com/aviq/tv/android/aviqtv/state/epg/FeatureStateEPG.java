/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureTV.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.aviqtv.state.epg;

import java.util.Calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.aviqtv.state.StatusBar;
import com.aviq.tv.android.aviqtv.state.epg.EpgGrid.NAVIGATION;
import com.aviq.tv.android.aviqtv.state.epg.EpgGrid.OnEpgGridEventListener;
import com.aviq.tv.android.aviqtv.state.menu.FeatureStateMenu;
import com.aviq.tv.android.aviqtv.state.programinfo.FeatureStateProgramInfo;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.ResultCode;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.IStateMenuItem;
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.feature.epg.Channel;
import com.aviq.tv.android.sdk.feature.epg.EpgData;
import com.aviq.tv.android.sdk.feature.epg.FeatureEPG;
import com.aviq.tv.android.sdk.feature.epg.Program;
import com.aviq.tv.android.sdk.feature.player.FeaturePlayer;

/**
 * EPG state feature
 */
public class FeatureStateEPG extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateEPG.class.getSimpleName();

	private FeatureEPG _featureEPG;
	private FeaturePlayer _featurePlayer;
	private EpgGrid _epgGrid;
	private EpgHeaderView _gridHeader;
	private EpgListView _gridList;
	private TextView _dateTimeView;
	private EpgProgramInfo _programInfo;
	private View _videoPlaceHolder;
	private View _rootView;
	private ImageView _gridTimebar;

	private EpgData _epgData;

	public FeatureStateEPG()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.Components.add(FeatureName.Component.PLAYER);
		_dependencies.States.add(FeatureName.State.MENU);
		_dependencies.States.add(FeatureName.State.PROGRAM_INFO);
		_dependencies.States.add(FeatureName.State.MESSAGE_BOX);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		try
		{
			_featureEPG = (FeatureEPG) Environment.getInstance().getFeatureComponent(FeatureName.Component.EPG);
			_featurePlayer = (FeaturePlayer) Environment.getInstance()
			        .getFeatureComponent(FeatureName.Component.PLAYER);

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
	public FeatureName.State getStateName()
	{
		return FeatureName.State.EPG;
	}

	// IMenuItemState implementation

	@Override
	public int getMenuItemResourceId()
	{
		return R.drawable.ic_menu_epg;
	}

	@Override
	public String getMenuItemCaption()
	{
		return Environment.getInstance().getResources().getString(R.string.menu_epg);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");

		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_epg, container, false);
		_dateTimeView = (TextView) viewGroup.findViewById(R.id.datetime);
		_gridHeader = (EpgHeaderView) viewGroup.findViewById(R.id.time_list);
		_gridList = (EpgListView) viewGroup.findViewById(R.id.gridList);
		_gridTimebar = (ImageView) viewGroup.findViewById(R.id.timebar);

		ViewGroup programInfoContainer = (ViewGroup) viewGroup.findViewById(R.id.infoarea_program);
		_programInfo = new EpgProgramInfo(getActivity(), programInfoContainer);
		_videoPlaceHolder = viewGroup.findViewById(R.id.videoview_placeholder);

		_rootView = viewGroup;

		// Hide player while view is relayouting and show it by the global layout listener
		_featurePlayer.hideVideoView();

		initEpgGridOnGlobalLayout();

		new StatusBar(viewGroup.findViewById(R.id.status_bar)).enable(StatusBar.Element.NAVIGATION).enable(
		        StatusBar.Element.DETAILS);
		return viewGroup;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		_epgGrid.deactivate();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.i(TAG, ".onKeyDown: keyCode = " + keyCode);

		if (keyCode == KeyEvent.KEYCODE_ENTER)
		{
			String channelId = _epgGrid.getSelectedChannel().getChannelId();
			
			Program program = (Program) _epgGrid.getSelectedProgramList().getSelectedItem();
			if (program != null)
			{
				String programId = program.getId();
				showProgramInfo(channelId, programId);
			}
			return true;
		}
		return _epgGrid.onKeyDown(keyCode, event);
	}

	private void initEpgGridOnGlobalLayout()
	{
		_gridHeader.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				_gridHeader.getViewTreeObserver().removeGlobalOnLayoutListener(this);

				View container = _rootView.findViewById(R.id.infoarea_program);

				int x = (int)(_rootView.getX() + container.getX() + _videoPlaceHolder.getX());
				int y = (int)(_rootView.getY() + container.getY() + _videoPlaceHolder.getY());
				int w = _videoPlaceHolder.getWidth();
				int h = _videoPlaceHolder.getHeight();
				_featurePlayer.setVideoViewPositionAndSize(x, y, w, h);

				// This method relies on the width of the _gridHeader widget. In
				// onResume() it is still zero, therefore, we get it from the
				// OnGlobalLayoutListener.
				initEpgGrid();
			}
		});
	}

	private void initEpgGrid()
	{
		_epgData = _featureEPG.getEpgData();
		_epgGrid = new EpgGrid(getActivity());
		_epgGrid.setDataProvider(_epgData);

		setEpgGridHeaderAbsMinTime();
		setEpgGridHeaderAbsMaxTime();

		_epgGrid.setEpgHeaderView(_gridHeader);
		_epgGrid.setDateTimeView(_dateTimeView);
		_epgGrid.setTimebarImageView(_gridTimebar);

		_epgGrid.setEpgListView(_gridList);

		// FIXME: get current channel
		Channel channel = _epgData.getChannel(0);
		_epgGrid.setSelectedChannel(channel);
		_epgGrid.setOnEpgGridItemSelection(_onEpgGridItemSelectionListener);
		_epgGrid.prepareEpg();
	}

	private void setEpgGridHeaderAbsMinTime()
	{
		Calendar now = Calendar.getInstance();

		int hoursBack = (int) Math.abs((now.getTimeInMillis() - _epgData.getMinEpgStartTime().getTimeInMillis())
		        / (1000 * 60 * 60));
		Calendar absMin = Calendar.getInstance();
		absMin.add(Calendar.HOUR_OF_DAY, -hoursBack);
		_gridHeader.setAbsoluteTimeMin(absMin);
	}

	private void setEpgGridHeaderAbsMaxTime()
	{
		Calendar now = Calendar.getInstance();

		int hoursAhead = (int) Math.abs((now.getTimeInMillis() - _epgData.getMaxEpgStopTime().getTimeInMillis())
		        / (1000 * 60 * 60));
		Calendar absMax = Calendar.getInstance();
		absMax.add(Calendar.HOUR_OF_DAY, hoursAhead);
		_gridHeader.setAbsoluteTimeMax(absMax);
	}

	private void showProgramInfo(String channelId, String programId)
	{
		try
		{
			FeatureStateProgramInfo programInfo = (FeatureStateProgramInfo) Environment.getInstance()
			        .getFeatureState(FeatureName.State.PROGRAM_INFO);

			Bundle featureParams = new Bundle();
			featureParams.putString(FeatureStateProgramInfo.ARGS_CHANNEL_ID, channelId);
			featureParams.putString(FeatureStateProgramInfo.ARGS_PROGRAM_ID, programId);

			Environment.getInstance().getStateManager().setStateOverlay(programInfo, featureParams);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		catch (StateException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	private final OnEpgGridEventListener _onEpgGridItemSelectionListener = new OnEpgGridEventListener()
	{
		@Override
		public void onEpgGridItemSelecting(Channel channel, Program program)
		{
		}

		@Override
		public void onEpgGridItemSelected(Channel channel, Program program)
		{
			_programInfo.updateBrief(channel.getChannelId(), program);
		}

		@Override
		public void onEpgPageScroll(NAVIGATION direction)
		{

		}

		@Override
        public void onEpgGridItemLongPress(Channel channel, Program program)
        {
			showProgramInfo(channel.getChannelId(), program.getId());
        }
	};

}
