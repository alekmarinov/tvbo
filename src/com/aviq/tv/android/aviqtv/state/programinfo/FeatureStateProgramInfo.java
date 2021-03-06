/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureTV.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.aviqtv.state.programinfo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.aviqtv.state.ContextButton;
import com.aviq.tv.android.aviqtv.state.ContextButtonGroup;
import com.aviq.tv.android.aviqtv.state.epg.EpgProgramInfo;
import com.aviq.tv.android.aviqtv.state.tv.FeatureStateTV;
import com.aviq.tv.android.sdk.core.AVKeyEvent;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.feature.epg.FeatureEPGCompat;
import com.aviq.tv.android.sdk.feature.epg.IEpgDataProvider;
import com.aviq.tv.android.sdk.feature.epg.Program;
import com.aviq.tv.android.sdk.feature.player.FeaturePlayer;
import com.aviq.tv.android.sdk.feature.watchlist.FeatureWatchlist;

/**
 * Program info state feature
 */
public class FeatureStateProgramInfo extends FeatureState
{
	public static final String TAG = FeatureStateProgramInfo.class.getSimpleName();

	public static final String ARGS_CHANNEL_ID = "channelId";
	public static final String ARGS_PROGRAM_ID = "programId";

	private FeatureEPGCompat _featureEPG;
	private FeaturePlayer _featurePlayer;
	private FeatureStateTV _featureStateTV;
	private FeatureWatchlist _watchlist;
	private EpgProgramInfo _programInfo;
	private ContextButtonGroup _contextButtonGroup;
	private IEpgDataProvider _epgData;
	private String _channelId;
	private String _programId;

	public FeatureStateProgramInfo() throws FeatureNotFoundException
	{
		require(FeatureName.Scheduler.EPG);
		require(FeatureName.Component.PLAYER);
		require(FeatureName.State.TV);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		_featureEPG = (FeatureEPGCompat) Environment.getInstance().getFeatureScheduler(FeatureName.Scheduler.EPG);
		_featurePlayer = (FeaturePlayer) Environment.getInstance().getFeatureComponent(FeatureName.Component.PLAYER);

		if (Environment.getInstance().getFeatureManager().hasFeature(FeatureName.Component.WATCHLIST))
		{
			_watchlist = (FeatureWatchlist) Environment.getInstance().getFeatureComponent(
			        FeatureName.Component.WATCHLIST);
		}

		_featureStateTV = (FeatureStateTV) Environment.getInstance().getFeatureState(FeatureName.State.TV);
		super.initialize(onFeatureInitialized);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.PROGRAM_INFO;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");

		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_program_info, container, false);

		_epgData = _featureEPG.getEpgData();

		// Extract fragment parameters

		Bundle params = getArguments();
		_channelId = params.getString(ARGS_CHANNEL_ID);
		_programId = params.getString(ARGS_PROGRAM_ID);

		Program program = _epgData.getProgramById(_channelId, _programId);

		// Show detailed program info
		_programInfo = new EpgProgramInfo(getActivity(), viewGroup);
		_programInfo.updateDetails(_channelId, program);

		// Create program options group of buttons
		_contextButtonGroup = (ContextButtonGroup) viewGroup.findViewById(R.id.program_options_list);
		_contextButtonGroup.setButtonOnClickListener(_contextButtonGroupOnClickListener);

		// Context button "Play"
		_contextButtonGroup.createButton(R.drawable.ic_option_btn_play, R.string.play);

		if (_watchlist != null && !_watchlist.isExpired(program))
		{
			// Context button "Add to watchlist" / "Remove from watchlist"
			if (_watchlist.isWatched(program))
				_contextButtonGroup.createButton(R.drawable.ic_option_btn_favorite, R.string.removeFromWatchlist);
			else
				_contextButtonGroup.createButton(R.drawable.ic_option_btn_favorite, R.string.addToWatchlist);
		}

		// Context button "Like" / "Unlike"
		// _contextButtonGroup.createButton(R.drawable.ic_option_btn_like,
		// R.string.like);

		return viewGroup;
	}

	@Override
	public boolean onKeyDown(AVKeyEvent event)
	{
		switch (event.Code)
		{
			case BACK:
				Environment.getInstance().getStateManager().hideStateOverlay();
				return true;

			case LEFT:
				_programInfo.showPrevPage();
				return true;

			case RIGHT:
				_programInfo.showNextPage();
				return true;

			case UP:
			case DOWN:
				return _contextButtonGroup.onKeyDown(event.Event.getKeyCode(), event.Event);
		}
		return super.onKeyDown(event);
	}

	private OnClickListener _contextButtonGroupOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			switch (view.getId())
			{
				case R.string.play:
				{
					Program program = _epgData.getProgramById(_channelId, _programId);
					Bundle bundle = new Bundle();
					bundle.putString("PROGRAM", program.getId());
					bundle.putInt(FeatureStateTV.Extras.CHANNEL_INDEX.name(), program.getChannel().getIndex());
					bundle.putLong(FeatureStateTV.Extras.PLAY_TIME.name(),
					        program.getStartTime().getTimeInMillis() / 1000);
					bundle.putLong(FeatureStateTV.Extras.PLAY_DURATION.name(), program.getLengthMillis() / 1000);
					try
					{
						Environment.getInstance().getStateManager().setStateMain(_featureStateTV, bundle);
					}
					catch (StateException e)
					{
						Log.e(TAG, e.getMessage(), e);
					}
				}
				break;

				case R.string.addToWatchlist:
				{
					Program program = _epgData.getProgramById(_channelId, _programId);

					Log.d(TAG, ".onClick: btn watchlist clicked on channel = " + _channelId + ", programID "
					        + _programId);
					_watchlist.addWatchlist(program);

					ContextButton button = (ContextButton) view;
					button.setContent(R.drawable.ic_option_btn_favorite, R.string.removeFromWatchlist);
				}
				break;

				case R.string.removeFromWatchlist:
				{
					Program program = _epgData.getProgramById(_channelId, _programId);

					Log.d(TAG, ".onClick: btn watchlist clicked on channel = " + _channelId + ", programID "
					        + _programId);
					_watchlist.removeWatchlist(program);

					ContextButton button = (ContextButton) view;
					button.setContent(R.drawable.ic_option_btn_favorite, R.string.addToWatchlist);
				}
				break;

				case R.string.like:
					Log.e(TAG, "-----: LIKE");
				break;

				default:
					Log.w(TAG, "Unknown context button clicked. No action taken.");
				break;
			}
		}
	};
}
