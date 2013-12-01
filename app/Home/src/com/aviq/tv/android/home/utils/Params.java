/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    Params.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: Define the default parameter values
 */
package com.aviq.tv.android.home.utils;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Params
{
	// Parameters dependent by application user. Read/Write.
	private static class User
	{
		static final Map<Param.User, Boolean> bools = new HashMap<Param.User, Boolean>();
		static final Map<Param.User, Integer> ints = new HashMap<Param.User, Integer>();
		static final Map<Param.User, String> strings = new HashMap<Param.User, String>();
	}

	// Factory set parameters. Read Only.
	private static class System
	{
		static final Map<Param.System, Boolean> bools = new HashMap<Param.System, Boolean>();
		static final Map<Param.System, Integer> ints = new HashMap<Param.System, Integer>();
		static final Map<Param.System, String> strings = new HashMap<Param.System, String>();
	}

	// Default values
	static
	{
		// user settings
		User.strings.put(Param.User.BRAND, "@BRAND@");
		User.strings.put(Param.User.MAC, "000000000000");
		User.bools.put(Param.User.IS_FIRST_TIME, true);
		User.bools.put(Param.User.IS_AFTER_UPDATE, false);
		User.strings.put(Param.User.ABMP_URL, "http://bg.aviq.com:9090");
		User.strings.put(Param.User.CURRENT_CHANNEL, "");
		User.strings.put(Param.User.LANGUAGE, "de");
		User.strings.put(Param.User.RAYV_USER, "1C6F65F9DE76");
		User.strings.put(Param.User.RAYV_PASS, "1C6F65F9DE76");

		// system settings
		System.bools.put(Param.System.IS_STUB, Boolean.parseBoolean("@IS_STUB@"));
		System.bools.put(Param.System.IS_DEVEL, false);
		System.ints.put(Param.System.ABMP_REGISTER_INTERVAL, 30 * 60); // seconds
		System.ints.put(Param.System.INTERNET_CHECK_INTERVAL, 1 * 20); // seconds
		System.ints.put(Param.System.ALERT_TIMEOUT, 5); // seconds
		System.ints.put(Param.System.OSD_CHANNELS_TIMEOUT, 2); // seconds
		System.ints.put(Param.System.OSD_VOLUME_BAR_TIMEOUT, 3); // seconds
		System.ints.put(Param.System.DISPLAY_MESSAGE_TIMEOUT, 3); // seconds
		System.ints.put(Param.System.EPG_DAYS_PAST, 7); // days
		System.ints.put(Param.System.EPG_DAYS_AHEAD, 7); // days
		System.ints.put(Param.System.EPG_UPDATE_INTERVAL, 60 * 60 * 12); // seconds
		System.strings.put(Param.System.TIMEZONE, "Europe/Zurich");
		System.strings.put(Param.System.PROXY_TYPE, Proxy.Type.HTTP.name());
		System.strings.put(Param.System.PROXY_HOST, "services.aviq.com");
		System.bools.put(Param.System.IS_PROXY_ENABLE, false);
		System.ints.put(Param.System.PROXY_PORT, 59334);
		System.ints.put(Param.System.HTTP_CONNECT_TIMEOUT, 3); // seconds
		System.ints.put(Param.System.HTTP_READ_TIMEOUT, 50); // seconds
		System.bools.put(Param.System.IS_PLAY_VIDEO, true);
		System.bools.put(Param.System.IS_PLAY_SOUND, false);
		System.strings.put(Param.System.ACRA_WEBDAV_USERNAME, "wilmaaSB510");
		System.strings.put(Param.System.ACRA_WEBDAV_PASSWORD, "Ng^6X49U73]w<tq&x");
		System.strings.put(Param.System.ACRA_WEBDAV_REPORT_TEMPLATE, "%s-%s-%s-%s-%s-%d");
		System.ints.put(Param.System.UPGRADE_CHECK_INTERVAL, 60 * 30); // seconds
		System.ints.put(Param.System.AUTO_STANDBY_INTERVAL, 60 * 60 * 3); // seconds
		System.ints.put(Param.System.PLAYER_MAX_RETRY_ATTEMPS, 1);
		System.strings.put(Param.System.ACRA_WEBDAV_URL, "http://devices.aviq.com:50556/crashlogs/AVIQTV-Production/");
		System.ints.put(Param.System.LOG_LEVEL, Log.INFO);
		System.strings.put(Param.System.RAYV_STREAM_URL_PATTERN,
		        "http://localhost:1234/RayVAgent/v1/RAYV/${USER}:${PASS}@${STREAM_ID}?streams=${STREAM_ID}:${BITRATE}");
		System.strings.put(Param.System.EPG_VERSION, "1");
		System.strings.put(Param.System.EPG_SERVER, "http://epg.aviq.bg");
		System.strings.put(Param.System.EPG_PROVIDER, "rayv");
		System.strings.put(Param.System.EPG_CHANNELS_URL, "${SERVER}/v${VERSION}/channels/${PROVIDER}");
		System.strings.put(Param.System.EPG_CHANNEL_LOGO_URL, "${SERVER}/static/${PROVIDER}/${CHANNEL}/${LOGO}");

		/*
		 * emulate pre-processor during build time
		 */

		// @RELEASE_BEGIN
		// RELEASE_END@

		// @DEBUG_BEGIN
		System.strings.put(Param.System.ACRA_WEBDAV_URL, "http://devices.aviq.com:50556/crashlogs/AVIQTV-Development/");
		System.bools.put(Param.System.IS_DEVEL, true);
		System.ints.put(Param.System.LOG_LEVEL, Log.VERBOSE);
		// DEBUG_END@

		// This are default values for developer purpose in Eclipse
		// They will be always removed by automatic build

		// @TESTCODE_BEGIN
		User.strings.put(Param.User.BRAND, "aviqtv");
		System.bools.put(Param.System.IS_STUB, false);
		User.bools.put(Param.User.IS_FIRST_TIME, false);
		System.ints.put(Param.System.EPG_DAYS_PAST, 1); // days
		System.ints.put(Param.System.EPG_DAYS_AHEAD, 1); // days
		// TESTCODE_END@
	}

	synchronized public static String getString(Param.User param)
	{
		return User.strings.get(param);
	}

	synchronized public static String getString(Param.System param)
	{
		return System.strings.get(param);
	}

	synchronized public static int getInt(Param.User param)
	{
		return User.ints.get(param);
	}

	synchronized public static int getInt(Param.System param)
	{
		return System.ints.get(param);
	}

	synchronized public static boolean getBool(Param.User param)
	{
		return User.bools.get(param);
	}

	synchronized public static boolean getBool(Param.System param)
	{
		return System.bools.get(param);
	}
}
