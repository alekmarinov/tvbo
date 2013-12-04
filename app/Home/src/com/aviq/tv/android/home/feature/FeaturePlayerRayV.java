/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeaturePlayerRayV.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Component feature providing RayV player
 */

package com.aviq.tv.android.home.feature;

import android.os.Bundle;
import android.util.Log;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.utils.Param;
import com.aviq.tv.android.home.utils.Prefs;
import com.aviq.tv.android.home.utils.TextUtils;
import com.rayv.StreamingAgent.Loader;

/**
 * Component feature providing RayV player
 */
public class FeaturePlayerRayV extends FeaturePlayer
{
	public static final String TAG = FeaturePlayerRayV.class.getSimpleName();

	/**
	 * @param environment
	 */
	public FeaturePlayerRayV(Environment environment)
	{
		super(environment);
	}

	@Override
	public void initialize(OnFeatureInitialized onFeatureInitialized)
	{
		super.initialize(onFeatureInitialized);

		// Start streaming agent
		Log.i(TAG, "Start streaming agent");
		final String streamerIni = TextUtils.inputSteamToString(_environment.getResources().openRawResource(
		        R.raw.streamer));

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				Loader.startStreamer(streamerIni);
			}
		}).start();

		onFeatureInitialized.onInitialized(this, ResultCode.OK);
	}

	public void play(String channelId)
	{
		Prefs prefs = _environment.getPrefs();
		Bundle bundle = new Bundle();
		bundle.putString("USER", prefs.getString(Param.User.RAYV_USER));
		bundle.putString("PASS", prefs.getString(Param.User.RAYV_PASS));
		bundle.putString("STREAM_ID", channelId);
		bundle.putInt("BITRATE", 1200);
		String url = prefs.getString(Param.System.RAYV_STREAM_URL_PATTERN, bundle);
		_player.play(url);
	}
}
