/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureHttpServer.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Component feature providing http server
 */

package com.aviq.tv.android.home.feature;

import java.io.IOException;

import android.util.Log;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.feature.FeatureComponent;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureName.Component;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.utils.HttpServer;

/**
 * Component feature providing http server
 *
 */
public class FeatureHttpServer extends FeatureComponent
{
	public static final String TAG = FeatureHttpServer.class.getSimpleName();

	@Override
	public void initialize(OnFeatureInitialized onFeatureInitialized)
	{
		super.initialize(onFeatureInitialized);

		// Start HTTP server
		Log.i(TAG, "Start HTTP server");
		HttpServer httpServer = new HttpServer(Environment.getInstance().getContext());
		try
		{
			httpServer.create();
		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		onFeatureInitialized.onInitialized(this, ResultCode.OK);
	}

	@Override
	public Component getComponentName()
	{
		return FeatureName.Component.HTTP_SERVER;
	}
}
