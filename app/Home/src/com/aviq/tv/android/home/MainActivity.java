/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    MainActivity.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: The main activity managing all application screens
 */

package com.aviq.tv.android.home;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

import com.aviq.tv.android.home.utils.Param;
import com.aviq.tv.android.home.utils.Prefs;

/**
 * The main activity managing all application screens
 *
 */
public class MainActivity extends Activity
{
	public static final String TAG = MainActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(TAG, ".onCreate");
		setContentView(R.layout.home_layout);

		Prefs prefs = getApp().getPrefs();
		Bundle bundle = new Bundle();

		bundle.putString("USER", "1C6F65F9DE76");
		bundle.putString("PASS", "1C6F65F9DE76");
		bundle.putString("STREAM_ID", "vtx_sf1");
		bundle.putInt("BITRATE", 1200);

		String url = prefs.getString(Param.System.RAYV_STREAM_URL_PATTERN, bundle);
		VideoView videoView = (VideoView)findViewById(R.id.player);
		videoView.setVideoURI(Uri.parse(url));
		videoView.start();
		Log.i(TAG, "play " + url);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.i(TAG, ".onDestroy");
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.i(TAG, ".onResume");
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.i(TAG, ".onPause");
	}

	/**
	 * @return the main application owning this activity
	 */
	public MainApplication getApp()
	{
		return (MainApplication)getApplication();
	}
}
