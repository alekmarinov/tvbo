/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureComponent.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Defines the base class for component feature type
 */

package com.aviq.tv.android.home.core.feature;

import android.os.Bundle;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.event.EventMessenger;
import com.aviq.tv.android.home.utils.Prefs;



/**
 * Defines the base class for component feature type
 */
public abstract class FeatureComponent implements IFeature
{
	protected FeatureSet _dependencies = new FeatureSet();

	public FeatureComponent()
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
		return IFeature.Type.COMPONENT;
	}

	@Override
    public String getName()
	{
		return getComponentName().toString();
	}

	@Override
    public Prefs getPrefs()
	{
		return Environment.getInstance().getFeaturePrefs(getComponentName());
	}

	/**
	 * @return an event messenger associated with this feature
	 */
	@Override
	public EventMessenger getEventMessenger()
	{
		return Environment.getInstance().getEventMessenger();
	}

	@Override
    public void onEvent(int msgId, Bundle bundle)
	{
	}

	public abstract FeatureName.Component getComponentName();
}
