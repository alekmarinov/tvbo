/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureStateSettingsEthernet.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Ethernet settings state feature
 */

package com.aviq.tv.android.aviqtv.state.settings.ethernet;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.aviqtv.state.keyboard.FeatureStateKeyboard;
import com.aviq.tv.android.aviqtv.state.settings.FeatureStateSettings;
import com.aviq.tv.android.sdk.core.AVKeyEvent;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.Key;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.IStateMenuItem;
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.core.state.StateManager.MessageParams;
import com.aviq.tv.android.sdk.feature.network.FeatureEthernet;
import com.aviq.tv.android.sdk.feature.network.NetworkConfig;

/**
 * Ethernet settings state feature
 */
public class FeatureStateSettingsEthernet extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateSettingsEthernet.class.getSimpleName();

	private FeatureStateSettings _featureStateSettings;
	private FeatureEthernet _featureEthernet;
	private ViewGroup _viewGroup;
	private RadioButton _rbDhcp;
	private RadioButton _rbManual;
	private TextView _tvIp;
	private TextView _tvMask;
	private TextView _tvGateway;
	private TextView _tvDns1;
	private TextView _tvDns2;
	private Button _btnClr;
	private Button _btnOk;

	public FeatureStateSettingsEthernet() throws FeatureNotFoundException
	{
		require(FeatureName.Component.ETHERNET);
		require(FeatureName.State.SETTINGS);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.SETTINGS_ETHERNET;
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		// insert in Settings
		_featureStateSettings = (FeatureStateSettings) Environment.getInstance().getFeatureState(
		        FeatureName.State.SETTINGS);
		_featureEthernet = (FeatureEthernet) Environment.getInstance().getFeatureComponent(
		        FeatureName.Component.ETHERNET);
		_featureStateSettings.addSettingState(this);

		super.initialize(onFeatureInitialized);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		_viewGroup = (ViewGroup) inflater.inflate(R.layout.state_settings_ethernet, container, false);
		_rbDhcp = (RadioButton) _viewGroup.findViewById(R.id.rb_dhcp);
		_rbManual = (RadioButton) _viewGroup.findViewById(R.id.rb_manual);
		_tvIp = (TextView) _viewGroup.findViewById(R.id.eth_ip);
		_tvMask = (TextView) _viewGroup.findViewById(R.id.eth_mask);
		_tvGateway = (TextView) _viewGroup.findViewById(R.id.eth_gateway);
		_tvDns1 = (TextView) _viewGroup.findViewById(R.id.eth_dns1);
		_tvDns2 = (TextView) _viewGroup.findViewById(R.id.eth_dns2);

		_btnOk = (Button) _viewGroup.findViewById(R.id.eth_btn_ok);
		_btnOk.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (writeEthernetConfiguration())
					backToSettings();
			}
		});
		_btnClr = (Button) _viewGroup.findViewById(R.id.eth_btn_clear);
		_btnClr.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				readEthernetConfiguration();
			}
		});

		_rbDhcp.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				enableManualSettings(!isChecked);
			}
		});

		_rbManual.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				enableManualSettings(isChecked);
			}
		});

		setEditClickListeners();

		readEthernetConfiguration();
		return _viewGroup;
	}

	@Override
	protected void onShow(boolean isViewUncovered)
	{
		super.onShow(isViewUncovered);
		_viewGroup.requestFocus();
	}

	@Override
	protected void onHide(boolean isViewCovered)
	{
		super.onHide(isViewCovered);
	}

	@Override
	public int getMenuItemResourceId()
	{
		// FIXME: replace with ic_menu_settings_ethernet when ready
		return R.drawable.ic_menu_settings;
	}

	@Override
	public String getMenuItemCaption()
	{
		return Environment.getInstance().getResources().getString(R.string.menu_settings_ethernet);
	}

	@Override
	public boolean onKeyDown(AVKeyEvent event)
	{
		Log.i(TAG, ".onKeyDown: key = " + event);
		if (event.is(Key.BACK))
		{
			backToSettings();
			return true;
		}
		return false;
	}

	private void readEthernetConfiguration()
	{
		NetworkConfig networkConfig = _featureEthernet.getNetworkConfig();
		Log.i(TAG, ".updateView: " + networkConfig);

		_rbDhcp.setChecked(networkConfig.IsDHCP);
		_tvIp.setText(networkConfig.Addr);
		_tvMask.setText(networkConfig.Mask);
		_tvGateway.setText(networkConfig.Gateway);
		_tvDns1.setText(networkConfig.Dns1);
		_tvDns2.setText(networkConfig.Dns2);
		enableManualSettings(!networkConfig.IsDHCP);
	}

	private boolean writeEthernetConfiguration()
	{
		NetworkConfig networkConfig = _featureEthernet.getNetworkConfig();
		networkConfig.IsDHCP = _rbDhcp.isChecked();
		networkConfig.Addr = _tvIp.getText().toString();
		networkConfig.Mask = _tvMask.getText().toString();
		networkConfig.Gateway = _tvGateway.getText().toString();
		networkConfig.Dns1 = _tvDns1.getText().toString();
		networkConfig.Dns2 = _tvDns2.getText().toString();

		try
		{
			_featureEthernet.configureNetwork(networkConfig);
			return true;
		}
		catch (SecurityException e)
		{
			// No permissions to configure ethernet
			MessageParams messageParams = new MessageParams().setText(R.string.error_permission_network_config)
			        .setType(MessageParams.Type.ERROR).enableButton(MessageParams.Button.OK);
			Environment.getInstance().getStateManager().showMessage(messageParams);
		}
		return false;
	}

	private void backToSettings()
	{
		try
		{
			Environment.getInstance().getStateManager().setStateMain(_featureStateSettings, null);
		}
		catch (StateException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void enableManualSettings(boolean isEnabled)
	{
		_tvIp.setEnabled(isEnabled);
		_tvMask.setEnabled(isEnabled);
		_tvGateway.setEnabled(isEnabled);
		_tvDns1.setEnabled(isEnabled);
		_tvDns2.setEnabled(isEnabled);

		if (isEnabled)
		{
			_rbManual.setNextFocusDownId(R.id.eth_ip);
			_rbManual.setNextFocusRightId(R.id.eth_ip);
			_btnClr.setNextFocusUpId(R.id.eth_dns2);
			_btnOk.setNextFocusUpId(R.id.eth_dns2);
		}
		else
		{
			_rbManual.setNextFocusDownId(R.id.eth_btn_clear);
			_rbManual.setNextFocusRightId(R.id.eth_btn_clear);
			_btnClr.setNextFocusUpId(R.id.rb_manual);
			_btnOk.setNextFocusUpId(R.id.rb_manual);
		}
	}

	private void setEditClickListeners()
	{
		OnClickListener editClickListener = new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				int promptResId = 0;
				String editText = null;
				switch (v.getId())
				{
					case R.id.eth_ip:
						promptResId = R.string.eth_ip;
					break;
					case R.id.eth_mask:
						promptResId = R.string.eth_mask;
					break;
					case R.id.eth_gateway:
						promptResId = R.string.eth_gateway;
					break;
					case R.id.eth_dns1:
						promptResId = R.string.eth_dns1;
					break;
					case R.id.eth_dns2:
						promptResId = R.string.eth_dns2;
					break;
				}

				String prompt = String.format(getResources().getString(R.string.edit_prompt),
				        getResources().getString(promptResId));

				Bundle params = new Bundle();
				params.putString(FeatureStateKeyboard.ARGS_PROMPT_TEXT, prompt);
				params.putString(FeatureStateKeyboard.ARGS_EDIT_TEXT, ((EditText) v).getText().toString());

				try
				{
					FeatureState keyaboardState = Environment.getInstance().getFeatureState(FeatureName.State.KEYBOARD);
					Environment.getInstance().getStateManager().setStateOverlay(keyaboardState, params);
				}
				catch (StateException e)
				{
					Log.e(TAG, e.getMessage(), e);
				}
			}
		};

		_tvIp.setOnClickListener(editClickListener);
		_tvMask.setOnClickListener(editClickListener);
		_tvGateway.setOnClickListener(editClickListener);
		_tvDns1.setOnClickListener(editClickListener);
		_tvDns2.setOnClickListener(editClickListener);
	}
}
