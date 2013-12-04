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
import android.view.KeyEvent;

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

	/**
	 * Invoked on key pressed
	 */
	boolean onKeyDown(int keyCode, KeyEvent event);

	/**
	 * Invoked on key released
	 */
	boolean onKeyUp(int keyCode, KeyEvent event);
}
