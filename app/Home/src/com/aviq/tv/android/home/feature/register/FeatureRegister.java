/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureRegister.java
 * Author:      alek
 * Date:        3 Dec 2013
 * Description: Feature registering box to ABMP
 */

package com.aviq.tv.android.home.feature.register;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.FeatureComponent;
import com.aviq.tv.android.home.core.FeatureName;
import com.aviq.tv.android.home.core.FeatureNotFoundException;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.feature.scheduler.internet.FeatureInternet;
import com.aviq.tv.android.home.utils.Log;
import com.aviq.tv.android.home.utils.Param;
import com.aviq.tv.android.home.utils.Prefs;
import com.aviq.tv.android.home.utils.TextUtils;

/**
 * Feature registering box to ABMP
 */
public class FeatureRegister extends FeatureComponent
{
	private static final String TAG = FeatureRegister.class.getSimpleName();
	private static final String MAC_ADDRESS_FILE = "/sys/class/net/eth0/address";
	private String _boxId;
	private String _version;

	/**
	 * @param environment
	 */
	public FeatureRegister(Environment environment)
	{
		super(environment);
		_dependencies.Schedulers.add(FeatureName.Scheduler.INTERNET);
	}

	@Override
	public void initialize(OnFeatureInitialized onFeatureInitialized)
	{
		try
		{
			_boxId = readMacAddress();
			_version = parseAppVersion();

			Prefs prefs = _environment.getPrefs();
			Bundle bundle = new Bundle();
			bundle.putString("SERVER", prefs.getString(Param.User.ABMP_SERVER));
			bundle.putString("BOX_ID", _boxId);
			bundle.putString("VERSION", _version);
			bundle.putString("BRAND", prefs.getString(Param.User.BRAND));
			bundle.putString("NETWORK", getActiveNetworkType());

			String abmpRegUrl = prefs.getString(Param.System.ABMP_REGISTER_URL, bundle);
			int registerInterval = prefs.getInt(Param.System.ABMP_REGISTER_INTERVAL);

			FeatureInternet featureInternet = (FeatureInternet) _environment
			        .getFeatureScheduler(FeatureName.Scheduler.INTERNET);
			featureInternet.addCheckUrl(abmpRegUrl, registerInterval, new FeatureInternet.OnResultReceived()
			{
				@Override
				public void onReceiveResult(int resultCode, Bundle resultData)
				{
					Log.i(TAG,
					        ".onReceiveResult: resultCode = " + resultCode + ", url = " + resultData.getString("URL"));
					if (resultCode != ResultCode.OK)
					{
						// TODO: Take decision what to do if the box is disabled
						// or no access to ABMP services
					}
				}
			});
			onFeatureInitialized.onInitialized(this, ResultCode.OK);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
			onFeatureInitialized.onInitialized(this, ResultCode.GENERAL_FAILURE);
		}
		catch (FileNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
			onFeatureInitialized.onInitialized(this, ResultCode.GENERAL_FAILURE);
		}
		catch (NameNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
			onFeatureInitialized.onInitialized(this, ResultCode.GENERAL_FAILURE);
		}
	}

	@Override
	public FeatureName.Component getId()
	{
		return FeatureName.Component.REGISTER;
	}

	public String getBoxId()
	{
		return _boxId;
	}

	private String readMacAddress() throws FileNotFoundException
	{
		FileInputStream fis = new FileInputStream(MAC_ADDRESS_FILE);
		String macAddress = TextUtils.inputSteamToString(fis);
		macAddress = macAddress.substring(0, 17);
		return macAddress.replace(":", "").toUpperCase();
	}

	private String getActiveNetworkType()
	{
		final ConnectivityManager connectivityManager = (ConnectivityManager) _environment.getContext()
		        .getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
		return (netInfo != null) ? netInfo.getTypeName().toLowerCase() : "";
	}

	private String parseAppVersion() throws NameNotFoundException
	{
		String version = _environment.getContext().getPackageManager()
		        .getPackageInfo(_environment.getContext().getPackageName(), 0).versionName;
		int dotIdx = version.lastIndexOf('.');
		if (dotIdx >= 0)
		{
			version = version.substring(dotIdx + 1);
		}
		return version;
	}
}
