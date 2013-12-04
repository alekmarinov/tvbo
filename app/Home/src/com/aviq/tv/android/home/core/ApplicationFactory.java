/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    ApplicationFactory.java
 * Author:      alek
 * Date:        4 Dec 2013
 * Description: Singleton with factory method to create application by id
 */

package com.aviq.tv.android.home.core;

import com.aviq.tv.android.home.app.aviqtv.ApplicationAVIQTV;

/**
 * Singleton with factory method to create application by id
 */
public class ApplicationFactory
{
	private static ApplicationFactory _instance;

	private ApplicationFactory()
	{
	}

	public static synchronized ApplicationFactory getInstance()
	{
		if (_instance == null)
		{
			_instance = new ApplicationFactory();
		}
		return _instance;
	}

	public IApplication createApplication(IApplication.Name appName) throws ApplicationNotFoundException
	{
		switch (appName)
		{
			case AVIQTV:
				return new ApplicationAVIQTV();
		}
		throw new ApplicationNotFoundException(appName);
	}
}
