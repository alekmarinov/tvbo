/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureFactory.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: AVIQTV specific feature factory
 */

package com.aviq.tv.android.aviqtv;

import com.aviq.tv.android.aviqtv.state.FeatureStateLoading;
import com.aviq.tv.android.aviqtv.state.MessageBox;
import com.aviq.tv.android.aviqtv.state.channels.FeatureStateChannels;
import com.aviq.tv.android.aviqtv.state.epg.FeatureStateEPG;
import com.aviq.tv.android.aviqtv.state.menu.FeatureStateMenu;
import com.aviq.tv.android.aviqtv.state.programinfo.FeatureStateProgramInfo;
import com.aviq.tv.android.aviqtv.state.settings.FeatureStateSettings;
import com.aviq.tv.android.aviqtv.state.settings.ethernet.FeatureStateSettingsEthernet;
import com.aviq.tv.android.aviqtv.state.tv.FeatureStateTV;
import com.aviq.tv.android.aviqtv.state.watchlist.FeatureStateWatchlist;
import com.aviq.tv.android.sdk.core.feature.FeatureComponent;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureScheduler;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.feature.IFeatureFactory;
import com.aviq.tv.android.sdk.feature.channels.FeatureChannels;
import com.aviq.tv.android.sdk.feature.epg.rayv.FeatureEPGRayV;
import com.aviq.tv.android.sdk.feature.httpserver.FeatureHttpServer;
import com.aviq.tv.android.sdk.feature.player.rayv.FeaturePlayerRayV;
import com.aviq.tv.android.sdk.feature.register.FeatureRegister;
import com.aviq.tv.android.sdk.feature.scheduler.internet.FeatureInternet;
import com.aviq.tv.android.sdk.feature.watchlist.FeatureWatchlist;

/**
 * AVIQTV specific feature factory
 */
public class FeatureFactory implements IFeatureFactory
{
	@Override
    public FeatureComponent createComponent(FeatureName.Component featureId) throws FeatureNotFoundException
	{
		switch (featureId)
		{
			case EPG:
				// return new FeatureEPGWilmaa();
				return new FeatureEPGRayV();
			case CHANNELS:
				return new FeatureChannels();
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
    public FeatureScheduler createScheduler(FeatureName.Scheduler featureId) throws FeatureNotFoundException
	{
		switch (featureId)
		{
			case INTERNET:
				return new FeatureInternet();
		}
		throw new FeatureNotFoundException(featureId);
	}

	@Override
    public FeatureState createState(FeatureName.State featureId) throws FeatureNotFoundException
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
			case SETTINGS:
				return new FeatureStateSettings();
			case SETTINGS_ETHERNET:
				return new FeatureStateSettingsEthernet();
		}
		throw new FeatureNotFoundException(featureId);
	}
}
