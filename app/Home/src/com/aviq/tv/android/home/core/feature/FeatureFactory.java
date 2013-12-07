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
import com.aviq.tv.android.home.feature.epg.FeatureEPG;
import com.aviq.tv.android.home.feature.player.rayv.FeaturePlayerRayV;
import com.aviq.tv.android.home.feature.register.FeatureRegister;
import com.aviq.tv.android.home.feature.scheduler.internet.FeatureInternet;
import com.aviq.tv.android.home.feature.state.MessageBox;
import com.aviq.tv.android.home.feature.state.tv.FeatureStateTV;

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
				return new FeatureEPG();
			case PLAYER:
				return new FeaturePlayerRayV();
			case HTTP_SERVER:
				return new FeatureHttpServer();
			case REGISTER:
				return new FeatureRegister();
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
			case TV:
				return new FeatureStateTV();
			case MESSAGE_BOX:
				return new MessageBox();
		}
		throw new FeatureNotFoundException(featureId);
	}
}