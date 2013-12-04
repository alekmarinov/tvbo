/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    IApplication.java
 * Author:      alek
 * Date:        4 Dec 2013
 * Description: Defines main application interface
 */

package com.aviq.tv.android.home.core;

import android.app.Activity;

/**
 * Defines main application interface
 */
public interface IApplication
{
	public static enum Name
	{
		AVIQTV
	}

	/**
	 * Invoked on activity create
	 */
	void onCreate(Activity activity);

	/**
	 * Invoked on activity destroy
	 */
	void onDestroy();

	/**
	 * Invoked on activity resume
	 */
	void onResume();

	/**
	 * Invoked on activity pause
	 */
	void onPause();
}
