/**
 * Copyright (c) 2007-2014, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureStateNetworkWizard.java
 * Author:      zhelyazko
 * Date:        13 Feb 2014
 * Description: Network Wizard state implementation
 */

package com.aviq.tv.android.aviqtv.state.wizard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.EventMessenger;
import com.aviq.tv.android.sdk.core.EventReceiver;
import com.aviq.tv.android.sdk.core.Log;
import com.aviq.tv.android.sdk.core.ResultCode;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.service.ServiceController.OnResultReceived;
import com.aviq.tv.android.sdk.core.state.StateException;

/**
 * Network Wizard state implementation
 */
public class FeatureStateNetworkWizard extends FeatureState
{
	private static final String TAG = FeatureStateNetworkWizard.class.getSimpleName();

	public static final int ON_INTERNET_CONNECTIVITY_ESTABLISHED = EventMessenger
	        .ID("ON_INTERNET_CONNECTIVITY_ESTABLISHED");
	public static final int ON_INTERNET_CONNECTIVITY_ERROR = EventMessenger.ID("ON_INTERNET_CONNECTIVITY_ERROR");

	public enum Param
	{
		/**
		 * The timeout in ms to connect to internet before displaying connecting
		 * notification
		 */
		DISPLAY_CONNECTING_TIMEOUT(10000),

		INTERNET_CHECK_URL("http://www.google.com");

		Param(int value)
		{
			Environment.getInstance().getFeaturePrefs(FeatureName.State.NETWORK_WIZARD).put(name(), value);
		}

		Param(String value)
		{
			Environment.getInstance().getFeaturePrefs(FeatureName.State.NETWORK_WIZARD).put(name(), value);
		}
	}

	private FeatureStateNetworkError _featureStateNetworkError;
	private OnFeatureInitialized _onFeatureInitialized;
	private Runnable _showOverlayDelayed = new Runnable()
	{
		@Override
		public void run()
		{
			try
			{
				Environment.getInstance().getStateManager().setStateOverlay(FeatureStateNetworkWizard.this, null);
			}
			catch (StateException e)
			{
				Log.e(TAG, e.getMessage(), e);
			}
		}
	};

	public FeatureStateNetworkWizard() throws FeatureNotFoundException
	{
		require(FeatureName.Scheduler.INTERNET);
		require(FeatureStateNetworkError.class);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.NETWORK_WIZARD;
	}

	@Override
	public void initialize(OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		_onFeatureInitialized = onFeatureInitialized;
		try
		{
			Environment env = Environment.getInstance();

			_featureStateNetworkError = (FeatureStateNetworkError) env.getFeatureManager().getFeature(FeatureStateNetworkError.class);

			// checking internet connection may exceed the allowed init timeout
			env.getFeatureManager().stopInitTimeout();
			_feature.Scheduler.INTERNET.startCheckUrl(getPrefs().getString(Param.INTERNET_CHECK_URL));
			checkInternet();
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
			onFeatureInitialized.onInitialized(this, ResultCode.GENERAL_FAILURE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.state_network_wizard, container, false);
	}

	// check if internet is available
	private void checkInternet()
	{
		Log.i(TAG, ".checkInternet");
		getEventMessenger().postDelayed(_showOverlayDelayed, getPrefs().getInt(Param.DISPLAY_CONNECTING_TIMEOUT));

		_feature.Scheduler.INTERNET.checkInternet(new OnResultReceived()
		{
			@Override
			public void onReceiveResult(int resultCode, Bundle resultData)
			{
				Log.i(TAG, ".checkInternet.onReceiveResult: resultCode = " + resultCode);
				getEventMessenger().removeCallbacks(_showOverlayDelayed);
				close();
				if (resultCode != ResultCode.OK)
				{
					// switch to network error state
					try
					{
						Environment.getInstance().getFeatureManager().stopInitTimeout();
						Environment.getInstance().getStateManager()
						        .setStateOverlay(_featureStateNetworkError, null);

						getEventMessenger().trigger(ON_INTERNET_CONNECTIVITY_ERROR);

						_featureStateNetworkError.getEventMessenger().register(new EventReceiver()
						{
							@Override
							public void onEvent(int msgId, Bundle bundle)
							{
								checkInternet();
								_featureStateNetworkError.getEventMessenger().unregister(this,
								        FeatureState.ON_HIDE);
							}
						}, FeatureState.ON_HIDE);
					}
					catch (StateException e)
					{
						Log.e(TAG, e.getMessage(), e);
						_onFeatureInitialized.onInitialized(FeatureStateNetworkWizard.this,
						        ResultCode.GENERAL_FAILURE);
					}
				}
				else
				{
					FeatureStateNetworkWizard.super.initialize(_onFeatureInitialized);
					getEventMessenger().trigger(ON_INTERNET_CONNECTIVITY_ESTABLISHED);
				}
			}
		});
	}
}
