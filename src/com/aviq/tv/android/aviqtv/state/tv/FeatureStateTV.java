/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureTV.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.aviqtv.state.tv;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.aviqtv.state.menu.FeatureStateMenu;
import com.aviq.tv.android.sdk.core.AVKeyEvent;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.EventMessenger;
import com.aviq.tv.android.sdk.core.Key;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.IStateMenuItem;
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.feature.epg.Channel;
import com.aviq.tv.android.sdk.feature.epg.FeatureEPG;
import com.aviq.tv.android.sdk.feature.epg.IEpgDataProvider;
import com.aviq.tv.android.sdk.feature.epg.Program;
import com.aviq.tv.android.sdk.feature.epg.bulsat.ChannelBulsat;
import com.aviq.tv.android.sdk.feature.player.FeaturePlayer;
import com.aviq.tv.android.sdk.feature.player.FeatureTimeshift;
import com.aviq.tv.android.sdk.feature.rcu.FeatureRCU;
import com.aviq.tv.android.sdk.utils.Calendars;

/**
 * TV state feature
 */
public class FeatureStateTV extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateTV.class.getSimpleName();
	public static final DateFormat CLOCK_FORMAT = new SimpleDateFormat("HH:mm:ss EEE, MMM d, ''yy, z", Locale.US);

	private static final int ON_TIMER = EventMessenger.ID("ON_TIMER");
	private static final int ON_OSD_AUTOHIDE = EventMessenger.ID("ON_OSD_AUTOHIDE");
	private static final int ON_NUM_TIMEOUT = EventMessenger.ID("ON_NUM_TIMEOUT");
	private DateFormat TIME_FORMAT;
	// private DateFormat DAY_FORMAT;
	// private DateFormat MONTH_FORMAT;
	// private DateFormat WEEKDAY_FORMAT;
	private TVStateManager _tvStateManager;
	private TextView _clock;
	private int _channelNumber = 0;
	private SparseIntArray _channelNumberToIndex;
	private int _lastChannelIndex;

	public enum Extras
	{
		CHANNEL_INDEX, PLAY_TIME, PLAY_DURATION
	}

	public enum Param
	{
		/**
		 * Delay in milliseconds to update program bar after channel selection
		 * change
		 */
		UPDATE_PROGRAM_BAR_DELAY(100),

		/**
		 * Delay in seconds to auto hide channel number entered with the numeric
		 * pad
		 */
		NUM_AUTOHIDE(3),

		/**
		 * Delay in seconds to auto hide volume bar
		 */
		OSD_AUTOHIDE_VOLUME(4),

		/**
		 * Number of seconds to seek forward in timeshift
		 */
		SEEK_SHORT_FORWARD(15),

		/**
		 * Number of seconds to seek backward in timeshift
		 */
		SEEK_SHORT_BACKWARD(-15),

		/**
		 * Number of seconds to seek forward in timeshift
		 */
		SEEK_LONG_FORWARD(300),

		/**
		 * Number of seconds to seek backward in timeshift
		 */
		SEEK_LONG_BACKWARD(-300);

		Param(int value)
		{
			Environment.getInstance().getFeaturePrefs(FeatureName.State.TV).put(name(), value);
		}

		Param(String value)
		{
			Environment.getInstance().getFeaturePrefs(FeatureName.State.TV).put(name(), value);
		}
	}

	// private ViewGroup _viewGroup;
	// private ZapperList _zapperList;
	// private TextView _clockTextView;
	// private TextView _channelNoTextView;
	// private ImageView _channelLogoImageView;
	// private ViewGroup _channelInfoContainer;
	// private FeatureEPG _featureEPG;
	// private IEpgDataProvider _epgData;
	// private FeaturePlayer _featurePlayer;
	// // private ProgramBarUpdater _programBarUpdater = new
	// ProgramBarUpdater();
	// private int _updateProgramBarDelay;
	// // private ProgramBar _programBar;
	// private Rect _displayTopTouchZone;
	// private boolean _isSnappingScroll = false;

	public FeatureStateTV() throws FeatureNotFoundException
	{
		require(FeatureName.Scheduler.EPG);
		require(FeatureName.Component.CHANNELS);
		require(FeatureName.Component.PLAYER);
		require(FeatureName.Component.LANGUAGE);
		require(FeatureName.State.MENU);
		require(FeatureName.Component.TIMEZONE);
		require(FeatureName.Component.TIMESHIFT);
		require(FeatureName.Component.RCU);
		require(FeatureName.Component.VOLUME);
		require(FeatureName.State.ERROR);
		// require(FeatureName.State.FAVOURITE_CHANNELS);
		require(FeatureName.State.MESSAGE_BOX);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		subscribe(this, ON_TIMER);
		subscribe(this, ON_OSD_AUTOHIDE);
		subscribe(this, ON_NUM_TIMEOUT);

		// subscribe(_feature.Component.PLAYER, FeaturePlayer.ON_PLAY_PAUSE);
		// subscribe(_feature.Component.PLAYER, FeaturePlayer.ON_PLAY_ERROR);
		// subscribe(_feature.Component.TIMESHIFT, FeatureTimeshift.ON_SEEK);
		// subscribe(Environment.getInstance().getEventMessenger(),
		// Environment.ON_RESUME);

		_feature.Component.RCU.getEventMessenger().register(this, FeatureRCU.ON_KEY_PRESSED);

		// _featureEPG = (FeatureEPG)
		// Environment.getInstance().getFeatureScheduler(FeatureName.Scheduler.EPG);
		// _featurePlayer = (FeaturePlayer)
		// Environment.getInstance().getFeatureComponent(FeatureName.Component.PLAYER);
		// _updateProgramBarDelay =
		// getPrefs().getInt(Param.UPDATE_PROGRAM_BAR_DELAY);

		FeatureStateMenu featureStateMenu = (FeatureStateMenu) Environment.getInstance().getFeatureState(
		        FeatureName.State.MENU);
		featureStateMenu.addMenuItemState(this);

		TIME_FORMAT = new SimpleDateFormat("HH:mm", _feature.Component.LANGUAGE.getLocale());
		// DAY_FORMAT = new SimpleDateFormat("EEE d",
		// _feature.Component.LANGUAGE.getLocale());
		// MONTH_FORMAT = new SimpleDateFormat("MMMM",
		// _feature.Component.LANGUAGE.getLocale());
		// WEEKDAY_FORMAT = new SimpleDateFormat("EEEE",
		// _feature.Component.LANGUAGE.getLocale());
		TIME_FORMAT.setTimeZone(_feature.Component.TIMEZONE.getTimeZone());
		// DAY_FORMAT.setTimeZone(_feature.Component.TIMEZONE.getTimeZone());
		// MONTH_FORMAT.setTimeZone(_feature.Component.TIMEZONE.getTimeZone());
		// WEEKDAY_FORMAT.setTimeZone(_feature.Component.TIMEZONE.getTimeZone());

		_lastChannelIndex = _feature.Component.CHANNELS.getLastChannelIndex();

		super.initialize(onFeatureInitialized);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.TV;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Log.i(TAG, ".onCreateView");
		// _viewGroup = (ViewGroup) inflater.inflate(R.layout.state_tv,
		// container, false);
		//
		// _clockTextView = (TextView) _viewGroup.findViewById(R.id.clock);
		// _tvStateManager = new TVStateManager(_viewGroup);
		// _zapperList = (ZapperList)
		// _viewGroup.findViewById(R.id.tv_channel_bar);
		// _channelNoTextView = (TextView)
		// _viewGroup.findViewById(R.id.channel_no);
		// _channelLogoImageView = (ImageView)
		// _viewGroup.findViewById(R.id.channel_logo);
		// // _programBar = new ProgramBar((ViewGroup)
		// // _viewGroup.findViewById(R.id.tv_program_bar));
		// _channelInfoContainer = (ViewGroup)
		// _viewGroup.findViewById(R.id.channel_info_container);
		// _epgData = _featureEPG.getEpgData();
		//
		// int channelIndex = 0;
		// updateChannelBarChannels(channelIndex);
		//
		// // Set player at full screen
		// _featurePlayer.setFullScreen();
		//
		// // Prepare the video view touch zones and set up the touch listener
		// prepareVideoViewTouchGestures();
		//
		// Bundle params = getArguments();
		// if (params != null)
		// {
		// String channelId = params.getString("CHANNEL");
		// if (channelId != null)
		// {
		// // Switching channel requested
		// _feature.Component.CHANNELS.play(channelId);
		// }
		// }
		// return _viewGroup;

		Log.i(TAG, ".onCreateView");

		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_tv, container, false);
		_clock = (TextView) viewGroup.findViewById(R.id.clock);

		_tvStateManager = new TVStateManager(viewGroup);
		// ((FeatureStateMenu)
		// _feature.State.MENU).setCurrentMenuItem(FeatureStateMenu.MENU_ITEM.TV);
		_tvStateManager.setState(TVStateEnum.HIDDEN);

		Bundle params = getArguments();
		if (params == null)
		{
			if (!_feature.Component.PLAYER.isPlaying())
				playChannel(_lastChannelIndex);
		}
		else
		{

			int channelIndex = params.getInt(Extras.CHANNEL_INDEX.name(), _lastChannelIndex);
			if (channelIndex != -1)
			{
				long playTime = params.getLong(Extras.PLAY_TIME.name(), 0);
				long playDuration = params.getLong(Extras.PLAY_DURATION.name(), 0);
				playChannel(channelIndex, playTime, playDuration);
			}
			else
			{
				Log.w(TAG, "Last channel is undefined");
			}
		}

		return viewGroup;
	}

	// @Override
	// protected void onShow(boolean isViewUncovered)
	// {
	// super.onShow(isViewUncovered);
	// int index = _zapperList.getPosition();
	// onSelectChannelIndex(_zapperList.getPosition());
	// }

	@Override
	public void onShow(boolean isViewUncovered)
	{
		super.onShow(isViewUncovered);
		Log.i(TAG, ".onShow: isViewUncovered = " + isViewUncovered);

		_tvStateManager.setState(TVStateEnum.HIDDEN);

		_feature.Component.PLAYER.setFullScreen();
		if (!isViewUncovered)
		{
			getEventMessenger().removeMessages(ON_TIMER);
			getEventMessenger().trigger(ON_TIMER, 1000);
			// update replay/record text
			_feature.Component.TIMESHIFT.getEventMessenger().trigger(FeatureTimeshift.ON_SEEK);
		}

		if (!_feature.Component.PLAYER.isPlaying())
			; // playChannel(_lastChannelIndex);
	}

	@Override
	public void onHide(boolean isViewCovered)
	{
		if (!isViewCovered)
		{
			_feature.Component.PLAYER.hide();// .setPositionAndSize(0, 0, 1,1);
		}
	}

	@Override
	public boolean onKeyDown(AVKeyEvent keyEvent)
	{
		Log.i(TAG, ".onKeyDown: key = " + keyEvent.Code);
		switch (keyEvent.Code)
		{
			case VOLUME_UP:
			case VOLUME_DOWN:
				// FIXME: place volume handling at a base class of all States
				// display global volume bar
				return true;
		}

		if (isShown())
		{
			return _tvStateManager.onKeyDown(keyEvent);
		}
		return super.onKeyDown(keyEvent);
	}

	@Override
	public void onEvent(int msgId, Bundle bundle)
	{
		super.onEvent(msgId, bundle);
		if (FeatureRCU.ON_KEY_PRESSED == msgId)
		{
			Key key = Key.valueOf(bundle.getString(Environment.EXTRA_KEY));
			boolean isConsumed = bundle.getBoolean(Environment.EXTRA_KEYCONSUMED);
			if (Key.TV.equals(key) && !isConsumed)
			{
				// show this TV state
				try
				{
					Environment.getInstance().getStateManager().setStateMain(FeatureStateTV.this, null);
				}
				catch (StateException e)
				{
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		else
		{
			if (isShown())
			{
				// handle event only if this state is shown
				if (Environment.ON_RESUME == msgId)
				{
					// restart last played channel
					if (!_feature.Component.PLAYER.isPlaying())
						; // playChannel(_lastChannelIndex);

					FeatureState overlayState = (FeatureState) Environment.getInstance().getStateManager()
					        .getOverlayState();
					if (overlayState == null)
					{
						TVStateEnum currentState = _tvStateManager.getCurrentStateEnum();
						if (TVStateEnum.HIDDEN.equals(currentState))
							_tvStateManager.setState(TVStateEnum.CHANNELS);
					}
				}
				else if (FeaturePlayer.ON_PLAY_ERROR == msgId)
				{
					// restart player once
					getEventMessenger().postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							playChannel(_lastChannelIndex);
						}
					}, 1000);
				}
				else
				{
					_tvStateManager.onEvent(msgId, bundle);
				}
			}
		}
	}

	private void playChannel(int index, long playTime, final long playDuration)
	{
		_feature.Component.CHANNELS.play(index, playTime, playDuration);
		_lastChannelIndex = index;
	}

	private void playChannel(int index)
	{
		playChannel(index, System.currentTimeMillis() / 1000, 0);
	}

	// private void onSelectChannelIndex(int channelIndex)
	// {
	// if (channelIndex < 0)
	// return;
	//
	// // Update selected channel number and logo
	// int globalIndex =
	// _feature.Component.CHANNELS.getActiveChannels().get(channelIndex).getIndex();
	// _channelNoTextView.setText(String.valueOf(channelIndex + 1));
	// _channelLogoImageView.setImageBitmap(_epgData.getChannelLogoBitmap(globalIndex));
	//
	// // Update program bar
	// updateProgramBar(_epgData.getChannel(globalIndex),
	// Calendar.getInstance());
	// }
	//
	// private void updateChannelBarChannels(int channelIndex)
	// {
	// _zapperList.setAdapter(new ChannelAdapter(getActivity(),
	// R.layout.tv_channel_item, getChannels(),
	// _feature.Scheduler.EPG), channelIndex);
	// }
	//
	// private List<Channel> getChannels()
	// {
	// return _feature.Component.CHANNELS.getActiveChannels();
	// }
	//
	// private void onSwitchChannelIndex(int channelIndex)
	// {
	// _feature.Component.CHANNELS.play(channelIndex);
	// }
	//
	// private void updateClock()
	// {
	// _clockTextView.setText(CLOCK_FORMAT.format(Calendar.getInstance().getTime()));
	// }

	// private Runnable _onStartProgramBarUpdate = new Runnable()
	// {
	// @Override
	// public void run()
	// {
	// subscribe(FeatureStateTV.this, ON_TIMER);
	// _programBarUpdater.run();
	// getEventMessenger().trigger(ON_TIMER, 1000);
	// }
	// };

	// private void updateProgramBar(Channel channel, Calendar when)
	// {
	// if (isSubscribed(FeatureStateTV.this, ON_TIMER))
	// unsubscribe(FeatureStateTV.this, ON_TIMER);
	// _programBarUpdater.Channel = channel;
	// _programBarUpdater.When = when;
	// Environment.getInstance().getEventMessenger().removeCallbacks(_onStartProgramBarUpdate);
	// Environment.getInstance().getEventMessenger().postDelayed(_onStartProgramBarUpdate,
	// _updateProgramBarDelay);
	// }

	// private class ProgramBarUpdater implements Runnable
	// {
	// private Channel Channel;
	// private Calendar When;
	//
	// @Override
	// public void run()
	// {
	// // start timer
	//
	// Program previousProgram = null;
	// Program currentProgram = null;
	// Program nextProgram = null;
	//
	// if (Channel != null)
	// {
	// String channelId = Channel.getChannelId();
	// _programBar.ChannelTitle.setText(Channel.getTitle());
	//
	// currentProgram = _epgData.getProgram(channelId, When);
	// if (currentProgram != null)
	// {
	// Calendar progTime = currentProgram.getStartTime().getInstance();
	// progTime.add(Calendar.MINUTE, -1);
	// previousProgram = _epgData.getProgram(channelId, progTime);
	// progTime = currentProgram.getStopTime().getInstance();
	// progTime.add(Calendar.MINUTE, 1);
	// nextProgram = _epgData.getProgram(channelId, progTime);
	// }
	// }
	//
	// _programBar.setPrograms(When, previousProgram, currentProgram,
	// nextProgram);
	// }
	// }
	//
	// private class ProgramBar
	// {
	// private final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm",
	// Locale.US);
	// private TextView ChannelTitle;
	// private TextView PreviousProgramTime;
	// private TextView PreviousProgramTitle;
	// private ImageView ProgramImage;
	// private TextView CurrentProgramTime;
	// private TextView CurrentProgramTitle;
	// private TextView NextProgramTime;
	// private TextView NextProgramTitle;
	// private TextView ProgressStartTime;
	// private TextView ProgressEndTime;
	// private ProgressBar ProgramProgress;
	//
	// private ProgramBar(ViewGroup parent)
	// {
	// ChannelTitle = (TextView) _viewGroup.findViewById(R.id.channel_title);
	// PreviousProgramTime = (TextView)
	// _viewGroup.findViewById(R.id.previous_program_time);
	// PreviousProgramTitle = (TextView)
	// _viewGroup.findViewById(R.id.previous_program_title);
	// ProgramImage = (ImageView) _viewGroup.findViewById(R.id.program_image);
	// CurrentProgramTime = (TextView)
	// _viewGroup.findViewById(R.id.current_program_time);
	// CurrentProgramTitle = (TextView)
	// _viewGroup.findViewById(R.id.current_program_title);
	// NextProgramTime = (TextView)
	// _viewGroup.findViewById(R.id.next_program_time);
	// NextProgramTitle = (TextView)
	// _viewGroup.findViewById(R.id.next_program_title);
	// ProgressStartTime = (TextView)
	// _viewGroup.findViewById(R.id.program_start);
	// ProgressEndTime = (TextView) _viewGroup.findViewById(R.id.program_end);
	// ProgramProgress = (ProgressBar)
	// _viewGroup.findViewById(R.id.program_progress);
	// }
	//
	// private void setPrograms(Calendar When, Program previousProgram, Program
	// currentProgram, Program nextProgram)
	// {
	// // update programs info
	// setPreviousProgram(previousProgram);
	// setCurrentProgram(currentProgram);
	// setNextProgram(nextProgram);
	//
	// // update progress bar
	// if (currentProgram != null)
	// {
	// try
	// {
	// Calendar startTime = currentProgram.getStartTime();
	// Calendar endTime = currentProgram.getStopTime();
	//
	// long elapsed = When.getTimeInMillis() - startTime.getTimeInMillis();
	// long total = endTime.getTimeInMillis() - startTime.getTimeInMillis();
	// ProgramProgress.setProgress((int) (100.0f * elapsed / total));
	//
	// ProgressStartTime.setText(parseDateTimeToHourMins(currentProgram.getStartTime()));
	// ProgressEndTime.setText(parseDateTimeToHourMins(currentProgram.getStopTime()));
	// }
	// catch (ParseException e)
	// {
	// Log.w(TAG, e.getMessage());
	// }
	// }
	// else
	// {
	// ProgramProgress.setProgress(0);
	// }
	// }
	//
	// private void setPreviousProgram(Program program)
	// {
	// setProgramToView(program, PreviousProgramTime, PreviousProgramTitle);
	// }
	//
	// private void setCurrentProgram(Program program)
	// {
	// setProgramToView(program, CurrentProgramTime, CurrentProgramTitle);
	// }
	//
	// private void setNextProgram(Program program)
	// {
	// setProgramToView(program, NextProgramTime, NextProgramTitle);
	// }
	//
	// private void setProgramToView(Program program, TextView programTime,
	// TextView programTitle)
	// {
	// if (program == null)
	// {
	// programTime.setText(null);
	// programTitle.setText(null);
	// }
	// else
	// {
	// try
	// {
	// programTime.setText(parseDateTimeToHourMins(program.getStartTime()));
	// }
	// catch (ParseException e)
	// {
	// Log.w(TAG, e.getMessage());
	// programTime.setText(null);
	// }
	// programTitle.setText(program.getTitle());
	// }
	// }
	//
	// private String parseDateTimeToHourMins(Calendar dateTime) throws
	// ParseException
	// {
	// return TIME_FORMAT.format(dateTime.getTime());
	// }
	// }

	// @Override
	// public void onEvent(int msgId, Bundle bundle)
	// {
	// // Log.i(TAG, ".onEvent: msgId = " + msgId);
	// if (msgId == ON_TIMER)
	// {
	// // Log.i(TAG, ".onEvent: Updating on timer event");
	// _programBarUpdater.run();
	// updateClock();
	// getEventMessenger().trigger(ON_TIMER, 1000);
	// }
	// }
	//
	// @Override
	// public boolean onKeyDown(AVKeyEvent event)
	// {
	// switch (event.Code)
	// {
	// case OK:
	// // Switch TV channel
	// onSwitchChannelIndex(_zapperList.getPosition());
	// return true;
	// case UP:
	// case DOWN:
	// _zapperList.onKeyDown(event.Event.getKeyCode(), event.Event);
	// onSelectChannelIndex(_zapperList.getPosition());
	// return true;
	// case BACK:
	// if (isShown())
	// hide();
	// else
	// show();
	// return true;
	// }
	// return false;
	// }

	// IMenuItemState implementation

	@Override
	public int getMenuItemResourceId()
	{
		return R.drawable.ic_menu_tv;
	}

	@Override
	public String getMenuItemCaption()
	{
		return Environment.getInstance().getResources().getString(R.string.menu_tv);
	}

	// private void prepareVideoViewTouchGestures()
	// {
	// Display display =
	// Environment.getInstance().getWindowManager().getDefaultDisplay();
	// Point size = new Point();
	// display.getSize(size);
	// int deviceScreenWidth = size.x;
	// int deviceScreenHeight = size.y;
	//
	// _displayTopTouchZone = new Rect(0, 0, deviceScreenWidth, (int) (0.20 *
	// deviceScreenHeight));
	//
	// _featurePlayer.getView().setOnTouchListener(_videoViewOnTouchListener);
	// }
	//
	// private OnTouchListener _videoViewOnTouchListener = new OnTouchListener()
	// {
	// @Override
	// public boolean onTouch(View v, MotionEvent event)
	// {
	// Log.v(TAG, ".onTouch: event = " + event.getAction());
	//
	// if (event.getAction() == MotionEvent.ACTION_DOWN)
	// {
	// return true;
	// }
	// else if (event.getAction() == MotionEvent.ACTION_UP)
	// {
	// int x = (int) event.getRawX();
	// int y = (int) event.getRawY();
	//
	// if (_displayTopTouchZone.contains(x, y))
	// {
	// BaseState menuState =
	// Environment.getInstance().getFeatureState(FeatureName.State.MENU);
	// try
	// {
	// Environment.getInstance().getStateManager().setStateOverlay(menuState,
	// null);
	// }
	// catch (StateException e)
	// {
	// Log.e(TAG, e.getMessage(), e);
	// }
	// }
	//
	// return true;
	// }
	//
	// return false;
	// }
	// };

	// private void playChannel(int index, long playTime, final long
	// playDuration)
	// {
	// _feature.Component.CHANNELS.play(index, playTime, playDuration);
	// _lastChannelIndex = index;
	// }
	//
	// private void playChannel(int index)
	// {
	// playChannel(index, System.currentTimeMillis() / 1000, 0);
	// }

	// TODO This disables the scrolling functionality specific to the touch
	// mode. It messes up the standard functionality.
	private boolean isTouchEnabled = false;

	private static class ChannelAdapter extends ArrayAdapter<Channel>
	{
		private final Context _context;
		private final int _layoutId;
		private final List<Channel> _channels;
		private final LayoutInflater _inflater;
		private final FeatureEPG _featureEPG;

		public ChannelAdapter(Context context, int layoutId, List<Channel> channels, FeatureEPG featureEPG)
		{
			super(context, -1, channels);
			_context = context;
			_layoutId = layoutId;
			_channels = channels;
			_featureEPG = featureEPG;
			_inflater = ((Activity) _context).getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View row = convertView;
			ChannelHolder holder = null;

			if (row == null)
			{
				row = _inflater.inflate(_layoutId, parent, false);
				holder = new ChannelHolder();
				holder.stationNo = (TextView) row.findViewById(R.id.stationNo);
				holder.stationLogo = (ImageView) row.findViewById(R.id.stationLogo);
				row.setTag(holder);
			}
			else
			{
				holder = (ChannelHolder) row.getTag();
			}

			if (position < _channels.size())
			{
				Channel channel = _channels.get(position);
				holder.stationNo.setText(String.valueOf(((ChannelBulsat) channel).getChannelNo()));
				Bitmap bmp = _featureEPG.getEpgData().getChannelLogoBitmap(channel.getIndex());
				holder.stationLogo.setImageBitmap(bmp);
			}
			return row;
		}

		private static class ChannelHolder
		{
			TextView stationNo;
			ImageView stationLogo;
		}
	}

	public static enum TVStateEnum
	{
		HIDDEN, VOLUME, SPOOLER, CHANNELS
	}

	private class TVStateManager
	{
		private TVStateEnum _currentStateEnum;
		private BaseTVState _currentState;
		private final BaseTVState _tvStateHidden;
		private final BaseTVState _tvStateVolume;
		private final BaseTVState _tvStateSpooler;
		private final BaseTVState _tvStateChannels;
		private final ViewGroup _root;

		private final ProgramView _programViewChannels;
		private final ProgramView _programViewSpooler;

		TVStateManager(ViewGroup root)
		{
			_root = root;
			_programViewChannels = new ProgramView((ViewGroup) _root.findViewById(R.id.tv_program_bar));
			_programViewSpooler = new ProgramView((ViewGroup) _root.findViewById(R.id.tv_program_bar));
			_tvStateHidden = createState(TVStateEnum.HIDDEN);
			_tvStateVolume = createState(TVStateEnum.VOLUME);
			_tvStateSpooler = createState(TVStateEnum.SPOOLER);
			_tvStateChannels = createState(TVStateEnum.CHANNELS);
		}

		void setState(TVStateEnum tvStateEnum)
		{
			Log.i(TAG, "TVStateManager.setState: tvStateEnum = " + tvStateEnum);
			if (_currentState != null)
				_currentState.setVisible(false);
			switch (tvStateEnum)
			{
				case HIDDEN:
					_currentState = _tvStateHidden;
				break;
				case VOLUME:
					_currentState = _tvStateVolume;
				break;
				case SPOOLER:
					_currentState = _tvStateSpooler;
				break;
				case CHANNELS:
					_currentState = _tvStateChannels;
				break;
			}
			_currentState.setVisible(true);
			_currentStateEnum = tvStateEnum;
		}

		BaseTVState getState(TVStateEnum tvStateEnum)
		{
			switch (tvStateEnum)
			{
				case HIDDEN:
					return _tvStateHidden;
				case VOLUME:
					return _tvStateVolume;
				case SPOOLER:
					return _tvStateSpooler;
				case CHANNELS:
					return _tvStateChannels;
			}
			return null;
		}

		boolean onKeyDown(AVKeyEvent keyEvent)
		{
			Log.i(TAG, "TVStateManager." + _currentState.getClass().getSimpleName() + ".onKeyDown: " + keyEvent);
			return _currentState.onKeyDown(keyEvent);
		}

		void onEvent(int eventId, Bundle bundle)
		{
			Log.i(TAG, "TVStateManager.onEvent: eventId = " + EventMessenger.idName(eventId));
			_currentState.onEvent(eventId, bundle);
		}

		TVStateEnum getCurrentStateEnum()
		{
			return _currentStateEnum;
		}

		private BaseTVState createState(TVStateEnum tvStateEnum)
		{
			switch (tvStateEnum)
			{
				case HIDDEN:
					return new TVStateHidden(_root);
				case VOLUME:
					return new TVStateVolume(_root);
				case SPOOLER:
					return new TVStateSpooler(_root, _programViewSpooler);
				case CHANNELS:
					return new TVStateChannels(_root, _programViewChannels);
			}
			return null;
		}
	}

	private void showChannels()
	{
		if (!TVStateEnum.CHANNELS.equals(_tvStateManager.getCurrentStateEnum()))
			_tvStateManager.setState(TVStateEnum.CHANNELS);
	}

	private abstract class BaseTVState
	{
		protected ProgramView _programView;
		protected ViewGroup _rootView;
		private final TextView _numView;
		protected TextView _tempView;
		protected ImageView _imgWeatherView;
		private int _maxChannelNo = -1;
		protected final ChannelSwitcher _channelSwitcher = new ChannelSwitcher();
		protected final ChannelSeeker _channelSeeker = new ChannelSeeker();

		protected final ChannelNumberHider _channelNumberHider = new ChannelNumberHider();

		private class ChannelSwitcher implements Runnable
		{
			private int _channelIndex;

			@Override
			public void run()
			{
				switchChannelIndex(_channelIndex);
			}
		}

		private class ChannelSeeker implements Runnable
		{
			private int _channelIndex;
			private long _playTime;

			@Override
			public void run()
			{
				playChannel(_channelIndex, _playTime, 0);
			}
		}

		private class ChannelNumberHider implements Runnable
		{
			@Override
			public void run()
			{
				_numView.setText(null);
				_numView.setVisibility(View.INVISIBLE);
			}
		}

		private void switchChannelIndex(int channelIndex)
		{
			ChannelBulsat channel = (ChannelBulsat) _feature.Component.CHANNELS.getActiveChannels().get(channelIndex);
			Log.i(TAG, ".switchChannelIndex: channelIndex = " + channelIndex);

			// Remember from which channel list we started the channel
			// TVStateChannels stateChannels = (TVStateChannels)
			// _tvStateManager.getState(TVStateEnum.CHANNELS);
			// Environment.getInstance().getUserPrefs().put(UserParam.IS_LAST_FAVORITES,
			// stateChannels.isFavorites());
			playChannel(channelIndex);
		}

		BaseTVState(ViewGroup rootView, ProgramView programView)
		{
			_rootView = rootView;
			_programView = programView;
			_numView = (TextView) _rootView.findViewById(R.id.rcu_channel_selection);
		}

		protected void onEvent(int msgId, Bundle bundle)
		{
			if (msgId == ON_NUM_TIMEOUT)
			{
				int channelIndex = getChannelIndexByNumber(_channelNumber);
				Log.i(TAG, ".ON_NUM_TIMEOUT: _channelNumber = " + _channelNumber + ", channelIndex = " + channelIndex);

				TVStateChannels stateChannels = (TVStateChannels) _tvStateManager.getState(TVStateEnum.CHANNELS);

				List<Channel> channels = stateChannels.getChannels();
				if (channelIndex >= 0 && channelIndex < channels.size())
				{
					Channel channel = channels.get(channelIndex);
					_channelSwitcher._channelIndex = channel.getIndex();
					getEventMessenger().removeCallbacks(_channelSwitcher);
					getEventMessenger().postDelayed(_channelSwitcher, 50);

					showChannels();
					stateChannels.selectChannelIndex(channelIndex);
				}
				_numView.setVisibility(View.INVISIBLE);
				_numView.setText(null);
				_channelNumber = 0;
			}
			else if (msgId == ON_OSD_AUTOHIDE)
			{
				if (!TVStateEnum.CHANNELS.equals(_tvStateManager.getCurrentStateEnum()))
					_tvStateManager.setState(TVStateEnum.HIDDEN);
			}
			else if (msgId == ON_TIMER)
			{
				getEventMessenger().trigger(ON_TIMER, 1000);
			}
		}

		boolean onKeyDown(AVKeyEvent keyEvent)
		{
			if (keyEvent.Event.getKeyCode() >= KeyEvent.KEYCODE_0 && keyEvent.Event.getKeyCode() <= KeyEvent.KEYCODE_9)
			{
				onDigitPressed(keyEvent.Event.getKeyCode() - KeyEvent.KEYCODE_0);
				return true;
			}
			else if (Key.BACK.equals(keyEvent.Code))
			{
				_tvStateManager.setState(TVStateEnum.HIDDEN);
				return true;
			}

			return false;
		}

		abstract void setVisible(boolean isVisible);

		/**
		 * @return the maximum number of channel depending on current channels
		 *         set
		 */
		private int getMaxChannelNumber()
		{
			TVStateChannels stateChannels = (TVStateChannels) _tvStateManager.getState(TVStateEnum.CHANNELS);

			if (_maxChannelNo < 0)
			{
				// compute maximum channel number in all channels
				_maxChannelNo = 0;
				List<Channel> channels = _feature.Component.CHANNELS.getActiveChannels();
				for (int i = 0; i < channels.size(); i++)
				{
					ChannelBulsat channel = (ChannelBulsat) channels.get(i);
					if (channel.getChannelNo() > _maxChannelNo)
						_maxChannelNo = channel.getChannelNo();
				}
			}
			return _maxChannelNo;
		}

		/**
		 * restart OSD auto hide timeout
		 */
		abstract void restartOSDTimeout();

		protected void stopOSDTimeout()
		{
			getEventMessenger().removeMessages(ON_OSD_AUTOHIDE);
		}

		/**
		 * Returns channel index by number
		 *
		 * @param channelNo
		 *            the number of channel as specified by the EPG provider
		 * @return channel index in the array of active channels
		 *         corresponding to the specified number
		 */
		protected int getChannelIndexByNumber(int channelNo)
		{
			TVStateChannels stateChannels = (TVStateChannels) _tvStateManager.getState(TVStateEnum.CHANNELS);
			if (_channelNumberToIndex == null)
			{
				_channelNumberToIndex = new SparseIntArray(200);

				List<Channel> activeChannels = _feature.Component.CHANNELS.getActiveChannels();
				for (int i = 0; i < activeChannels.size(); i++)
				{
					ChannelBulsat bchannel = (ChannelBulsat) activeChannels.get(i);
					_channelNumberToIndex.put(bchannel.getChannelNo(), bchannel.getIndex() + 1);
				}
			}
			Log.d(TAG, ".getChannelIndexByNumber: " + channelNo + " -> " + (_channelNumberToIndex.get(channelNo) - 1));
			return _channelNumberToIndex.get(channelNo) - 1;
		}

		protected void onDigitPressed(int digit)
		{
			Log.i(TAG, ".onDigitPressed: digit = " + digit + ", old _channelNumber = " + _channelNumber);
			_channelNumber = _channelNumber * 10 + digit;
			_numView.setText(String.valueOf(_channelNumber));
			getEventMessenger().removeMessages(ON_NUM_TIMEOUT);
			int maxChannelNumber = getMaxChannelNumber();
			showChannels();

			TVStateChannels stateChannels = (TVStateChannels) _tvStateManager.getState(TVStateEnum.CHANNELS);

			int index = -1;
			if (_channelNumber < 10)
			{
				index = getChannelIndexByNumber(_channelNumber * 100 + 1);
			}
			else if (_channelNumber < 100)
			{
				if (digit == 0)
					index = getChannelIndexByNumber(_channelNumber * 10 + 1);
				else
					index = getChannelIndexByNumber(_channelNumber * 10);
			}

			if (index != -1)
				stateChannels.selectChannelIndex(index);

			if (_channelNumber > 100 || _channelNumber * 10 > maxChannelNumber)
			{
				Log.i(TAG, ".onDigitPressed: " + digit + " -> " + _channelNumber + ", trigger ON_NUM_TIMEOUT");
				// switch to selected channel number
				getEventMessenger().trigger(ON_NUM_TIMEOUT);
			}
			else
			{
				Log.i(TAG, ".onDigitPressed: " + digit + " -> " + _channelNumber + ", trigger ON_NUM_TIMEOUT delayed");
				if (_channelNumber > 0)
				{
					_numView.setVisibility(View.VISIBLE);
					getEventMessenger().trigger(ON_NUM_TIMEOUT, getPrefs().getInt(Param.NUM_AUTOHIDE) * 1000);
				}
				else
					_numView.setVisibility(View.INVISIBLE);
			}
		}
	}

	private class TVStateSpooler extends BaseTVState
	{
		private final ViewGroup _programSpoolContainerView;
		private final ViewGroup _selectedChannelView;
		private final TextView _channelNoTextView;
		private final ImageView _channelLogoImageView;
		protected ImageView _selectedChannelShaddow;
//		private final ImageView _tvSpooler;
//		private final ImageView _play;
//		private final ImageView _pause;
//		private TextView _replayOrRecord;

		TVStateSpooler(ViewGroup rootView, ProgramView programView)
		{
			super(rootView, programView);
			_programSpoolContainerView = (ViewGroup) _rootView.findViewById(R.id.tv_program_bar);
			_selectedChannelView = (ViewGroup) _rootView.findViewById(R.id.channel_info_container);
			_channelNoTextView = (TextView) _selectedChannelView.findViewById(R.id.channel_no);
			_channelLogoImageView = (ImageView) _selectedChannelView.findViewById(R.id.channel_logo);
			_selectedChannelShaddow = (ImageView) _programSpoolContainerView.findViewById(R.id.channel_indicator);
//			_tvSpooler = (ImageView) _rootView.findViewById(R.id.tv_spooler);
//			_play = (ImageView) _rootView.findViewById(R.id.ic_play);
//			_pause = (ImageView) _rootView.findViewById(R.id.ic_pause);
//			_replayOrRecord = (TextView) _programSpoolContainerView.findViewById(R.id.replay);
		}

		@Override
		protected void onEvent(int msgId, Bundle bundle)
		{
			super.onEvent(msgId, bundle);
			if (msgId == ON_TIMER)
			{
				updateProgramBar();
				updateReplay();
			}
			else if (msgId == FeaturePlayer.ON_PLAY_PAUSE)
			{
				if (_feature.Component.PLAYER.isPaused())
				{
					_programView.TimeshiftCursor.setImageLevel(1);
					stopOSDTimeout();
				}
				else
				{
					_programView.TimeshiftCursor.setImageLevel(0);
					restartOSDTimeout();
				}
				updatePlayPause();
			}
			else if (msgId == FeatureTimeshift.ON_SEEK)
			{
				updateReplay();

				// ON_SEEK is sent also on timeshift duration change
				// we must disable pause in case timeshift buffer is set to 0
				updatePlayPause();
			}
		}

		@Override
		protected boolean onKeyDown(AVKeyEvent keyEvent)
		{
			if (super.onKeyDown(keyEvent))
				return true;

			switch (keyEvent.Code)
			{
				case DOWN:
					seekRel(getPrefs().getInt(Param.SEEK_LONG_BACKWARD));
					return true;
				case UP:
					seekRel(getPrefs().getInt(Param.SEEK_LONG_FORWARD));
					return true;
				case LEFT:
				{
					seekRel(getPrefs().getInt(Param.SEEK_SHORT_BACKWARD));
					return true;
				}
				case RIGHT:
				{
					seekRel(getPrefs().getInt(Param.SEEK_SHORT_FORWARD));
					return true;
				}
				case OK:
				{
					if (_feature.Component.PLAYER.isPaused())
						_feature.Component.PLAYER.resume();
					else
						_feature.Component.PLAYER.pause();
					return true;
				}
				default:
			}
			return super.onKeyDown(keyEvent);
		}

		@Override
		protected void restartOSDTimeout()
		{
			stopOSDTimeout();
			getEventMessenger().trigger(ON_OSD_AUTOHIDE, getPrefs().getInt(Param.OSD_AUTOHIDE_VOLUME) * 1000);
		}

		private void seekRel(int secs)
		{
			if (_feature.Component.TIMESHIFT.getTimeshiftDuration() == 0)
				return;
			long playTime = _feature.Component.TIMESHIFT.seekRel(secs);

			_channelSeeker._playTime = playTime;
			_channelSeeker._channelIndex = _lastChannelIndex;
			getEventMessenger().removeCallbacks(_channelSeeker);
			getEventMessenger().postDelayed(_channelSeeker, 200);
			updateProgramBar();
			restartOSDTimeout();
		}

		private void updateReplay()
		{
			Calendar dateCal = Calendar.getInstance(_feature.Component.TIMEZONE.getTimeZone());
			dateCal.setTimeInMillis(1000 * _feature.Component.TIMESHIFT.getPlayingTime());
			int dayOffset = Calendars.getDayOffsetByDate(dateCal);
			String dateStr;
			if (dayOffset == 0)
				dateStr = Calendars.makeHHMMSSString(dateCal);
			else if (dayOffset == -1)
				dateStr = getResources().getString(R.string.yesterday) + " " + Calendars.makeHHMMSSString(dateCal);
			else
				dateStr = Calendars.makeString(dateCal);

			Log.d(TAG, ".updateReply: since " + dateStr);
//			String replyText = getResources().getString(R.string.replay, dateStr);
//			_replayOrRecord.setText(replyText);

//			long delta = _feature.Component.TIMEZONE.getCurrentTime().getTimeInMillis() - 1000
//			        * _feature.Component.TIMESHIFT.getPlayingTime();
//			if (delta > 1000 * getPrefs().getInt(Param.SEEK_SHORT_FORWARD) / 2)
//				_replayOrRecord.setVisibility(View.VISIBLE);
//			else
//				_replayOrRecord.setVisibility(View.INVISIBLE);
		}

		@Override
		void setVisible(boolean isVisible)
		{
			_programSpoolContainerView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
			_selectedChannelView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
			_selectedChannelShaddow.setVisibility(View.INVISIBLE);
//			_tvSpooler.setVisibility(View.INVISIBLE);
//			_play.setVisibility(View.INVISIBLE);
//			_pause.setVisibility(View.INVISIBLE);
			if (isVisible)
			{
				int channelIndex = _lastChannelIndex;
				ChannelBulsat channel = (ChannelBulsat) _feature.Component.CHANNELS.getActiveChannels().get(
				        channelIndex);
				TVStateChannels tvStateChannels = (TVStateChannels) _tvStateManager.getState(TVStateEnum.CHANNELS);

				_channelNoTextView.setText(channel.getChannelNo() + "");

				_channelLogoImageView.setImageBitmap(_feature.Scheduler.EPG.getEpgData().getChannelLogoBitmap(
				        channel.getIndex(), IEpgDataProvider.ChannelLogoType.SELECTED));
				updateProgramBar();
				if (_feature.Component.PLAYER.isPaused())
					_programView.TimeshiftCursor.setImageLevel(1);
				else
					_programView.TimeshiftCursor.setImageLevel(0);

				restartOSDTimeout();
				updateReplay();
				updatePlayPause();
				updateProgramBar();
			}
		}

		private void updateProgramBar()
		{
			_programView.update(_lastChannelIndex);
		}

		protected void updatePlayPause()
		{
//			if (_feature.Component.PLAYER.isPaused())
//			{
//				_play.setImageLevel(0);
//				_pause.setImageLevel(1);
//			}
//			else
//			{
//				_play.setImageLevel(1);
//				_pause.setImageLevel(0);
//			}

//			if (_feature.Component.TIMESHIFT.getTimeshiftDuration() > 0)
//			{
//				_tvSpooler.setVisibility(View.VISIBLE);
//				_play.setVisibility(View.VISIBLE);
//				_pause.setVisibility(View.VISIBLE);
//			}
//			else
//			{
//				_tvSpooler.setVisibility(View.INVISIBLE);
//				_play.setVisibility(View.INVISIBLE);
//				_pause.setVisibility(View.INVISIBLE);
//			}
		}
	}

	private class TVStateHidden extends BaseTVState
	{
		protected final TextView _numView;

		TVStateHidden(ViewGroup rootView)
		{
			super(rootView, null);
			_numView = (TextView) _rootView.findViewById(R.id.rcu_channel_selection);
		}

		@Override
		protected void onEvent(int msgId, Bundle bundle)
		{
			super.onEvent(msgId, bundle);
			if (msgId == FeaturePlayer.ON_PLAY_PAUSE)
			{
				if (_feature.Component.PLAYER.isPaused())
				{
					showChannels();

					// delegate same event to channels state
					_tvStateManager.onEvent(msgId, bundle);
				}
			}
		}

		@Override
		boolean onKeyDown(AVKeyEvent keyEvent)
		{
			if (super.onKeyDown(keyEvent))
				return true;
			TVStateChannels stateChannels = (TVStateChannels) _tvStateManager.getState(TVStateEnum.CHANNELS);
			switch (keyEvent.Code)
			{
				case LEFT:
					_feature.Component.VOLUME.lower();
					_tvStateManager.setState(TVStateEnum.VOLUME);
					restartOSDTimeout();
				break;
				case RIGHT:
					_feature.Component.VOLUME.raise();
					_tvStateManager.setState(TVStateEnum.VOLUME);
					restartOSDTimeout();
				break;

				case UP:
				case DOWN:
					// switches to next channel immediately
					// List<Channel> channels = stateChannels.getChannels();
					// int lastChannelIndex =
					// stateChannels.getLastChannelIndex();
					// // computes next channel index
					// int channelIndex = Key.UP.equals(keyEvent.Code) ?
					// lastChannelIndex + 1 : lastChannelIndex - 1;
					// if (channelIndex < 0)
					// channelIndex = channels.size() - 1;
					// else if (channelIndex > channels.size() - 1)
					// channelIndex = 0;
					//
					// Log.i(TAG, "Switching to channelIndex = " + channelIndex
					// + ", lastChannelIndex = "
					// + lastChannelIndex);
					//
					// if (channelIndex >= 0 && channelIndex < channels.size())
					// {
					// ChannelBulsat channel = (ChannelBulsat)
					// channels.get(channelIndex);
					// //
					// _feature.Component.CHANNELS.setLastChannelId(channel.getChannelId());
					// _lastChannelIndex = channel.getIndex();
					// _channelSwitcher._channelIndex = channel.getIndex();
					// getEventMessenger().removeCallbacks(_channelSwitcher);
					// getEventMessenger().postDelayed(_channelSwitcher, 50);
					// _numView.setVisibility(View.VISIBLE);
					// _numView.setText(String.valueOf(channel.getChannelNo()));
					// getEventMessenger().removeCallbacks(_channelNumberHider);
					// getEventMessenger().postDelayed(_channelNumberHider,
					// getPrefs().getInt(Param.NUM_AUTOHIDE) * 1000);
					// }
					showChannels();
					keyEvent.Event.startTracking();
					if (!Key.UP.equals(keyEvent.Code) || !Key.DOWN.equals(keyEvent.Code))
						return _tvStateManager.onKeyDown(keyEvent);
				break;

				case OK:
					showChannels();
					keyEvent.Event.startTracking();
					if (!Key.OK.equals(keyEvent.Code))
						return _tvStateManager.onKeyDown(keyEvent);
				break;

				default:
			}
			return super.onKeyDown(keyEvent);
		}

		@Override
		void setVisible(boolean isVisible)
		{

		}

		@Override
		void restartOSDTimeout()
		{
			// nothing to hide
		}
	}

	private class TVStateVolume extends TVStateHidden
	{
		// private final TextView _volumeDayOfMonth;
		// private final TextView _volumeTimeOfDay;
		// private final ProgressBar _volumeProgress;
		// private final View _volumeContainer;

		TVStateVolume(ViewGroup rootView)
		{
			super(rootView);
			// _volumeDayOfMonth = (TextView)
			// _rootView.findViewById(R.id.vol_day_of_month);
			// _volumeTimeOfDay = (TextView)
			// _rootView.findViewById(R.id.vol_time_of_day);
			// _volumeProgress = (ProgressBar)
			// _rootView.findViewById(R.id.volume_bar);
			// _volumeContainer = _rootView.findViewById(R.id.volume_container);
		}

		@Override
		void setVisible(boolean isVisible)
		{
			// TODO Auto-generated method stub

		}

		@Override
		void restartOSDTimeout()
		{
			// TODO Auto-generated method stub

		}
	}

	private class TVStateChannels extends BaseTVState
	{
		protected ZapperList _zapperList;
		protected ViewGroup _selectedChannelView;
		protected ViewGroup _programContainerView;
		protected TextView _channelNoTextView;
		protected ImageView _channelLogoImageView;
		protected ImageView _selectedChannelShaddow;

		// private final ImageView _play;
		// private final ImageView _pause;

		TVStateChannels(ViewGroup rootView, ProgramView programView)
		{
			super(rootView, programView);
			_zapperList = (ZapperList) _rootView.findViewById(R.id.tv_channel_bar);
			_selectedChannelView = (ViewGroup) _rootView.findViewById(R.id.channel_info_container);
			_programContainerView = (ViewGroup) _rootView.findViewById(R.id.tv_program_bar);
			_channelNoTextView = (TextView) _selectedChannelView.findViewById(R.id.channel_no);
			_channelLogoImageView = (ImageView) _selectedChannelView.findViewById(R.id.channel_logo);

			_selectedChannelShaddow = (ImageView) _programContainerView.findViewById(R.id.channel_indicator);
			// _play = (ImageView)
			// _programContainerView.findViewById(R.id.ic_play);
			// _pause = (ImageView)
			// _programContainerView.findViewById(R.id.ic_pause);

			int channelIndex = _lastChannelIndex;
			if (channelIndex < 0)
				channelIndex = 0;

			ChannelBulsat channel = (ChannelBulsat) _feature.Component.CHANNELS.getActiveChannels().get(channelIndex);

			updateChannelBarChannels(channelIndex);
			updateChannel(channelIndex);
			setVisible(false);
		}

		@Override
		void setVisible(boolean isVisible)
		{
			_zapperList.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
			_selectedChannelView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
			_selectedChannelShaddow.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
			_programContainerView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
			// _play.setVisibility(View.INVISIBLE);
			// _pause.setVisibility(View.INVISIBLE);

			if (isVisible)
			{
				// the index in user or all channels list depending if we
				// are in favorites mode or not
				int channelIndex = getLastChannelIndex();

				// reset index to 1st channel if index is not found in the
				// list
				if (channelIndex < 0)
					channelIndex = 0;

				updateChannelBarChannels(channelIndex);
				Log.i(TAG, ".setVisible:updateChannel: channelIndex = " + channelIndex);

				updateChannel(channelIndex);
				selectChannelIndex(channelIndex);

				if (_feature.Component.PLAYER.isPaused())
					_programView.TimeshiftCursor.setImageLevel(1);
				else
					_programView.TimeshiftCursor.setImageLevel(0);
				restartOSDTimeout();
				updateProgramBar();
			}
		}

		protected List<Channel> getChannels()
		{
			return _feature.Component.CHANNELS.getActiveChannels();
		}

		@Override
		protected void onEvent(int msgId, Bundle bundle)
		{
			super.onEvent(msgId, bundle);
			if (msgId == ON_TIMER)
			{
				updateProgramBar();
			}
			else if (msgId == FeaturePlayer.ON_PLAY_PAUSE)
			{
				if (_feature.Component.PLAYER.isPaused())
				{
					_programView.TimeshiftCursor.setImageLevel(1);
					stopOSDTimeout();
				}
				else
				{
					_programView.TimeshiftCursor.setImageLevel(0);
					restartOSDTimeout();
				}
				updateProgramBar();
			}
		}

		@Override
		protected boolean onKeyDown(AVKeyEvent keyEvent)
		{
			if (super.onKeyDown(keyEvent))
				return true;

			switch (keyEvent.Code)
			{
				case UP:
				case DOWN:
					// move up or down to the channel bar
					// _zapperList.onKeyDown(keyEvent);
					_zapperList.onKeyDown(keyEvent.Event.getKeyCode(), keyEvent.Event);
					selectChannelIndex(_zapperList.getPosition());
					return true;

				case LEFT:
				case RIGHT:
				{
					restartOSDTimeout();
					return true;
				}
				case OK:
					if (keyEvent.Event.getRepeatCount() > 0)
					{
						// ignore OK button if sent by key repetition
						return true;
					}

					// switch the new selected channel immediately

					List<Channel> channels = getChannels();
					int channelIndex = _zapperList.getPosition();
					if (channelIndex >= 0 && channelIndex < channels.size())
					{
						Channel channel = channels.get(channelIndex);
						if (getLastChannelIndex() != channelIndex)
						{
							_lastChannelIndex = channel.getIndex();
							_channelSwitcher._channelIndex = channel.getIndex();
							getEventMessenger().removeCallbacks(_channelSwitcher);
							getEventMessenger().postDelayed(_channelSwitcher, 50);

							// force timeshift to live immediately to avoid
							// seeking position change one second later
							_feature.Component.TIMESHIFT.seekLive();
							updateChannel(channelIndex);
						}
						_tvStateManager.setState(TVStateEnum.SPOOLER);
					}
					else
					{
						Log.w(TAG,
						        ".onKeyDown OK: channel index " + channelIndex + " exceeds range [0:" + channels.size()
						                + "]");
					}
				default:
			}
			return super.onKeyDown(keyEvent);
		}

		@Override
		void restartOSDTimeout()
		{
			// will not hide channel bar
		}

		void selectChannelIndex(int channelIndex)
		{
			Log.i(TAG, ".selectChannelIndex: channelIndex = " + channelIndex);
			// Update selected channel number and logo
			List<Channel> channels = getChannels();
			if (channelIndex >= 0 && channelIndex < channels.size())
			{
				if (channelIndex != _zapperList.getPosition())
					_zapperList.setPosition(channelIndex, false);
				ChannelBulsat channel = updateChannel(channelIndex);
			}
		}

		// returns last channel index in user channels list
		protected int getLastChannelIndex()
		{
			return _lastChannelIndex;
		}

		protected ChannelBulsat updateChannel(int channelIndex)
		{
			Log.d(TAG, ".updateChannel: channelIndex = " + channelIndex);
			List<Channel> channels = getChannels();
			if (channelIndex < 0 || channelIndex >= channels.size())
			{
				Log.w(TAG, ".updateChannel: channel index " + channelIndex + " exceeds range [0:" + channels.size()
				        + "]");
				return null;
			}

			updateProgramBar();
			ChannelBulsat channel = (ChannelBulsat) channels.get(channelIndex);

			// update channel number and logo
			_channelNoTextView.setText(channel.getChannelNo() + "");
			_channelLogoImageView.setImageBitmap(_feature.Scheduler.EPG.getEpgData().getChannelLogoBitmap(
			        channel.getIndex(), IEpgDataProvider.ChannelLogoType.SELECTED));

			return channel;
		}

		void updateChannelBarChannels(int channelIndex)
		{
			_zapperList.setAdapter(new ChannelAdapter(getActivity(), R.layout.tv_channel_item, getChannels(),
			        _feature.Scheduler.EPG), channelIndex);
		}

		private void updateProgramBar()
		{
			int channelIndex = _zapperList.getPosition();
			List<Channel> channels = getChannels();
			if (channels.size() == 0)
			{
				Log.w(TAG, ".updateProgramBar: channels list is empty");
				return;
			}
			if (channelIndex < 0 || channelIndex >= channels.size())
			{
				channelIndex = getLastChannelIndex();
				if (channelIndex < 0)
					channelIndex = 0;
			}
			Channel channel = getChannels().get(channelIndex);
			_programView.update(channel.getIndex());
			// updatePlayPause((ChannelBulsat) channel);
		}

	}

	private class ProgramView
	{
		private final TextView prevProgramTime;
		private final TextView prevProgramTitle;
		private final TextView currProgramTime;
		private final TextView currProgramTitle;
		private final TextView nextProgramTime;
		private final TextView nextProgramTitle;
		private final ProgressBar ProgramProgress;
		private final ImageView TimeshiftCursor;
		private final TextView ProgramTimeStart;
		private final TextView ProgramTimeEnd;
		private final TextView ChannelTitle;

		// private final TextView TimeOfDay;

		// private final TextView DayOfMonth;

		ProgramView(ViewGroup viewGroup)
		{
			// TimeOfDay = (TextView) viewGroup.findViewById(R.id.clock);
			// DayOfMonth = (TextView)
			// viewGroup.findViewById(R.id.day_of_month);

			viewGroup = (ViewGroup) viewGroup.findViewById(R.id.tv_program_bar);
			ChannelTitle = (TextView) viewGroup.findViewById(R.id.channel_title);
			ProgramTimeStart = (TextView) viewGroup.findViewById(R.id.program_start);
			ProgramTimeEnd = (TextView) viewGroup.findViewById(R.id.program_end);
			nextProgramTime = (TextView) viewGroup.findViewById(R.id.next_program_time);
			nextProgramTitle = (TextView) viewGroup.findViewById(R.id.next_program_title);
			currProgramTime = (TextView) viewGroup.findViewById(R.id.current_program_time);
			currProgramTitle = (TextView) viewGroup.findViewById(R.id.current_program_title);
			prevProgramTime = (TextView) viewGroup.findViewById(R.id.previous_program_time);
			prevProgramTitle = (TextView) viewGroup.findViewById(R.id.previous_program_title);
			ProgramProgress = (ProgressBar) viewGroup.findViewById(R.id.program_progress);
			ProgramProgress.setMax(1000);
			TimeshiftCursor = (ImageView) viewGroup.findViewById(R.id.timeshift_cursor);
			if (_feature.Component.PLAYER.isPaused())
				TimeshiftCursor.setImageLevel(1);
			else
				TimeshiftCursor.setImageLevel(0);
		}

		private void updatePrograms(String channelId, Program currProgram)
		{
			Program prevProgram = null;
			Program nextProgram = null;
			if (currProgram != null)
			{
				prevProgram = _feature.Scheduler.EPG.getEpgData().getProgramByOffset(channelId,
				        currProgram.getStartTime(), -1);
				nextProgram = _feature.Scheduler.EPG.getEpgData().getProgramByOffset(channelId,
				        currProgram.getStartTime(), 1);
			}

			setPreviousProgram(channelId, prevProgram);
			setCurrentProgram(channelId, currProgram);
			setNextProgram(channelId, nextProgram);
		}

		void update(int channelIndex)
		{
			Log.i(TAG, "ProgramView.update: channelIndex = " + channelIndex + ", lastChannelIndex = "
			        + _lastChannelIndex);
			int nChannels = _feature.Component.CHANNELS.getActiveChannels().size();
			if (channelIndex < 0 || channelIndex >= nChannels)
			{
				Log.w(TAG, "Current channel index " + channelIndex + " is out of range active channels [0:" + nChannels
				        + "]");
				return;
			}

			Calendar currentTime = _feature.Component.TIMEZONE.getCurrentTime();
			Calendar playTime = _feature.Component.TIMEZONE.getCurrentTime();

			FeatureTimeshift timeshift = _feature.Component.TIMESHIFT;
			boolean inTimeshift = (channelIndex == _lastChannelIndex);
			if (inTimeshift)
			{
				// shift in time the current playing channel
				playTime.setTimeInMillis(1000 * timeshift.getPlayingTime());
			}

			// updates current playing time
			// TimeOfDay.setText(TIME_FORMAT.format(currentTime.getTime()));
			// DayOfMonth.setText(formatDayOfMonth(currentTime.getTime()));

			Channel channel = _feature.Component.CHANNELS.getActiveChannels().get(channelIndex);
			ChannelTitle.setText(channel.getTitle());

			String channelId = channel.getChannelId();
			Program currProgram = _feature.Scheduler.EPG.getEpgData().getProgram(channelId, playTime);

			// hide cursor now and show it later if necessary
			TimeshiftCursor.setVisibility(View.INVISIBLE);

			if (currProgram == null)
			{
				ProgramProgress.setVisibility(View.INVISIBLE);
				ProgramTimeStart.setVisibility(View.INVISIBLE);
				ProgramTimeEnd.setVisibility(View.INVISIBLE);
				updatePrograms(channelId, currProgram);
				return;
			}
			else
			{
				ProgramProgress.setVisibility(View.VISIBLE);
				ProgramTimeStart.setVisibility(View.VISIBLE);
				ProgramTimeStart.setText(parseDateTimeToHourMins(currProgram.getStartTime()));
				ProgramTimeEnd.setVisibility(View.VISIBLE);
				ProgramTimeEnd.setText(parseDateTimeToHourMins(currProgram.getStopTime()));
			}

			updatePrograms(channelId, currProgram);

			if (inTimeshift)
			{
				// update timeshift progress
				Calendar timeshiftStartCalendar = _feature.Component.TIMEZONE.getCurrentTime();
				long timeshiftStart = timeshift.currentTime() - timeshift.getTimeshiftDuration();
				timeshiftStartCalendar.setTimeInMillis(1000 * timeshiftStart);

				// show as a secondary progress the empty buffer
				// check if beginning of the buffer is inside current program
				if (currProgram.getStartTime().before(timeshiftStartCalendar)
				        && currProgram.getStopTime().after(timeshiftStartCalendar))
				{
					// show as a secondary progress the empty buffer from
					// program start to timeshift start
					float timeshiftStartFragment = getProgressInProgram(currProgram, timeshiftStart);
					ProgramProgress.setSecondaryProgress((int) (1000 * timeshiftStartFragment));
				}
				// is whole current program outside of the buffer
				else if (timeshiftStartCalendar.after(currProgram.getStopTime())
				        || currProgram.getStartTime().after(currentTime))
				{
					// show as a secondary progress the whole area
					ProgramProgress.setSecondaryProgress(1000);
				}
				else
				{
					// no empty buffer visible
					ProgramProgress.setSecondaryProgress(0);
				}

				// show timeshift buffer fragment
				float currentTimeFragment = getProgressInProgram(currProgram, currentTime.getTimeInMillis() / 1000);
				ProgramProgress.setProgress((int) (1000 * currentTimeFragment));

				float cursorFragment = getProgressInProgram(currProgram, playTime.getTimeInMillis() / 1000);
				int progressBarWidth = ProgramProgress.getWidth();
				int offset = (int) (progressBarWidth * cursorFragment) - TimeshiftCursor.getWidth() / 2;
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) TimeshiftCursor.getLayoutParams();
				params.setMargins(offset, 0, 0, 0);
				TimeshiftCursor.setLayoutParams(params);
				// show cursor
				TimeshiftCursor.setVisibility(View.VISIBLE);
			}
			else
			{
				ProgramProgress.setSecondaryProgress(0);
				float currentTimeFragment = getProgressInProgram(currProgram, currentTime.getTimeInMillis() / 1000);
				ProgramProgress.setProgress((int) (1000 * currentTimeFragment));
			}
		}

		private float getProgressInProgram(Program program, long timestamp)
		{
			long elapsed = timestamp - program.getStartTime().getTimeInMillis() / 1000;
			long total = (program.getStopTime().getTimeInMillis() - program.getStartTime().getTimeInMillis()) / 1000;
			float fragment = (float) elapsed / total;
			if (fragment < 0)
				fragment = 0;
			else if (fragment > 1.0f)
				fragment = 1.0f;
			return fragment;
		}

		private String parseDateTimeToHourMins(Calendar dateTime)
		{
			return TIME_FORMAT.format(dateTime.getTime());
		}

		private void setPreviousProgram(String channelId, Program program)
		{
			setProgramToView(channelId, program, prevProgramTime, prevProgramTitle);
		}

		private void setCurrentProgram(String channelId, Program program)
		{
			setProgramToView(channelId, program, currProgramTime, currProgramTitle);
		}

		private void setNextProgram(String channelId, Program program)
		{
			setProgramToView(channelId, program, nextProgramTime, nextProgramTitle);
		}

		private void setProgramToView(String channelId, Program program, TextView programTime, TextView programTitle)
		{
			if (program == null)
			{
				programTime.setText(null);
				programTitle.setText(getResources().getString(R.string.no_epg_info));
			}
			else
			{
				programTime.setText(parseDateTimeToHourMins(program.getStartTime()));
				programTitle.setText(program.getTitle());
			}
		}
	}

}
