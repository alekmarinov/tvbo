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
import android.view.ViewGroup;
import android.widget.Button;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.feature.epg.EpgData;
import com.aviq.tv.android.home.feature.epg.FeatureEPG;
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

	public FeatureStateProgramInfo()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
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
		String channelId = params.getString(ARGS_CHANNEL_ID);
		String programId = params.getString(ARGS_PROGRAM_ID);

		EpgData epgData = _featureEPG.getEpgData();

		EpgProgramInfo programInfo = new EpgProgramInfo(getActivity(), viewGroup);
		programInfo.updateDetails(channelId, epgData.getProgram(channelId, programId));
		Button optionsContainer = (Button)viewGroup.findViewById(R.id.btn_play);
		optionsContainer.requestFocus();

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
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Environment.getInstance().getStateManager().hideStateOverlay();
		}
		return super.onKeyDown(keyCode, event);
	}

}
