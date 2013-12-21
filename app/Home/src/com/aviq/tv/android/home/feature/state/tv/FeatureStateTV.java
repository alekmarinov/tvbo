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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.event.EventMessenger;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.core.state.IStateMenuItem;
import com.aviq.tv.android.home.feature.epg.Channel;
import com.aviq.tv.android.home.feature.epg.EpgData;
import com.aviq.tv.android.home.feature.epg.FeatureEPG;
import com.aviq.tv.android.home.feature.epg.Program;
import com.aviq.tv.android.home.feature.player.FeaturePlayer;
import com.aviq.tv.android.home.feature.state.menu.FeatureStateMenu;

/**
 * TV state feature
 */
public class FeatureStateTV extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateTV.class.getSimpleName();
	public static final DateFormat CLOCK_FORMAT = new SimpleDateFormat("HH:mm:ss EEE, MMM d, ''yy, z");
	public static final int ON_TIMER = EventMessenger.ID();

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
	private TextView _clockTextView;
	private TextView _channelNoTextView;
	private ImageView _channelLogoImageView;
	private FeatureEPG _featureEPG;
	private EpgData _epgData;
	private FeaturePlayer _featurePlayer;
	private ProgramBarUpdater _programBarUpdater = new ProgramBarUpdater();
	private int _updateProgramBarDelay;
	private ProgramBar _programBar;

	public FeatureStateTV()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.Components.add(FeatureName.Component.PLAYER);
		_dependencies.States.add(FeatureName.State.MENU);
		_dependencies.States.add(FeatureName.State.MESSAGE_BOX);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		try
		{
			_featureEPG = (FeatureEPG) Environment.getInstance().getFeatureComponent(FeatureName.Component.EPG);
			_featurePlayer = (FeaturePlayer) Environment.getInstance()
			        .getFeatureComponent(FeatureName.Component.PLAYER);
			_updateProgramBarDelay = getPrefs().getInt(Param.UPDATE_PROGRAM_BAR_DELAY);

			FeatureStateMenu featureStateMenu = (FeatureStateMenu) Environment.getInstance().getFeatureState(
			        FeatureName.State.MENU);
			featureStateMenu.addMenuItemState(this);

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
		_clockTextView = (TextView) _viewGroup.findViewById(R.id.clock);
		_zapperListView = (ZapperListView) _viewGroup.findViewById(R.id.tv_channel_bar);
		_channelNoTextView = (TextView) _viewGroup.findViewById(R.id.channel_no);
		_channelLogoImageView = (ImageView) _viewGroup.findViewById(R.id.channel_logo);
		_programBar = new ProgramBar((ViewGroup) _viewGroup.findViewById(R.id.tv_program_bar));

		_epgData = _featureEPG.getEpgData();
		for (int i = 0; i < _epgData.getChannelCount(); i++)
		{
			Bitmap bmp = _epgData.getChannelLogoBitmap(i);
			if (bmp == null)
				Log.w(TAG, "Channel " + _epgData.getChannel(i).getChannelId() + " doesn't have image logo!");
			_zapperListView.addBitmap(bmp);
		}
		onSelectChannelIndex(0);
		return _viewGroup;
	}

	private void onSelectChannelIndex(int channelIndex)
	{
		// stop timer
		// Update selected channel logo
		_channelNoTextView.setText(String.valueOf(channelIndex + 1));
		_channelLogoImageView.setImageBitmap(_epgData.getChannelLogoBitmap(channelIndex));

		// Update program bar
		updateProgramBar(_epgData.getChannel(channelIndex), Calendar.getInstance());
	}

	private void onSwitchChannelIndex(int channelIndex)
	{
		String streamUrl = _featureEPG.getChannelStreamUrl(channelIndex);
		_featurePlayer.getPlayer().play(streamUrl);
	}

	private void updateClock()
	{
		_clockTextView.setText(CLOCK_FORMAT.format(Calendar.getInstance().getTime()));
	}

	private Runnable _onStartProgramBarUpdate = new Runnable()
	{
		@Override
		public void run()
		{
			subscribe(FeatureStateTV.this, ON_TIMER);
			_programBarUpdater.run();
			getEventMessenger().trigger(ON_TIMER, 1000);
		}
	};

	private void updateProgramBar(Channel channel, Calendar when)
	{
		if (isSubscribed(FeatureStateTV.this, ON_TIMER))
			unsubscribe(FeatureStateTV.this, ON_TIMER);
		_programBarUpdater.Channel = channel;
		_programBarUpdater.When = when;
		Environment.getInstance().getEventMessenger().removeCallbacks(_onStartProgramBarUpdate);
		Environment.getInstance().getEventMessenger().postDelayed(_onStartProgramBarUpdate, _updateProgramBarDelay);
	}

	private class ProgramBarUpdater implements Runnable
	{
		private Channel Channel;
		private Calendar When;

		@Override
		public void run()
		{
			// start timer

			Program previousProgram = null;
			Program currentProgram = null;
			Program nextProgram = null;

			if (Channel != null)
			{
				String channelId = Channel.getChannelId();
				_programBar.ChannelTitle.setText(Channel.getTitle());
				int programIndex = _epgData.getProgramIndex(channelId, When);
				previousProgram = _epgData.getProgramByIndex(channelId, programIndex - 1);
				currentProgram = _epgData.getProgramByIndex(channelId, programIndex);
				nextProgram = _epgData.getProgramByIndex(channelId, programIndex + 1);
			}

			_programBar.setPrograms(When, previousProgram, currentProgram, nextProgram);
		}
	}

	private class ProgramBar
	{
		private final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
		private TextView ChannelTitle;
		private TextView PreviousProgramTime;
		private TextView PreviousProgramTitle;
		private ImageView ProgramImage;
		private TextView CurrentProgramTime;
		private TextView CurrentProgramTitle;
		private TextView NextProgramTime;
		private TextView NextProgramTitle;
		private TextView ProgressStartTime;
		private TextView ProgressEndTime;
		private ProgressBar ProgramProgress;

		private ProgramBar(ViewGroup parent)
		{
			ChannelTitle = (TextView) _viewGroup.findViewById(R.id.channel_title);
			PreviousProgramTime = (TextView) _viewGroup.findViewById(R.id.previous_program_time);
			PreviousProgramTitle = (TextView) _viewGroup.findViewById(R.id.previous_program_title);
			ProgramImage = (ImageView) _viewGroup.findViewById(R.id.program_image);
			CurrentProgramTime = (TextView) _viewGroup.findViewById(R.id.current_program_time);
			CurrentProgramTitle = (TextView) _viewGroup.findViewById(R.id.current_program_title);
			NextProgramTime = (TextView) _viewGroup.findViewById(R.id.next_program_time);
			NextProgramTitle = (TextView) _viewGroup.findViewById(R.id.next_program_title);
			ProgressStartTime = (TextView) _viewGroup.findViewById(R.id.program_start);
			ProgressEndTime = (TextView) _viewGroup.findViewById(R.id.program_end);
			ProgramProgress = (ProgressBar) _viewGroup.findViewById(R.id.program_progress);
		}

		private void setPrograms(Calendar When, Program previousProgram, Program currentProgram, Program nextProgram)
		{
			// update programs info
			setPreviousProgram(previousProgram);
			setCurrentProgram(currentProgram);
			setNextProgram(nextProgram);

			// update progress bar
			if (currentProgram != null)
			{
				try
				{
					Calendar startTime = parseDateTime(currentProgram.getStartTime());
					Calendar endTime = parseDateTime(currentProgram.getStopTime());

					long elapsed = When.getTimeInMillis() - startTime.getTimeInMillis();
					long total = endTime.getTimeInMillis() - startTime.getTimeInMillis();
					ProgramProgress.setProgress((int) (100.0f * elapsed / total));

					ProgressStartTime.setText(parseDateTimeToHourMins(currentProgram.getStartTime()));
					ProgressEndTime.setText(parseDateTimeToHourMins(currentProgram.getStopTime()));
				}
				catch (ParseException e)
				{
					Log.w(TAG, e.getMessage());
				}
			}
			else
			{
				ProgramProgress.setProgress(0);
			}
		}

		private void setPreviousProgram(Program program)
		{
			setProgramToView(program, PreviousProgramTime, PreviousProgramTitle);
		}

		private void setCurrentProgram(Program program)
		{
			setProgramToView(program, CurrentProgramTime, CurrentProgramTitle);
		}

		private void setNextProgram(Program program)
		{
			setProgramToView(program, NextProgramTime, NextProgramTitle);
		}

		private void setProgramToView(Program program, TextView programTime, TextView programTitle)
		{
			if (program == null)
			{
				programTime.setText(null);
				programTitle.setText(null);
			}
			else
			{
				try
				{
					programTime.setText(parseDateTimeToHourMins(program.getStartTime()));
				}
				catch (ParseException e)
				{
					Log.w(TAG, e.getMessage());
					programTime.setText(null);
				}
				programTitle.setText(program.getTitle());
			}
		}

		private Calendar parseDateTime(String dateTime) throws ParseException
		{
			return Program.getEpgTime(dateTime);
		}

		private String parseDateTimeToHourMins(String dateTime) throws ParseException
		{
			Calendar startTime = parseDateTime(dateTime);
			return TIME_FORMAT.format(startTime.getTime());
		}
	}

	@Override
	public void onEvent(int msgId, Bundle bundle)
	{
		// Log.i(TAG, ".onEvent: msgId = " + msgId);
		if (msgId == ON_TIMER)
		{
			// Log.i(TAG, ".onEvent: Updating on timer event");
			_programBarUpdater.run();
			updateClock();
			getEventMessenger().trigger(ON_TIMER, 1000);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_ENTER:
				// Switch TV channel
				onSwitchChannelIndex(_zapperListView.getSelectIndex());
				return true;
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

	// IMenuItemState implementation

	@Override
	public int getMenuItemResourceId()
	{
		return R.drawable.ic_menu_tv;
	}

	@Override
	public String getMenuItemCaption()
	{
		return getStateName().name();
	}
}
