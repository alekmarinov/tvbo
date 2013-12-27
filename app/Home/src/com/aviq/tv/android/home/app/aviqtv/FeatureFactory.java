/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureFactory.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: AVIQTV specific feature factory
 */

package com.aviq.tv.android.home.app.aviqtv;

import com.aviq.tv.android.home.app.aviqtv.state.channels.FeatureStateChannels;
import com.aviq.tv.android.home.app.aviqtv.state.epg.FeatureStateEPG;
import com.aviq.tv.android.home.app.aviqtv.state.menu.FeatureStateMenu;
import com.aviq.tv.android.home.app.aviqtv.state.programinfo.FeatureStateProgramInfo;
import com.aviq.tv.android.home.app.aviqtv.state.tv.FeatureStateTV;
import com.aviq.tv.android.home.app.aviqtv.state.watchlist.FeatureStateWatchlist;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.IFeature;
import com.aviq.tv.android.home.core.feature.IFeatureFactory;
import com.aviq.tv.android.home.feature.FeatureHttpServer;
import com.aviq.tv.android.home.feature.FeatureWatchlist;
import com.aviq.tv.android.home.feature.epg.rayv.FeatureEPGRayV;
import com.aviq.tv.android.home.feature.player.rayv.FeaturePlayerRayV;
import com.aviq.tv.android.home.feature.register.FeatureRegister;
import com.aviq.tv.android.home.feature.scheduler.internet.FeatureInternet;
import com.aviq.tv.android.home.feature.state.FeatureStateLoading;
import com.aviq.tv.android.home.feature.state.MessageBox;

/**
 * AVIQTV specific feature factory
 */
public class FeatureFactory implements IFeatureFactory
{
	@Override
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

	@Override
    public IFeature createScheduler(FeatureName.Scheduler featureId) throws FeatureNotFoundException
	{
		switch (featureId)
		{
			case INTERNET:
				return new FeatureInternet();
		}
		throw new FeatureNotFoundException(featureId);
	}

	@Override
    public IFeature createState(FeatureName.State featureId) throws FeatureNotFoundException
	{
		switch (featureId)
		{
			case MENU:
				return new FeatureStateMenu();
			case LOADING:
				return new FeatureStateLoading();
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
		}
		throw new FeatureNotFoundException(featureId);
	}
}
