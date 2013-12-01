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

	public static FeatureFactory getInstance()
	{
		if (_instance == null)
		{
			_instance = new FeatureFactory();
		}
		return _instance;
	}

	public IFeature createComponent(FeatureName.Component featureName, Environment environment) throws FeatureNotFoundException
	{
		switch (featureName)
		{
			case EPG:
				return new FeatureEPG(environment);
			case PLAYER:
				return new FeaturePlayerRayV(environment);
			case HTTP_SERVER:
				return new FeatureHttpServer(environment);
		}
		throw new FeatureNotFoundException("Feature " + featureName + " is not found");
	}

	public IFeature createScheduler(FeatureName.Scheduler featureName, Environment environment) throws FeatureNotFoundException
	{
		switch (featureName)
		{
		}
		throw new FeatureNotFoundException("Feature " + featureName + " is not found");
	}

	public IFeature createState(FeatureName.State featureName, Environment environment) throws FeatureNotFoundException
	{
		switch (featureName)
		{
		}
		throw new FeatureNotFoundException("Feature " + featureName + " is not found");
	}
}
