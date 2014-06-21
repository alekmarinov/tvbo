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

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.AVKeyEvent;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.EventMessenger;
import com.aviq.tv.android.sdk.core.Key;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.StateManager.MessageParams;

/**
 * Base class of all application message boxes
 */
public class MessageBox extends FeatureState
{
	private static final String TAG = MessageBox.class.getSimpleName();

	public static final int ON_BUTTON_PRESSED = EventMessenger.ID("ON_BUTTON_PRESSED");

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
			case ERROR:
				if (title == null)
					title = getResources().getString(R.string.msg_title_error);
			break;
		}
		titleText.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);

		titleText.setText(title);
		messageText.setText(text);
		titleText.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
		messageText.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);

		for (MessageParams.Button buttonName : MessageParams.Button.values())
		{
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
				_bundle.putString("pressed", view.getTag().toString());
				getEventMessenger().trigger(ON_BUTTON_PRESSED, _bundle);
				hide();
			}
		});
		_contextButtonGroup.setVisibility(MessageParams.Button.values().length > 0 ? View.VISIBLE : View.GONE);

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

	/**
	 * Hides message from screen
	 */
	@Override
	public void hide()
	{
		Environment.getInstance().getStateManager().hideMessage();
	}

	@Override
	public boolean onKeyDown(AVKeyEvent event)
	{
		Log.i(TAG, ".onKeyDown: key = " + event);
		if (event.is(Key.BACK))
		{
			hide();
		}
		else
		{
			_contextButtonGroup.onKeyDown(event.Event.getKeyCode(), event.Event);
		}
		return true;
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
				_contextButtonGroup.createButton(R.drawable.ic_ok, R.string.yes).setTag(MessageParams.Button.YES);
			break;
			case NO:
				_contextButtonGroup.createButton(android.R.drawable.ic_delete, R.string.no).setTag(
				        MessageParams.Button.NO);
			break;
		}
	}
}
