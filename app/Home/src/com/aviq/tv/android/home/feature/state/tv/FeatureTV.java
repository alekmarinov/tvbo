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
import com.aviq.tv.android.home.core.FeatureState;
import com.aviq.tv.android.home.core.ResultCode;

/**
 * TV state feature
 *
 */
public class FeatureTV extends FeatureState
{
	public static final String TAG = FeatureTV.class.getSimpleName();

	public FeatureTV(Environment environment)
	{
		super(environment);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		super.initialize(onFeatureInitialized);
		onFeatureInitialized.onInitialized(this, ResultCode.OK);
	}

	@Override
    public FeatureName.State getId()
    {
	    return FeatureName.State.TV;
    }
}
