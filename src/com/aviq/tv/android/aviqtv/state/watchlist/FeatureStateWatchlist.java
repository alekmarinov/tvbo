/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureStateWatchlist.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.aviqtv.state.watchlist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.aviqtv.state.ThumbnailsView;
import com.aviq.tv.android.aviqtv.state.epg.EpgProgramInfo;
import com.aviq.tv.android.aviqtv.state.menu.FeatureStateMenu;
import com.aviq.tv.android.aviqtv.state.programinfo.FeatureStateProgramInfo;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.ResultCode;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.IStateMenuItem;
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.feature.epg.Program;
import com.aviq.tv.android.sdk.feature.watchlist.FeatureWatchlist;

/**
 * Watchlist state feature
 */
public class FeatureStateWatchlist extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateWatchlist.class.getSimpleName();

	private ViewGroup _viewGroup;
	private FeatureWatchlist _watchlist;
	private EpgProgramInfo _programInfo;
	private ThumbnailsView _watchlistGrid;

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

		_watchlistGrid = (ThumbnailsView) _viewGroup.findViewById(R.id.watchlist_grid);
		_watchlistGrid.setThumbItemCreater(_thumbnailCreater);
		for (Program program: _watchlist.getWatchedPrograms())
		{
			_watchlistGrid.addThumbItem(program);
		}

		_watchlistGrid.setOnItemSelectedListener(_onItemSelectedListener);
		_watchlistGrid.setOnItemClickListener(_onItemClickListener);

		// No items? Then hide some drawables that come with the TextViews
		if (_watchlist.getWatchedPrograms().size() == 0)
			_programInfo.updateBrief(null, null);

		return _viewGroup;
	}

	private OnItemSelectedListener _onItemSelectedListener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
		{
			Program program = (Program) _watchlistGrid.getItemAtPosition(position);
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

				Program program = (Program) _watchlistGrid.getSelectedItem();
				String channelId = program.getChannel().getChannelId();

				Bundle featureParams = new Bundle();
				featureParams.putString(FeatureStateProgramInfo.ARGS_CHANNEL_ID, channelId);
				featureParams.putString(FeatureStateProgramInfo.ARGS_PROGRAM_ID, program.getId());

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
	};

	@Override
	protected void onShow()
	{
		super.onShow();
		_viewGroup.requestFocus();
//BaseAdapter adapter = ((BaseAdapter) _watchlistGrid.getAdapter());
//adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onHide()
	{
		super.onHide();
//_watchlistGrid.getAdapter().unregisterDataSetObserver(observer);
	}

//
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event)
//	{
//		return _gridView.onKeyDown(keyCode, event);
//	}

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
			Program program = (Program)object;
			ImageView thumbView = (ImageView)view.findViewById(R.id.thumbnail);
			TextView titleView = (TextView)view.findViewById(R.id.title);
			// thumbView.setImageBitmap();
			titleView.setText(program.getTitle());
        }
	};
}
