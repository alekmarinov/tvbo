/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    Prefs.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: Provides access to user and system preferences
 */

package com.aviq.tv.android.home.utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

public class Prefs
{
	private static final String TAG = Prefs.class.getSimpleName();
	private final SharedPreferences _userPrefs;
	private final SharedPreferences _systemPrefs;

	public Prefs(SharedPreferences userPrefs, SharedPreferences systemPrefs)
	{
		_userPrefs = userPrefs;
		_systemPrefs = systemPrefs;
	}

	/**
	 * Returns string param from User settings
	 *
	 * @param key parameter defined in Param.User enum
	 * @return String param from User settings
	 */
	public String getString(Param.User key)
	{
		String result = _userPrefs.getString(key.toString(), Params.getString(key));
		if (result != null)
			put(key, result);
		return result;
	}

	/**
	 * Returns string param from System settings
	 *
	 * @param key parameter defined in Param.System enum
	 * @return String param from System settings
	 */
	public String getString(Param.System key)
	{
		String result = _systemPrefs.getString(key.toString(), Params.getString(key));
		if (result != null)
			put(key, result);
		return result;
	}

	/**
	 * Returns string param from User settings with applied substitutions provided by the <i>bundle</i>.
	 *
	 * @param key parameter defined in Param.User enum
	 * @return String param from User settings with applied substitutions
	 */
	public String getString(Param.User key, Bundle bundle)
	{
		return Strings.substitude(getString(key), bundle);
	}

	/**
	 * Returns string param from System settings with applied substitutions provided by the <i>bundle</i>.
	 *
	 * @param key parameter defined in Param.System enum
	 * @return String param from System settings with applied substitutions
	 */
	public String getString(Param.System key, Bundle bundle)
	{
		return Strings.substitude(getString(key), bundle);
	}

	/**
	 * Returns int param from User settings
	 *
	 * @param key parameter defined in Param.User enum
	 * @return Int param from User settings
	 */
	public int getInt(Param.User key)
	{
		int defaultValue = Params.getInt(key);
		int result = _userPrefs.getInt(key.toString(), defaultValue);
		put(key, result);
		return result;
	}

	/**
	 * Returns int param from System settings
	 *
	 * @param key parameter defined in Param.System enum
	 * @return Int param from System settings
	 */
	public int getInt(Param.System key)
	{
		int defaultValue = Params.getInt(key);
		int result = _systemPrefs.getInt(key.toString(), defaultValue);
		put(key, result);
		return result;
	}

	/**
	 * Returns boolean param from User settings
	 *
	 * @param key parameter defined in Param.User enum
	 * @return Boolean param from User settings
	 */
	public boolean getBool(Param.User key)
	{
		boolean defaultValue = Params.getBool(key);
		boolean result = _userPrefs.getBoolean(key.toString(), defaultValue);
		put(key, result);
		return result;
	}

	/**
	 * Returns boolean param from System settings
	 *
	 * @param key parameter defined in Param.System enum
	 * @return Boolean param from System settings
	 */
	public boolean getBool(Param.System key)
	{
		boolean defaultValue = Params.getBool(key);
		boolean result = _systemPrefs.getBoolean(key.toString(), defaultValue);
		put(key, result);
		return result;
	}

	/**
	 * Sets string parameter in User settings
	 *
	 * @param key parameter defined in Param.User enum
	 * @param value
	 */
	public void put(Param.User key, String value)
	{
		Log.d(TAG, "Set " + key + " = " + value);
		Editor edit = _userPrefs.edit();
		edit.putString(key.toString(), value);
		edit.commit();
	}

	/**
	 * Sets string parameter in System settings
	 *
	 * @param key parameter defined in Param.System enum
	 * @param value
	 */
	private void put(Param.System key, String value)
	{
		Log.d(TAG, "Set " + key + " = " + value);
		Editor edit = _systemPrefs.edit();
		edit.putString(key.toString(), value);
		edit.commit();
	}

	/**
	 * Sets int parameter in User settings
	 *
	 * @param key parameter defined in Param.User enum
	 * @param value
	 */
	public void put(Param.User key, int value)
	{
		Log.d(TAG, "Set " + key + " = " + value);
		Editor edit = _userPrefs.edit();
		edit.putInt(key.toString(), value);
		edit.commit();
	}

	/**
	 * Sets int parameter in System settings
	 *
	 * @param key parameter defined in Param.System enum
	 * @param value
	 */
	private void put(Param.System key, int value)
	{
		Log.d(TAG, "Set " + key + " = " + value);
		Editor edit = _systemPrefs.edit();
		edit.putInt(key.toString(), value);
		edit.commit();
	}

	/**
	 * Sets boolean parameter in User settings
	 *
	 * @param key parameter defined in Param.User enum
	 * @param value
	 */
	public void put(Param.User key, boolean value)
	{
		Log.d(TAG, "Set " + key + " = " + value);
		Editor edit = _userPrefs.edit();
		edit.putBoolean(key.toString(), value);
		edit.commit();
	}

	/**
	 * Sets boolean parameter in System settings
	 *
	 * @param key parameter defined in Param.System enum
	 * @param value
	 */
	private void put(Param.System key, boolean value)
	{
		Log.d(TAG, "Set " + key + " = " + value);
		Editor edit = _systemPrefs.edit();
		edit.putBoolean(key.toString(), value);
		edit.commit();
	}

	/**
	 * Clean all system parameters to defaults
	 */
	public void clear()
	{
		Editor edit = _systemPrefs.edit();
		edit.clear();
		edit.commit();
	}
}
