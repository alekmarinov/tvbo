/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    ApplicationNotFoundException.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Throw when requested application is not defined
 */

package com.aviq.tv.android.home.core;

/**
 * Throw when requested application is not defined
 *
 */
@SuppressWarnings("serial")
public class ApplicationNotFoundException extends Exception
{
	public ApplicationNotFoundException()
	{
	}

	/**
	 * @param IApplication.Name
	 */
	public ApplicationNotFoundException(IApplication.Name appName)
	{
		super("Application " + appName + " is not found");
	}

	/**
	 * @param detailMessage
	 */
	public ApplicationNotFoundException(String detailMessage)
	{
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public ApplicationNotFoundException(Throwable throwable)
	{
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public ApplicationNotFoundException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}
}
