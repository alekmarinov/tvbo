/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureTV.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.home.feature.state.tv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.feature.epg.FeatureEPG;
import com.aviq.tv.android.home.feature.state.MessageBox;

/**
 * TV state feature
 */
public class FeatureStateTV extends FeatureState
{
	public static final String TAG = FeatureStateTV.class.getSimpleName();

	public enum Param
	{
		/**
		 * Delay in milliseconds to update program bar after channel selection
		 * change
		 */
		UPDATE_PROGRAM_BAR_DELAY(100);

		Param(int value)
		{
			Environment.getInstance().getFeaturePrefs(FeatureName.State.TV).put(name(), value);
		}

		Param(String value)
		{
			Environment.getInstance().getFeaturePrefs(FeatureName.State.TV).put(name(), value);
		}
	}

	private ViewGroup _viewGroup;
	private ZapperListView _zapperListView;
	private TextView _channelTitleTextView;
	private TextView _currentProgramTitle;
	private FeatureEPG _featureEPG;
	private ProgramBarUpdater _programBarUpdater = new ProgramBarUpdater();
	private int _updateProgramBarDelay;

	public FeatureStateTV()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.Components.add(FeatureName.Component.PLAYER);
		_dependencies.States.add(FeatureName.State.MESSAGE_BOX);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		super.initialize(onFeatureInitialized);
		try
		{
			_featureEPG = (FeatureEPG) Environment.getInstance().getFeatureComponent(FeatureName.Component.EPG);
			_updateProgramBarDelay = getPrefs().getInt(Param.UPDATE_PROGRAM_BAR_DELAY);
			onFeatureInitialized.onInitialized(this, ResultCode.OK);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
			onFeatureInitialized.onInitialized(this, ResultCode.GENERAL_FAILURE);
		}
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.TV;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		_viewGroup = (ViewGroup) inflater.inflate(R.layout.state_tv, container, false);
		_zapperListView = (ZapperListView) _viewGroup.findViewById(R.id.tv_channel_bar);
		_channelTitleTextView = (TextView) _viewGroup.findViewById(R.id.channel_title);
		_currentProgramTitle = (TextView) _viewGroup.findViewById(R.id.current_program_title);
		try
		{
			FeatureEPG featureEPG = (FeatureEPG) Environment.getInstance().getFeatureComponent(
			        FeatureName.Component.EPG);
			for (int i = 0; i < featureEPG.getChannelCount(); i++)
			{
				Bitmap bmp = featureEPG.getChannelLogoBitmap(i);
				if (bmp != null)
					_zapperListView.addBitmap(bmp);
				else
					Log.w(TAG, "Channel " + featureEPG.getChannelId(i) + " doesn't have image logo!");
			}
			onSelectChannelIndex(0);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return _viewGroup;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_ENTER:
				// test with message box
				boolean isAdded = false;
				try
				{
					MessageBox messageBox = (MessageBox) Environment.getInstance().getFeatureState(
					        FeatureName.State.MESSAGE_BOX);
					isAdded = messageBox.isAdded();
				}
				catch (FeatureNotFoundException e)
				{
					Log.e(TAG, e.getMessage(), e);
				}

				// show hide message box
				if (isAdded)
				{
					Environment.getInstance().getStateManager().hideMessage();
				}
				else
				{
					Environment.getInstance().getStateManager()
					        .showMessage(MessageBox.Type.ERROR, R.string.connection_lost);
				}
				return false;
			case KeyEvent.KEYCODE_DPAD_UP:
				_zapperListView.scrollUp();
				onSelectChannelIndex(_zapperListView.getSelectIndex());
				return true;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				_zapperListView.scrollDown();
				onSelectChannelIndex(_zapperListView.getSelectIndex());
				return true;
		}
		return false;
	}

	private void onSelectChannelIndex(int channelIndex)
	{
		String channelId = _featureEPG.getChannelId(channelIndex);

		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String now = df.format(Calendar.getInstance().getTime());
		Entry<String, String> currentTitle = _featureEPG.getProgram(channelId, now);
		Log.i(TAG, ".onSelectChannelIndex: channelIndex = " + channelIndex + ", channelId = " + channelId + ", now = "
		        + now + ", currentTitle = " + (currentTitle != null ? currentTitle.getKey() : "null") + " -> "
		        + (currentTitle != null ? currentTitle.getValue() : "null"));
		updateProgramBar(_featureEPG.getChannelTitle(channelIndex), currentTitle != null?currentTitle.getValue():"");
	}

	private void updateProgramBar(String channelTitle, String currentProgramTitle)
	{
		_programBarUpdater.ChannelTitle = channelTitle;
		_programBarUpdater.CurrentProgramTitle = currentProgramTitle;
		Environment.getInstance().getHandler().removeCallbacks(_programBarUpdater);
		Environment.getInstance().getHandler().postDelayed(_programBarUpdater, _updateProgramBarDelay);
	}

	private class ProgramBarUpdater implements Runnable
	{
		public String ChannelTitle;
		public String CurrentProgramTitle;

		@Override
		public void run()
		{
			_channelTitleTextView.setText(ChannelTitle);
			_currentProgramTitle.setText(CurrentProgramTitle);
		}
	}

}
