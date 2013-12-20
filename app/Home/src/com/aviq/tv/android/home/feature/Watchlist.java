/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    Watchlist.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Component feature managing programs watchlist
 */

package com.aviq.tv.android.home.feature;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.feature.FeatureComponent;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureName.Component;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.feature.epg.EpgData;
import com.aviq.tv.android.home.feature.epg.FeatureEPG;
import com.aviq.tv.android.home.feature.epg.Program;

/**
 * Component feature managing programs watchlist
 */
public class Watchlist extends FeatureComponent
{
	public static final String TAG = Watchlist.class.getSimpleName();

	public enum Param
	{
		/**
		 * List of program identifier keys
		 */
		WATCHLIST
	}

	private ArrayList<Program> _watchedPrograms = new ArrayList<Program>();

	public Watchlist()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
	}

	@Override
	public void initialize(OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		try
		{
			FeatureEPG featureEPG = (FeatureEPG) Environment.getInstance().getFeatureComponent(FeatureName.Component.EPG);
			_watchedPrograms = loadWatchlist(featureEPG.getEpgData());
			super.initialize(onFeatureInitialized);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
			onFeatureInitialized.onInitialized(this, ResultCode.GENERAL_FAILURE);
		}
	}

	/**
	 * Add program to watchlist
	 *
	 * @param program
	 */
	public void addWatchlist(Program program)
	{
		if (!isWatched(program))
		{
			_watchedPrograms.add(program);
			saveWatchlist(_watchedPrograms);
		}
	}

	/**
	 * Remove program from the watchlist
	 *
	 * @param program
	 */
	public void removeWatchlist(Program program)
	{
		if (_watchedPrograms.remove(program))
			saveWatchlist(_watchedPrograms);
	}

	/**
	 * Check if program is added to the watchlist
	 *
	 * @param program
	 * @return true if the program is watched
	 */
	public boolean isWatched(Program program)
	{
		for (Program watchedProgram: _watchedPrograms)
		{
			if (program.equals(watchedProgram))
				return true;
		}
		return false;
	}

	/**
	 * @return list of programs whatchlist
	 */
	public ArrayList<Program> getWatchedPrograms()
	{
		return _watchedPrograms;
	}

	@Override
	public Component getComponentName()
	{
		return FeatureName.Component.WATCHLIST;
	}

	// Saves programs list to watchlist settings
	private void saveWatchlist(List<Program> programs)
	{
		StringBuffer buffer = new StringBuffer();
		for (Program program: programs)
		{
			if (buffer.length() > 0)
				buffer.append(',');
			buffer.append(program.getChannel().getChannelId());
			buffer.append('/');
			buffer.append(program.getId());
		}
		getPrefs().put(Param.WATCHLIST, buffer.toString());
	}

	// Loads programs list from watchlist settings
	private ArrayList<Program> loadWatchlist(EpgData epgData)
	{
		ArrayList<Program> programs = new ArrayList<Program>();
		String buffer = getPrefs().getString(Param.WATCHLIST);
		String[] programIds = buffer.split(",");
		for (String programId: programIds)
		{
			String[] idElements = programId.split("/");
			if (idElements.length != 2)
			{
				Log.w(TAG, "Invalid program ID saved in the watchlist: " + programId);
			}
			else
			{
				String chId = idElements[0];
				String prId = idElements[1];
				Program program = epgData.getProgram(chId, prId);
				if (program == null)
				{
					Log.w(TAG, "Program " + prId + " not found in channel " + chId);
					continue;
				}
				programs.add(program);
			}
		}
		return programs;
	}
}
