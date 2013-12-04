/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureFactory.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Singleton with factory methods to create features by id
 */

package com.aviq.tv.android.home.core;

import com.aviq.tv.android.home.feature.FeatureEPG;
import com.aviq.tv.android.home.feature.FeatureHttpServer;
import com.aviq.tv.android.home.feature.FeaturePlayerRayV;
import com.aviq.tv.android.home.feature.register.FeatureRegister;
import com.aviq.tv.android.home.feature.scheduler.internet.FeatureInternet;
import com.aviq.tv.android.home.feature.state.tv.FeatureTV;

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

	public IFeature createComponent(FeatureName.Component featureId, Environment environment) throws FeatureNotFoundException
	{
		switch (featureId)
		{
			case EPG:
				return new FeatureEPG(environment);
			case PLAYER:
				return new FeaturePlayerRayV(environment);
			case HTTP_SERVER:
				return new FeatureHttpServer(environment);
			case REGISTER:
				return new FeatureRegister(environment);
		}
		throw new FeatureNotFoundException(featureId);
	}

	public IFeature createScheduler(FeatureName.Scheduler featureId, Environment environment) throws FeatureNotFoundException
	{
		switch (featureId)
		{
			case INTERNET:
				return new FeatureInternet(environment);
		}
		throw new FeatureNotFoundException(featureId);
	}

	public IFeature createState(FeatureName.State featureId, Environment environment) throws FeatureNotFoundException
	{
		switch (featureId)
		{
			case TV:
				return new FeatureTV(environment);
		}
		throw new FeatureNotFoundException(featureId);
	}
}
