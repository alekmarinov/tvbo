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

import com.aviq.tv.android.home.core.Environment;
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

	public Prefs getPrefs()
	{
		return Environment.getInstance().getFeaturePrefs(getComponentName());
	}

	public abstract FeatureName.Component getComponentName();
}
