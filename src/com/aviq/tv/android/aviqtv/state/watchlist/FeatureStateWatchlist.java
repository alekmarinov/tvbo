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

import java.util.Calendar;

import android.content.res.Resources;
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
import com.aviq.tv.android.aviqtv.state.MessageBox;
import com.aviq.tv.android.aviqtv.state.StatusBar;
import com.aviq.tv.android.aviqtv.state.ThumbnailsView;
import com.aviq.tv.android.aviqtv.state.epg.EpgProgramInfo;
import com.aviq.tv.android.aviqtv.state.menu.FeatureStateMenu;
import com.aviq.tv.android.aviqtv.state.programinfo.FeatureStateProgramInfo;
import com.aviq.tv.android.aviqtv.state.tv.FeatureStateTV;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.ResultCode;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.IStateMenuItem;
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.core.state.StateManager.MessageParams;
import com.aviq.tv.android.sdk.feature.epg.FeatureEPG;
import com.aviq.tv.android.sdk.feature.epg.Program;
import com.aviq.tv.android.sdk.feature.player.FeaturePlayer;
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
	private FeaturePlayer _featurePlayer;
	private FeatureEPG _featureEPG;
	private FeatureStateTV _featureStateTV;

	public FeatureStateWatchlist()
	{
		_dependencies.Components.add(FeatureName.Component.PLAYER);
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.Components.add(FeatureName.Component.WATCHLIST);
		_dependencies.States.add(FeatureName.State.TV);
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

			_featurePlayer = (FeaturePlayer) Environment.getInstance()
			        .getFeatureComponent(FeatureName.Component.PLAYER);
			_featureEPG = (FeatureEPG) Environment.getInstance().getFeatureComponent(FeatureName.Component.EPG);
			_watchlist = (FeatureWatchlist) Environment.getInstance().getFeatureComponent(
			        FeatureName.Component.WATCHLIST);
			_featureStateTV = (FeatureStateTV)Environment.getInstance().getFeatureState(FeatureName.State.TV);

			subscribe(_watchlist, FeatureWatchlist.ON_PROGRAM_REMOVED);
			subscribe(_watchlist, FeatureWatchlist.ON_PROGRAM_ADDED);

			_watchlist.getEventMessenger().register(this, FeatureWatchlist.ON_PROGRAM_NOTIFY);

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

		ViewGroup programInfoContainer = (ViewGroup) _viewGroup.findViewById(R.id.infoarea_program);
		_programInfo = new EpgProgramInfo(getActivity(), programInfoContainer);

		_watchlistGrid = (ThumbnailsView) _viewGroup.findViewById(R.id.watchlist_grid);
		_watchlistGrid.setThumbItemCreater(_thumbnailCreater);
		_watchlistGrid.setThumbItems(_watchlist.getWatchedPrograms());

		_watchlistGrid.setOnItemSelectedListener(_onItemSelectedListener);
		_watchlistGrid.setOnItemClickListener(_onItemClickListener);

		// No items? Then hide some drawables that come with the TextViews
		if (_watchlist.getWatchedPrograms().size() == 0)
			_programInfo.updateBrief(null, null);

		// Hide player while view re-layouts and show it by the global layout
		// listener
		_featurePlayer.hideVideoView();
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
				        _featurePlayer.setVideoViewPositionAndSize(x, y, w, h);
			        }
		        });

		new StatusBar(_viewGroup.findViewById(R.id.status_bar)).enable(StatusBar.Element.NAVIGATION).enable(
		        StatusBar.Element.DETAILS);
		return _viewGroup;
	}

	@Override
	public void onEvent(int msgId, Bundle bundle)
	{
		Log.i(TAG, ".onEvent: msgId = " + msgId);
		if (msgId == FeatureWatchlist.ON_PROGRAM_NOTIFY)
		{
			String programId = bundle.getString("PROGRAM");
			String channelId = bundle.getString("CHANNEL");
			Log.i(TAG, ".onEvent: ON_PROGRAM_NOTIFY - > " + channelId + "/" + programId);
			Program program = _featureEPG.getEpgData().getProgram(channelId, programId);
			int minsRemaining = (int)(Calendar.getInstance().getTimeInMillis() - program.getStartTimeCalendar().getTimeInMillis()) / (60 * 1000);
			Resources resources = Environment.getInstance().getResources();

			String messageTitle = resources.getString(R.string.watchlist_notify_title);
			String messageText;
			if (minsRemaining > 0)
			{
				messageText = String.format(resources.getString(R.string.watchlist_notify_soon), minsRemaining,
						program.getTitle(), program.getChannel().getTitle());
			}
			else
			{
				messageText = String.format(resources.getString(R.string.watchlist_notify_now), program.getTitle(), program.getChannel().getTitle());
			}
			MessageParams messageParams = new MessageParams().setType(MessageParams.Type.INFO).setTitle(messageTitle).setText(messageText)
			        .enableButton(MessageParams.Button.YES).enableButton(MessageParams.Button.NO);
			// copy event params to message bundle
			messageParams.getParamsBundle().putAll(bundle);
			MessageBox messageBox = (MessageBox)Environment.getInstance().getStateManager().showMessage(messageParams);
			messageBox.getEventMessenger().register(this, MessageBox.ON_BUTTON_PRESSED);
		}
		else if (msgId == MessageBox.ON_BUTTON_PRESSED)
		{
			MessageBox messageBox = (MessageBox)Environment.getInstance().getStateManager().getMessageState();
			messageBox.getEventMessenger().unregister(this, MessageBox.ON_BUTTON_PRESSED);
			if (MessageParams.Button.YES.name().equals(bundle.getString("pressed")))
			{
				try
                {
	                Environment.getInstance().getStateManager().setStateMain(_featureStateTV, bundle);
                }
                catch (StateException e)
                {
                	Log.e(TAG, e.getMessage(), e);
                }
			}
		}
		else if (msgId == FeatureWatchlist.ON_PROGRAM_ADDED || msgId == FeatureWatchlist.ON_PROGRAM_REMOVED)
		{
			_watchlistGrid.notifyDataSetChanged();
		}
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
		return R.drawable.ic_menu_watchlist;
	}

	@Override
	public String getMenuItemCaption()
	{
		return Environment.getInstance().getResources().getString(R.string.menu_watchlist);
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
			Program program = (Program) object;

			if (Calendar.getInstance().compareTo(program.getStopTimeCalendar()) > 0)
			{
				// mark program expired
				view.setAlpha(0.5f);
			}
			else
			{
				// mark program active
				view.setAlpha(1.0f);
			}

			ImageView thumbView = (ImageView) view.findViewById(R.id.thumbnail);
			TextView titleView = (TextView) view.findViewById(R.id.title);
			// thumbView.setImageBitmap();
			titleView.setText(program.getTitle());
		}
	};
}
