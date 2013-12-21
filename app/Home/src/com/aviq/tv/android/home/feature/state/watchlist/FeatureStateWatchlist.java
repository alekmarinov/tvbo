/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureStateWatchlist.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.home.feature.state.watchlist;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.core.state.BaseState;
import com.aviq.tv.android.home.core.state.IStateMenuItem;
import com.aviq.tv.android.home.core.state.StateException;
import com.aviq.tv.android.home.feature.FeatureWatchlist;
import com.aviq.tv.android.home.feature.epg.Program;
import com.aviq.tv.android.home.feature.state.epg.EpgProgramInfo;
import com.aviq.tv.android.home.feature.state.menu.FeatureStateMenu;
import com.aviq.tv.android.home.feature.state.programinfo.FeatureStateProgramInfo;

/**
 * Watchlist state feature
 */
public class FeatureStateWatchlist extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateWatchlist.class.getSimpleName();

	private ViewGroup _viewGroup;
	private FeatureWatchlist _watchlist;
	private EpgProgramInfo _programInfo;
	private GridView _gridView;
	private GridViewAdapter<Program> _adapter;

	public FeatureStateWatchlist()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.Components.add(FeatureName.Component.WATCHLIST);
		_dependencies.States.add(FeatureName.State.PROGRAM_INFO);
		_dependencies.States.add(FeatureName.State.MENU);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.WATCHLIST;
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

			_watchlist = (FeatureWatchlist) Environment.getInstance().getFeatureComponent(FeatureName.Component.WATCHLIST);

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
		_viewGroup = (ViewGroup) inflater.inflate(R.layout.state_watchlist, container, false);

		ViewGroup programInfoContainer = (ViewGroup) _viewGroup.findViewById(R.id.program_details_container);
		_programInfo = new EpgProgramInfo(getActivity(), programInfoContainer);
		
		_gridView = (GridView) _viewGroup.findViewById(R.id.watchlist_grid);
		_adapter = new GridViewAdapter<Program>(Environment.getInstance().getContext(),
		        _watchlist.getWatchedPrograms(), R.layout.grid_item_watchlist);
		_gridView.setAdapter(_adapter);
		_gridView.setOnItemSelectedListener(_onItemSelectedListener);
		_gridView.setOnItemClickListener(_onItemClickListener);
//		_viewGroup.requestFocus();

		// Initial refresh of the program info widget
		if (_adapter.getCount() > 0)
		{
			Program program = (Program) _adapter.getItem(0);
			_programInfo.updateBrief(program.getChannel().getChannelId(), program);
		}
		
		return _viewGroup;
	}
	
	private OnItemSelectedListener _onItemSelectedListener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
		{
			Program program = (Program) _adapter.getItem(position);
			_programInfo.updateBrief(program.getChannel().getChannelId(), program);
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
				FeatureStateProgramInfo programInfo = (FeatureStateProgramInfo) Environment.getInstance()
				        .getFeatureState(FeatureName.State.PROGRAM_INFO);
				
				Program program = (Program) _gridView.getSelectedItem();
				String channelId = program.getChannel().getChannelId();
				
				Bundle featureParams = new Bundle();
				featureParams.putString(FeatureStateProgramInfo.ARGS_CHANNEL_ID, channelId);
				featureParams.putString(FeatureStateProgramInfo.ARGS_PROGRAM_ID, program.getId());
				
				Environment.getInstance().getStateManager().setStateOverlay((BaseState) programInfo, featureParams);
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
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		return _gridView.onKeyDown(keyCode, event);
	}

	@Override
	public int getMenuItemResourceId()
	{
		return R.drawable.ic_menu_watchlist;
	}

	@Override
	public String getMenuItemCaption()
	{
		return getStateName().name();
	}
}
