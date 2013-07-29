/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    TextUtils.java
 * Author:      Nadia
 * Date:        16 Jul 2013
 *
 * Description: Convenient text utilities
 */

package com.aviq.tv.android.home.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

public class TextUtils
{
	private static final String TAG = TextUtils.class.getSimpleName();

	/**
	 * Reads an InputStream and returns its contents as a String
	 *
	 * @param inputStream
	 *            The InputStream to read from.
	 * @return The contents of the InputStream as a String.
	 */
	public static String inputSteamToString(InputStream inputStream)
	{
		StringBuilder outputBuilder = new StringBuilder();
		BufferedReader reader = null;
		try
		{
			String string;
			if (inputStream != null)
			{
				reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				while (null != (string = reader.readLine()))
				{
					outputBuilder.append(string).append('\n');
				}
			}
		}
		catch (IOException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}

		return outputBuilder.toString().trim();
	}
}
