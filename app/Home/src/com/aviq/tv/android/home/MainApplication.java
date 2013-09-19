/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    MainApplication.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: Main application definition. Init stateless components here.
 */

package com.aviq.tv.android.home;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.aviq.tv.android.home.utils.HttpServer;
import com.aviq.tv.android.home.utils.Prefs;
import com.aviq.tv.android.home.utils.TextUtils;
import com.rayv.StreamingAgent.Loader;

/**
 * Main application definition. Init stateless components here.
 */
public class MainApplication extends Application
{
	public static final String TAG = MainApplication.class.getSimpleName();
	private HttpServer _httpServer;
	private Prefs _prefs;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		try
		{
			Log.i(TAG, ".onCreate: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
			
			// Start streaming agent
			Log.i(TAG, "Start streaming agent");
			final String streamerIni = TextUtils.inputSteamToString(getResources().openRawResource(R.raw.streamer));
			
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					Loader.startStreamer(streamerIni);
				}
			}).start();
			
			// Start HTTP server
			Log.i(TAG, "Start HTTP server");
			HttpServer httpServer = new HttpServer(this);
			httpServer.create();
			
			// Initialize preferences
			_prefs = new Prefs(getSharedPreferences("user", Activity.MODE_PRIVATE), getSharedPreferences("system",
			        Activity.MODE_PRIVATE));
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}
	
	/**
	 * Returns global initialized HttpServer instance
	 * 
	 * @return HttpServer
	 */
	public HttpServer getHttpServer()
	{
		return _httpServer;
	}
	
	/**
	 * Returns global preferences manager
	 * 
	 * @return Prefs
	 */
	public Prefs getPrefs()
	{
		return _prefs;
	}
}
