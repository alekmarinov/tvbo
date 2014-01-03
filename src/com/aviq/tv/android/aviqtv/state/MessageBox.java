/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    MessageBox.java
 * Author:      alek
 * Date:        14 Oct 2013
 * Description:
 */

package com.aviq.tv.android.aviqtv.state;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.EventMessenger;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.StateManager.MessageParams;

/**
 * Base class of all application message boxes
 */
public class MessageBox extends FeatureState
{
	private static final String TAG = MessageBox.class.getSimpleName();

	public static final int ON_BUTTON_PRESSED = EventMessenger.ID();

	private View _rootView;
	private ContextButtonGroup _contextButtonGroup;
	private Bundle _bundle;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_messagebox, container, false);
		ViewGroup messageContainer = (ViewGroup) viewGroup.findViewById(R.id.messageContainer);
		TextView titleText = (TextView) viewGroup.findViewById(R.id.title);
		TextView messageText = (TextView) viewGroup.findViewById(R.id.message);
		_contextButtonGroup = (ContextButtonGroup) viewGroup.findViewById(R.id.buttons);
		_rootView = viewGroup;

		_bundle = getArguments();
		if (_bundle == null)
			throw new IllegalArgumentException(".onCreateView: Invalid arguments for " + TAG);

		String title = _bundle.getString(MessageParams.PARAM_TITLE);
		String text = _bundle.getString(MessageParams.PARAM_TEXT);

		MessageParams.Type msgType = MessageParams.Type.valueOf(_bundle.getString(MessageParams.PARAM_TYPE));
		int drawableResId = R.drawable.transparent;

		switch (msgType)
		{
			case INFO:
			// FIXME: Change background image for INFO
			break;
			case WARN:
				drawableResId = R.drawable.ic_warning;
			break;
			default:
			break;
		}

		Drawable img = getResources().getDrawable(drawableResId);
		titleText.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);

		titleText.setText(title);
		messageText.setText(text);


		for (MessageParams.Button buttonName : MessageParams.Button.values())
		{
			Log.i(TAG, buttonName + " -> " + _bundle.getBoolean(buttonName.name()));
			if (_bundle.getBoolean(buttonName.name()))
			{
				createContextButton(buttonName);
			}
		}
		_contextButtonGroup.setButtonOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Log.i(TAG, ".onClick: " + view.getTag());
				_bundle.putBoolean(view.getTag().toString(), true);
				getEventMessenger().trigger(ON_BUTTON_PRESSED, _bundle);
				hide();
			}
		});

		Log.i(TAG, ".onCreateView: " + messageText.getText());
		return viewGroup;
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.MESSAGE_BOX;
	}

	@Override
	public void onShow(boolean isViewUncovered)
	{
		super.onShow(isViewUncovered);
		_rootView.requestFocus();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.i(TAG, ".onKeyDown: keyCode = " + keyCode);
		return _contextButtonGroup.onKeyDown(keyCode, event);
	}

	private void createContextButton(MessageParams.Button buttonName)
	{
		Log.i(TAG, "Create button: " + buttonName);
		switch (buttonName)
		{
			case OK:
				_contextButtonGroup.createButton(R.drawable.ic_ok, R.string.ok).setTag(MessageParams.Button.OK);
			break;
			case CANCEL:
				_contextButtonGroup.createButton(android.R.drawable.ic_delete, R.string.cancel).setTag(
				        MessageParams.Button.CANCEL);
			break;
			case YES:
				_contextButtonGroup.createButton(R.drawable.ic_ok, R.string.yes).setTag(
				        MessageParams.Button.YES);
			break;
			case NO:
				_contextButtonGroup.createButton(android.R.drawable.ic_delete, R.string.no).setTag(
				        MessageParams.Button.NO);
			break;
		}
	}
}
