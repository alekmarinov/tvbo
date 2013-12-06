/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureScheduler.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Defines the base class for scheduler feature type
 */

package com.aviq.tv.android.home.core.feature;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.utils.Prefs;



/**
 * Defines the base class for scheduler feature type
 *
 */
public abstract class FeatureScheduler implements IFeature
{
	protected FeatureSet _dependencies = new FeatureSet();

	public FeatureScheduler()
	{
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
		return getSchedulerName().toString();
	}

	@Override
    public Prefs getPrefs()
	{
		return Environment.getInstance().getFeaturePrefs(getSchedulerName());
	}

	public abstract FeatureName.Scheduler getSchedulerName();
}
