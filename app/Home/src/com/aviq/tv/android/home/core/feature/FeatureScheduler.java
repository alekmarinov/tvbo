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

import android.os.Bundle;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.event.EventMessenger;
import com.aviq.tv.android.home.utils.Prefs;



/**
 * Defines the base class for scheduler feature type
 *
 */
public abstract class FeatureScheduler implements IFeature
{
	protected FeatureSet _dependencies = new FeatureSet();
	protected EventMessenger _eventMessanger = new EventMessenger();

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

	/**
	 * @return an event messenger associated with this feature
	 */
	@Override
	public EventMessenger getEventMessanger()
	{
		return _eventMessanger;
	}

	@Override
    public void onEvent(int msgId, Bundle bundle)
	{
	}

	public abstract FeatureName.Scheduler getSchedulerName();
}
