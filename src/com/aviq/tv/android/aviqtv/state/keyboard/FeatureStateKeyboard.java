/**
 * Copyright (c) 2003-2013, AVIQ Systems AG
 *
 * Project:     AviqTV
 * Filename:
 * Author:      Nadia
 * Date:        Jan 8, 2014
 * Description: Entity class Channel
 */

package com.aviq.tv.android.aviqtv.state.keyboard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.Log;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.feature.httpserver.FeatureHttpServer;

public class FeatureStateKeyboard extends FeatureState
{
	private static final String TAG = FeatureStateKeyboard.class.getSimpleName();

	public static final String ARGS_PROMPT_TEXT = "promptText";
	public static final String ARGS_EDIT_TEXT = "editText";
	public static final String ARGS_IS_PASSWORD = "isPassword";

	private WebView _webView;
	private TextView _prompt;
	private EditText _editField;

	public FeatureStateKeyboard()
	{
		_dependencies.Components.add(FeatureName.Component.HTTP_SERVER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_keyboard, container, false);
		_webView = (WebView) viewGroup.findViewById(R.id.keyboard);
		_prompt = (TextView) viewGroup.findViewById(R.id.prompt);
		_editField = (EditText) viewGroup.findViewById(R.id.edit);

		Bundle params = getArguments();
		_prompt.setText(params.getString(ARGS_PROMPT_TEXT));
		_editField.setText(params.getString(ARGS_EDIT_TEXT));
		if (params.getBoolean(ARGS_IS_PASSWORD))
			_editField.setTransformationMethod(PasswordTransformationMethod.getInstance());

		_webView.setWebChromeClient(new WebChromeClient()
		{
			@Override
			public void onConsoleMessage(final String message, int lineNumber, String sourceID)
			{
				Log.d(TAG, message + " -- From line " + lineNumber + " of " + sourceID);
				if (message.startsWith("key-changed:"))
				{
					Environment.getInstance().getActivity().runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							String key = message.replace("key-changed:", "");

							if (key.equals("ok"))
							{
								if (_editField.getText().length() != 0)
								{
									Environment.getInstance().getStateManager().hideStateOverlay();
									// send password back to calling activity
									// Intent intent = new
									// Intent(WifiPassword.this,
									// WifiWizard.class);
									// intent.putExtra(WIFI_PASSWORD,
									// _editField.getText().toString());
									// intent.putExtra(WIFI_ACCESS_POINT,
									// _ssid);
									// setResult(RESULT_OK, intent);
									// finish();
								}
								else
								{
									// Toast.makeText(WifiPassword.this,
									// R.string.wifi_password_empty,
									// Toast.LENGTH_SHORT)
									// .show();
								}
							}
							else if (key.equals("backspace"))
							{
								// removes last character
								if (_editField.getText().length() > 0)
								{
									_editField.getText().delete(_editField.getText().length() - 1,
									        _editField.getText().length());
								}
							}
							// appends key character
							else if (key.equals("&amp;"))
								_editField.append("&");
							else if (key.equals("&gt;"))
								_editField.append(">");
							else if (key.equals("&lt;"))
								_editField.append("<");
							else
								_editField.append(key);
						}
					});
				}
			}
		});

		WebSettings settings = _webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setAppCacheEnabled(true);

		// handle Access-Control-Allow-Origin issue with SDK >=16
		if (android.os.Build.VERSION.SDK_INT >= 16) // android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
		{
			try
			{
				fixJellyBeanIssues(settings);
			}
			catch (Exception e)
			{
				Log.e(TAG, ".fixJellyBeanIssues got exception", e);
			}
		}

		// load url
		try
		{
			FeatureHttpServer httpServer = (FeatureHttpServer) Environment.getInstance().getFeatureComponent(
			        FeatureName.Component.HTTP_SERVER);
			int port = httpServer.getListenPort();

			Log.d(TAG, "load url through httpServer");
			_webView.loadUrl("http://localhost:" + port + "/keyboard/aviqtv.html");
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);

			Log.d(TAG, "load url from assets");
			_webView.loadUrl("file:///android_asset/keyboard/aviqtv.html");
		}
		_webView.setBackgroundColor(0x00000000);

		// solves blocked Left|OK issue
		_webView.setFocusable(false);
		_editField.setFocusable(false);
		return viewGroup;
	}

	protected void fixJellyBeanIssues(WebSettings settings) throws NoSuchMethodException, IllegalArgumentException,
	        IllegalAccessException, InvocationTargetException
	{
		Class<?> clazz = settings.getClass();
		Method method = clazz.getMethod("setAllowUniversalAccessFromFileURLs", boolean.class);
		if (method != null)
		{
			method.invoke(settings, true);
		}
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.KEYBOARD;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		super.onKeyDown(keyCode, event);
		boolean isMenuKey = false;
		int jsCode = 0;
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				// Hide overlay state
				Environment.getInstance().getStateManager().hideStateOverlay();
				return true;
			case KeyEvent.KEYCODE_F2:
				return true;
			case KeyEvent.KEYCODE_DPAD_UP:
				jsCode = 38;
			break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				jsCode = 37;
			break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				jsCode = 39;
			break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				jsCode = 40;
			break;
			case KeyEvent.KEYCODE_ENTER:
			case KeyEvent.KEYCODE_DPAD_CENTER:
				jsCode = 13;
			break;
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				return true;
		}
		Log.d(TAG, "onKeyDown: keyCode = " + keyCode + ", jsCode = " + jsCode);

		if (isMenuKey)
			_webView.loadUrl("javascript: top.keyboard.showNextPage();");
		else
			_webView.loadUrl("javascript: top.keyboard.handleKey(" + jsCode + ");");

		return true;
	}
}
