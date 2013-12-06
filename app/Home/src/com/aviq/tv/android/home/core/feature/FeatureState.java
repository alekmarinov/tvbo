/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureState.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Defines the base class for state feature type
 */

package com.aviq.tv.android.home.core.feature;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.state.BaseState;
import com.aviq.tv.android.home.utils.Prefs;

/**
 * Defines the base class for state feature type
 *
 */
public abstract class FeatureState extends BaseState implements IFeature
{
	protected FeatureSet _dependencies = new FeatureSet();

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
		return getStateName().toString();
	}

	@Override
    public Prefs getPrefs()
	{
		return Environment.getInstance().getFeaturePrefs(getStateName());
	}

    public abstract FeatureName.State getStateName();
}
