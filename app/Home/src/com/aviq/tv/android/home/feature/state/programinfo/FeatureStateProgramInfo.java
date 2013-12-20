/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureTV.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.home.feature.state.programinfo;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.feature.FeatureWatchlist;
import com.aviq.tv.android.home.feature.epg.EpgData;
import com.aviq.tv.android.home.feature.epg.FeatureEPG;
import com.aviq.tv.android.home.feature.epg.Program;
import com.aviq.tv.android.home.feature.player.FeaturePlayer;
import com.aviq.tv.android.home.feature.state.epg.EpgProgramInfo;
import com.aviq.tv.android.home.feature.state.epg.FeatureStateEPG;

/**
 * Program info state feature
 */
public class FeatureStateProgramInfo extends FeatureState
{
	public static final String TAG = FeatureStateEPG.class.getSimpleName();

	public static final String ARGS_CHANNEL_ID = "channelId";
	public static final String ARGS_PROGRAM_ID = "programId";

	private FeatureEPG _featureEPG;
	private FeaturePlayer _featurePlayer;
	private FeatureWatchlist _watchlist;
	private EpgProgramInfo _programInfo;

	public FeatureStateProgramInfo()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.Components.add(FeatureName.Component.WATCHLIST);
		_dependencies.Components.add(FeatureName.Component.PLAYER);
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
			_watchlist = (FeatureWatchlist) Environment.getInstance().getFeatureComponent(
			        FeatureName.Component.WATCHLIST);
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
		return FeatureName.State.PROGRAM_INFO;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");

		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_program_info, container, false);

		Bundle params = getArguments();
		final String channelId = params.getString(ARGS_CHANNEL_ID);
		final String programId = params.getString(ARGS_PROGRAM_ID);

		EpgData epgData = _featureEPG.getEpgData();
		final Program program = epgData.getProgram(channelId, programId);

		EpgProgramInfo programInfo = new EpgProgramInfo(getActivity(), viewGroup);
		_programInfo = new EpgProgramInfo(getActivity(), viewGroup);
		_programInfo.updateDetails(channelId, epgData.getProgram(channelId, programId));
		programInfo.updateDetails(channelId, program);
		Button watchlistBtn = (Button) viewGroup.findViewById(R.id.btn_watchlist);
		watchlistBtn.requestFocus();

		watchlistBtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Log.d(TAG, ".onClick: btn watchlist clicked on channel = " + channelId + ", programID " + programId);
				_watchlist.addWatchlist(program);
			}
		});

		return viewGroup;
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				Environment.getInstance().getStateManager().hideStateOverlay();
				return true;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				_programInfo.showPrevPage();
				return true;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				_programInfo.showNextPage();
				return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
