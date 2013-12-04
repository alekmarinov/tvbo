/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureState.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Defines the base class for state feature type
 */

package com.aviq.tv.android.home.core;

import com.aviq.tv.android.home.state.BaseState;



/**
 * Defines the base class for state feature type
 *
 */
public abstract class FeatureState implements IFeature
{
	protected FeatureSet _dependencies = new FeatureSet();
	protected Environment _environment;

	public FeatureState(Environment environment)
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
		return IFeature.Type.STATE;
	}

	@Override
    public String getName()
	{
		return getId().toString();
	}

	public abstract BaseState getState();

	public abstract FeatureName.State getId();
}
