/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureScheduler.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Defines the base class for scheduler feature type
 */

package com.aviq.tv.android.home.core;


/**
 * Defines the base class for scheduler feature type
 *
 */
public abstract class FeatureScheduler implements IFeature
{
	protected FeatureSet _dependencies = new FeatureSet();
	protected Environment _environment;

	public FeatureScheduler(Environment environment)
	{
		_environment = environment;
	}

	@Override
	public void initialize(OnFeatureInitialized onFeatureInitialized)
	{
	}

	@Override
	public FeatureSet dependencies()
	{
		return _dependencies;
	}

	@Override
	public Type getType()
	{
		return IFeature.Type.SCHEDULER;
	}

	@Override
    public String getName()
	{
		return getId().toString();
	}

	public abstract FeatureName.Scheduler getId();
}
