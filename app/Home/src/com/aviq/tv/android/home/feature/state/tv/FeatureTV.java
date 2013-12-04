/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureTV.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.home.feature.state.tv;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.FeatureName;
import com.aviq.tv.android.home.core.FeatureName.Component;
import com.aviq.tv.android.home.core.FeatureState;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.state.BaseState;
import com.aviq.tv.android.home.state.tv.StateTV;

/**
 * TV state feature
 *
 */
public class FeatureTV extends FeatureState
{
	public static final String TAG = FeatureTV.class.getSimpleName();
	private StateTV _stateTV;

	public FeatureTV(Environment environment)
	{
		super(environment);
		_dependencies.Components.add(Component.EPG);
		_dependencies.Components.add(Component.PLAYER);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		super.initialize(onFeatureInitialized);
		_stateTV = new StateTV(_environment);
		onFeatureInitialized.onInitialized(this, ResultCode.OK);
	}

	@Override
    public FeatureName.State getId()
    {
	    return FeatureName.State.TV;
    }

	@Override
    public BaseState getState()
    {
	    return _stateTV;
    }
}
