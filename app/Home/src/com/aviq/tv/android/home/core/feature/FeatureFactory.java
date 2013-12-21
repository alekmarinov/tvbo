/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureFactory.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Singleton with factory methods to create features by id
 */

package com.aviq.tv.android.home.core.feature;

import com.aviq.tv.android.home.feature.FeatureHttpServer;
import com.aviq.tv.android.home.feature.FeatureWatchlist;
import com.aviq.tv.android.home.feature.epg.rayv.FeatureEPGRayV;
import com.aviq.tv.android.home.feature.player.rayv.FeaturePlayerRayV;
import com.aviq.tv.android.home.feature.register.FeatureRegister;
import com.aviq.tv.android.home.feature.scheduler.internet.FeatureInternet;
import com.aviq.tv.android.home.feature.state.MessageBox;
import com.aviq.tv.android.home.feature.state.channels.FeatureStateChannels;
import com.aviq.tv.android.home.feature.state.epg.FeatureStateEPG;
import com.aviq.tv.android.home.feature.state.menu.FeatureStateMenu;
import com.aviq.tv.android.home.feature.state.programinfo.FeatureStateProgramInfo;
import com.aviq.tv.android.home.feature.state.tv.FeatureStateTV;
import com.aviq.tv.android.home.feature.state.tv.FeatureStateTest;
import com.aviq.tv.android.home.feature.state.watchlist.FeatureStateWatchlist;

/**
 * Singleton with factory methods to create features by id
 *
 */
public class FeatureFactory
{
	private static FeatureFactory _instance;

	private FeatureFactory()
	{
	}

	public static synchronized FeatureFactory getInstance()
	{
		if (_instance == null)
		{
			_instance = new FeatureFactory();
		}
		return _instance;
	}

	public IFeature createComponent(FeatureName.Component featureId) throws FeatureNotFoundException
	{
		switch (featureId)
		{
			case EPG:
				// return new FeatureEPGWilmaa();
				return new FeatureEPGRayV();
			case PLAYER:
				return new FeaturePlayerRayV();
				// return new FeaturePlayer();
			case HTTP_SERVER:
				return new FeatureHttpServer();
			case REGISTER:
				return new FeatureRegister();
			case WATCHLIST:
				return new FeatureWatchlist();
			default:
				break;
		}
		throw new FeatureNotFoundException(featureId);
	}

	public IFeature createScheduler(FeatureName.Scheduler featureId) throws FeatureNotFoundException
	{
		switch (featureId)
		{
			case INTERNET:
				return new FeatureInternet();
		}
		throw new FeatureNotFoundException(featureId);
	}

	public IFeature createState(FeatureName.State featureId) throws FeatureNotFoundException
	{
		switch (featureId)
		{
			case MENU:
				return new FeatureStateMenu();
			case TV:
				return new FeatureStateTV();
			case EPG:
				return new FeatureStateEPG();
			case MESSAGE_BOX:
				return new MessageBox();
			case PROGRAM_INFO:
				return new FeatureStateProgramInfo();
			case WATCHLIST:
				return new FeatureStateWatchlist();
			case CHANNELS:
				return new FeatureStateChannels();
			case TEST:
				return new FeatureStateTest();
		}
		throw new FeatureNotFoundException(featureId);
	}
}
