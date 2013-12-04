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
import android.os.Bundle;
import android.util.Log;

import com.aviq.tv.android.home.core.ApplicationFactory;
import com.aviq.tv.android.home.core.ApplicationNotFoundException;
import com.aviq.tv.android.home.core.IApplication;

/**
 * The main activity managing all application screens
 */
public class MainActivity extends Activity
{
	public static final String TAG = MainActivity.class.getSimpleName();
	private IApplication _application;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(TAG, ".onCreate");
		try
        {
	        _application = ApplicationFactory.getInstance().createApplication(IApplication.Name.AVIQTV);
	        _application.onCreate(this);
        }
        catch (ApplicationNotFoundException e)
        {
        	Log.e(TAG, e.getMessage(), e);
        }
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.i(TAG, ".onDestroy");
		_application.onDestroy();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.i(TAG, ".onResume");
		_application.onResume();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.i(TAG, ".onPause");
		_application.onPause();
	}
}
