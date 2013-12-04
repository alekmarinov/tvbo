/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureNotFoundException.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Throw when requested feature is not defined
 */

package com.aviq.tv.android.home.core;

/**
 * Throw when requested feature is not defined
 *
 */
@SuppressWarnings("serial")
public class FeatureNotFoundException extends Exception
{
	public FeatureNotFoundException()
	{
	}

	/**
	 * @param detailMessage
	 */
	public FeatureNotFoundException(String detailMessage)
	{
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public FeatureNotFoundException(Throwable throwable)
	{
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public FeatureNotFoundException(String detailMessage, Throwable throwable)
	{
		super(detailMessage, throwable);
	}

}
