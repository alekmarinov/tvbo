/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    MessageBox.java
 * Author:      alek
 * Date:        14 Oct 2013
 * Description:
 */

package com.aviq.tv.android.home.feature.state;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureState;

/**
 * Base class of all application message boxes
 */
public class MessageBox extends FeatureState
{
	private static final String TAG = MessageBox.class.getSimpleName();

	public static final String PARAM_TYPE = "PARAM_TYPE";
	public static final String PARAM_TEXT_ID = "PARAM_TEXT_ID";

	public enum Type
	{
		INFO, WARN, ERROR
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_messagebox, container, false);
		ViewGroup messageContainer = (ViewGroup) viewGroup.findViewById(R.id.messageContainer);
		TextView messageText = (TextView) viewGroup.findViewById(R.id.messageText);

		Bundle params = getArguments();
		if (params == null)
			throw new IllegalArgumentException(".onCreateView: Invalid arguments for " + TAG);

		int textId = params.getInt(PARAM_TEXT_ID);
		int resId = R.drawable.problem;
		Type msgType = Type.valueOf(params.getString(PARAM_TYPE));
		switch (msgType)
		{
			case INFO:
				// FIXME: Change background image for INFO
				break;
			case WARN:
				// FIXME: Change background image for WARN
				break;
			default:
				break;
		}
		messageText.setText(textId);
		messageContainer.setBackgroundResource(resId);
		Log.i(TAG, ".onCreateView: " + messageText.getText());

		return viewGroup;
	}

	@Override
    public FeatureName.State getStateName()
    {
	    return FeatureName.State.MESSAGE_BOX;
    }
}
