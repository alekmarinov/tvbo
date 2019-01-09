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
import com.aviq.tv.android.sdk.core.feature.FeatureError;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.service.ServiceController.OnResultReceived;
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
	private TVStateManager _tvStateManager;
	private int _channelNumber = 0;
	private SparseIntArray _channelNumberToIndex;
	private String _lastChannelId;

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
		 * Delay in seconds to auto hide OSD
		 */
		OSD_AUTOHIDE(8),

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

	public FeatureStateTV() throws FeatureNotFoundException
	{
		require(FeatureName.Component.EPG);
		require(FeatureName.Component.CHANNELS);
		require(FeatureName.Component.PLAYER);
		require(FeatureName.Component.LANGUAGE);
		require(FeatureName.State.MENU);
		require(FeatureName.Component.TIMEZONE);
		require(FeatureName.Component.TIMESHIFT);
		require(FeatureName.Component.RCU);
		require(FeatureName.Component.VOLUME);
		require(FeatureName.State.ERROR);
		require(FeatureName.State.MESSAGE_BOX);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		subscribe(this, ON_TIMER);
		subscribe(this, ON_OSD_AUTOHIDE);
		subscribe(this, ON_NUM_TIMEOUT);

		subscribe(_feature.Component.PLAYER, FeaturePlayer.ON_PLAY_PAUSE);
		subscribe(_feature.Component.PLAYER, FeaturePlayer.ON_PLAY_ERROR);
		subscribe(_feature.Component.TIMESHIFT, FeatureTimeshift.ON_SEEK);
		subscribe(Environment.getInstance().getEventMessenger(), Environment.ON_RESUME);

		_feature.Component.RCU.getEventMessenger().register(this, FeatureRCU.ON_KEY_PRESSED);

		FeatureStateMenu featureStateMenu = (FeatureStateMenu) Environment.getInstance().getFeatureState(
		        FeatureName.State.MENU);
		featureStateMenu.addMenuItemState(this);

		TIME_FORMAT = new SimpleDateFormat("HH:mm", _feature.Component.LANGUAGE.getLocale());
		TIME_FORMAT.setTimeZone(_feature.Component.TIMEZONE.getTimeZone());
		_lastChannelId = _feature.Component.CHANNELS.getLastChannelId();

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
		Log.i(TAG, ".onCreateView");

		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_tv, container, false);

		_tvStateManager = new TVStateManager(viewGroup);

		Bundle params = getArguments();
		if (params == null)
		{
			if (!_feature.Component.PLAYER.isPlaying())
			{
				playLastChannel();
			}
		}
		else
		{
			int channelIndex = params.getInt(Extras.CHANNEL_INDEX.name(), -1);
			if (channelIndex == -1)
			{
				playLastChannel();
			}
			else
			{
				long playTime = params.getLong(Extras.PLAY_TIME.name(), 0);
				long playDuration = params.getLong(Extras.PLAY_DURATION.name(), 0);
				playChannel(channelIndex, playTime, playDuration);
			}
		}

		return viewGroup;
	}

	@Override
	public void onShow(boolean isViewUncovered)
	{
		super.onShow(isViewUncovered);
		Log.i(TAG, ".onShow: isViewUncovered = " + isViewUncovered);

		_tvStateManager.setState(TVStateEnum.SPOOLER);

		_feature.Component.PLAYER.setFullScreen();
		if (!isViewUncovered)
		{
			getEventMessenger().removeMessages(ON_TIMER);
			getEventMessenger().trigger(ON_TIMER, 1000);
			// update replay/record text
			_feature.Component.TIMESHIFT.getEventMessenger().trigger(FeatureTimeshift.ON_SEEK);
		}

		if (!_feature.Component.PLAYER.isPlaying())
			playLastChannel();
	}

	@Override
	public void onHide(boolean isViewCovered)
	{
		if (!isViewCovered)
		{
			_feature.Component.PLAYER.hide();
		}
	}

	@Override
	public boolean onKeyDown(AVKeyEvent keyEvent)
	{
		Log.i(TAG, ".onKeyDown: key = " + keyEvent.Code);
		// FIXME: place volume handling at a base class of all States
		// display global volume bar

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
						playLastChannel();

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
							playLastChannel();
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

		// FIXME: set last channel index
		// _lastChannelIndex = index;
	}

	private void playChannel(int index)
	{
		playChannel(index, System.currentTimeMillis() / 1000, 0);
	}

	private void playLastChannel()
	{
		if (_lastChannelId == null)
			playChannel(0);
		else
		{
			_feature.Component.EPG.getChannelById(_lastChannelId, new OnResultReceived()
			{
				@Override
				public void onReceiveResult(FeatureError error, Object object)
				{
					if (!error.isError())
					{
						Channel channel = (Channel) object;
						playChannel(channel.getIndex());
					}
					else
					{
						Log.e(TAG, error.getMessage(), error);
					}
				}
			});
		}
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
		return Environment.getInstance().getResources().getString(R.string.menu_tv);
	}

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
				final ImageView stationLogo = holder.stationLogo;
				_featureEPG.getChannelLogoBitmap(channel, Channel.LOGO_NORMAL, new OnResultReceived()
				{
					@Override
					public void onReceiveResult(FeatureError error, Object object)
					{
						if (!error.isError())
						{
							stationLogo.setImageBitmap((Bitmap) object);
						}
						else
						{
							Log.e(TAG, error.getMessage(), error);
						}
					}
				});
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
		HIDDEN, SPOOLER, CHANNELS
	}

	private class TVStateManager
	{
		private TVStateEnum _currentStateEnum;
		private BaseTVState _currentState;
		private final BaseTVState _tvStateHidden;
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
		private int _maxChannelNo = -1;
		protected final ChannelSwitcher _channelSwitcher = new ChannelSwitcher();
		protected final ChannelSeeker _channelSeeker = new ChannelSeeker();

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

		private void switchChannelIndex(int channelIndex)
		{
			Log.i(TAG, ".switchChannelIndex: channelIndex = " + channelIndex);
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
				// FIXME: implement this
				/*
				 * int channelIndex = getChannelIndexByNumber(_channelNumber);
				 * Log.i(TAG, ".ON_NUM_TIMEOUT: _channelNumber = " +
				 * _channelNumber + ", channelIndex = " + channelIndex);
				 * TVStateChannels stateChannels = (TVStateChannels)
				 * _tvStateManager.getState(TVStateEnum.CHANNELS);
				 * List<Channel> channels = stateChannels.getChannels();
				 * if (channelIndex >= 0 && channelIndex < channels.size())
				 * {
				 * Channel channel = channels.get(channelIndex);
				 * _channelSwitcher._channelIndex = channel.getIndex();
				 * getEventMessenger().removeCallbacks(_channelSwitcher);
				 * getEventMessenger().postDelayed(_channelSwitcher, 50);
				 * showChannels();
				 * stateChannels.selectChannelIndex(channelIndex);
				 * }
				 * _numView.setVisibility(View.INVISIBLE);
				 * _numView.setText(null);
				 * _channelNumber = 0;
				 */
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
				// FIXME: implement this
				// onDigitPressed(keyEvent.Event.getKeyCode() -
				// KeyEvent.KEYCODE_0);
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
			if (_maxChannelNo < 0)
			{
				// compute maximum channel number in all channels
				_maxChannelNo = 0;
				// List<Channel> channels =
				// _feature.Component.CHANNELS.getActiveChannels();
				// for (int i = 0; i < channels.size(); i++)
				// {
				// ChannelBulsat channel = (ChannelBulsat) channels.get(i);
				// if (channel.getChannelNo() > _maxChannelNo)
				// _maxChannelNo = channel.getChannelNo();
				// }
				// FIXME: find last channel number
				_maxChannelNo = 999;
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
		// FIXME: implement this
		/*
		 * protected int getChannelIndexByNumber(int channelNo)
		 * {
		 * if (_channelNumberToIndex == null)
		 * {
		 * _channelNumberToIndex = new SparseIntArray(200);
		 * List<Channel> activeChannels =
		 * _feature.Component.CHANNELS.getActiveChannels();
		 * for (int i = 0; i < activeChannels.size(); i++)
		 * {
		 * ChannelBulsat bchannel = (ChannelBulsat) activeChannels.get(i);
		 * _channelNumberToIndex.put(bchannel.getChannelNo(),
		 * bchannel.getIndex() + 1);
		 * }
		 * }
		 * Log.d(TAG, ".getChannelIndexByNumber: " + channelNo + " -> " +
		 * (_channelNumberToIndex.get(channelNo) - 1));
		 * return _channelNumberToIndex.get(channelNo) - 1;
		 * }
		 * protected void onDigitPressed(int digit)
		 * {
		 * Log.i(TAG, ".onDigitPressed: digit = " + digit +
		 * ", old _channelNumber = " + _channelNumber);
		 * _channelNumber = _channelNumber * 10 + digit;
		 * _numView.setText(String.valueOf(_channelNumber));
		 * getEventMessenger().removeMessages(ON_NUM_TIMEOUT);
		 * int maxChannelNumber = getMaxChannelNumber();
		 * showChannels();
		 * TVStateChannels stateChannels = (TVStateChannels)
		 * _tvStateManager.getState(TVStateEnum.CHANNELS);
		 * int index = -1;
		 * if (_channelNumber < 10)
		 * {
		 * index = getChannelIndexByNumber(_channelNumber * 100 + 1);
		 * }
		 * else if (_channelNumber < 100)
		 * {
		 * if (digit == 0)
		 * index = getChannelIndexByNumber(_channelNumber * 10 + 1);
		 * else
		 * index = getChannelIndexByNumber(_channelNumber * 10);
		 * }
		 * if (index != -1)
		 * stateChannels.selectChannelIndex(index);
		 * if (_channelNumber > 100 || _channelNumber * 10 > maxChannelNumber)
		 * {
		 * Log.i(TAG, ".onDigitPressed: " + digit + " -> " + _channelNumber +
		 * ", trigger ON_NUM_TIMEOUT");
		 * // switch to selected channel number
		 * getEventMessenger().trigger(ON_NUM_TIMEOUT);
		 * }
		 * else
		 * {
		 * Log.i(TAG, ".onDigitPressed: " + digit + " -> " + _channelNumber +
		 * ", trigger ON_NUM_TIMEOUT delayed");
		 * if (_channelNumber > 0)
		 * {
		 * _numView.setVisibility(View.VISIBLE);
		 * getEventMessenger().trigger(ON_NUM_TIMEOUT,
		 * getPrefs().getInt(Param.NUM_AUTOHIDE) * 1000);
		 * }
		 * else
		 * _numView.setVisibility(View.INVISIBLE);
		 * }
		 * }
		 */
	}

	private class TVStateSpooler extends BaseTVState
	{
		private final ViewGroup _programSpoolContainerView;
		private final ViewGroup _selectedChannelView;
		private final TextView _channelNoTextView;
		private final ImageView _channelLogoImageView;
		protected ImageView _selectedChannelShaddow;

		TVStateSpooler(ViewGroup rootView, ProgramView programView)
		{
			super(rootView, programView);
			_programSpoolContainerView = (ViewGroup) _rootView.findViewById(R.id.tv_program_bar);
			_selectedChannelView = (ViewGroup) _rootView.findViewById(R.id.channel_info_container);
			_channelNoTextView = (TextView) _selectedChannelView.findViewById(R.id.channel_no);
			_channelLogoImageView = (ImageView) _selectedChannelView.findViewById(R.id.channel_logo);
			_selectedChannelShaddow = (ImageView) _programSpoolContainerView.findViewById(R.id.channel_indicator);
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
			}
			else if (msgId == FeatureTimeshift.ON_SEEK)
			{
				updateReplay();
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
					showChannels();
					_tvStateManager.onKeyDown(keyEvent);
					return true;
				case UP:
					showChannels();
					_tvStateManager.onKeyDown(keyEvent);
					return true;
				case LEFT:
				{
					seekRel(getPrefs().getInt(Param.SEEK_LONG_BACKWARD));
					return true;
				}
				case RIGHT:
				{
					seekRel(getPrefs().getInt(Param.SEEK_LONG_FORWARD));
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
			getEventMessenger().trigger(ON_OSD_AUTOHIDE, getPrefs().getInt(Param.OSD_AUTOHIDE) * 1000);
		}

		private void seekRel(int secs)
		{
			if (_feature.Component.TIMESHIFT.getTimeshiftDuration() == 0)
				return;
			final long playTime = _feature.Component.TIMESHIFT.seekRel(secs);

			_feature.Component.EPG.getChannelById(_lastChannelId, new OnResultReceived()
			{
				@Override
				public void onReceiveResult(FeatureError error, Object object)
				{
					if (!error.isError())
					{
						Channel channel = (Channel)object;
						_channelSeeker._playTime = playTime;
						_channelSeeker._channelIndex = channel.getIndex();
						getEventMessenger().removeCallbacks(_channelSeeker);
						getEventMessenger().postDelayed(_channelSeeker, 200);
						updateProgramBar();
						restartOSDTimeout();
					}
					else
					{
						Log.e(TAG, error.getMessage(), error);
					}
				}
			});
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
		}

		@Override
		void setVisible(boolean isVisible)
		{
			_programSpoolContainerView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
			_selectedChannelView.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
			_selectedChannelShaddow.setVisibility(View.INVISIBLE);
			if (isVisible)
			{
				_feature.Component.EPG.getChannelById(_lastChannelId, new OnResultReceived()
				{
					@Override
					public void onReceiveResult(FeatureError error, Object object)
					{
						if (!error.isError())
						{
							ChannelBulsat channel = (ChannelBulsat)object;
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
							updateProgramBar();
						}
						else
						{
							Log.e(TAG, error.getMessage(), error);
						}
					}
				});
			}
		}

		private void updateProgramBar()
		{
			_programView.update(_lastChannelId);
		}
	}

	private class TVStateHidden extends BaseTVState
	{
		TVStateHidden(ViewGroup rootView)
		{
			super(rootView, null);
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
			switch (keyEvent.Code)
			{
				case UP:
				case DOWN:
					showChannels();
					keyEvent.Event.startTracking();
					if (!Key.UP.equals(keyEvent.Code) || !Key.DOWN.equals(keyEvent.Code))
						return _tvStateManager.onKeyDown(keyEvent);
				break;

				case OK:
					_tvStateManager.setState(TVStateEnum.SPOOLER);
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

	private class TVStateChannels extends BaseTVState
	{
		protected ZapperList _zapperList;
		protected ViewGroup _selectedChannelView;
		protected ViewGroup _programContainerView;
		protected TextView _channelNoTextView;
		protected ImageView _channelLogoImageView;
		protected ImageView _selectedChannelShaddow;

		TVStateChannels(ViewGroup rootView, ProgramView programView)
		{
			super(rootView, programView);
			_zapperList = (ZapperList) _rootView.findViewById(R.id.tv_channel_bar);
			_selectedChannelView = (ViewGroup) _rootView.findViewById(R.id.channel_info_container);
			_programContainerView = (ViewGroup) _rootView.findViewById(R.id.tv_program_bar);
			_channelNoTextView = (TextView) _selectedChannelView.findViewById(R.id.channel_no);
			_channelLogoImageView = (ImageView) _selectedChannelView.findViewById(R.id.channel_logo);

			_selectedChannelShaddow = (ImageView) _programContainerView.findViewById(R.id.channel_indicator);

			int channelIndex = _lastChannelIndex;
			if (channelIndex < 0)
				channelIndex = 0;

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
				updateChannel(channelIndex);
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
			        _feature.Component.EPG), channelIndex);
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

		ProgramView(ViewGroup viewGroup)
		{
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

		private void updatePrograms(Program prevProgram, Program currProgram, Program nextProgram)
		{
			setPreviousProgram(prevProgram);
			setCurrentProgram(currProgram);
			setNextProgram(nextProgram);
		}

		void update(final String channelId)
		{
			Log.i(TAG, "ProgramView.update: channelId = " + channelId);

			_feature.Component.EPG.getChannelById(channelId, new OnResultReceived()
			{
				@Override
				public void onReceiveResult(FeatureError error, Object object)
				{
					if (!error.isError())
					{
						Channel channel = (Channel)object;
						final Calendar currentTime = _feature.Component.TIMEZONE.getCurrentTime();
						final Calendar playTime = _feature.Component.TIMEZONE.getCurrentTime();

						final FeatureTimeshift timeshift = _feature.Component.TIMESHIFT;
						final boolean inTimeshift = (channelId == _lastChannelId);
						if (inTimeshift)
						{
							// shift in time the current playing channel
							playTime.setTimeInMillis(1000 * timeshift.getPlayingTime());
						}

						// updates current playing time
						ChannelTitle.setText(channel.getTitle());

						_feature.Component.EPG.getPrograms(channel, playTime, -1, 3, new OnResultReceived()
						{
							@Override
							public void onReceiveResult(FeatureError error, Object object)
							{
								@SuppressWarnings("unchecked")
								List<Program> programs = (List<Program>) object;
								Program currProgram = null, prevProgram = null, nextProgram = null;
								if (programs != null)
									if (programs.size() > 0)
										prevProgram = programs.get(0);
									else if (programs.size() > 1)
										currProgram = programs.get(1);
									else if (programs.size() > 2)
										nextProgram = programs.get(2);

								updatePrograms(prevProgram, currProgram, nextProgram);

								if (currProgram == null)
								{
									ProgramProgress.setVisibility(View.INVISIBLE);
									ProgramTimeStart.setVisibility(View.INVISIBLE);
									ProgramTimeEnd.setVisibility(View.INVISIBLE);
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

								if (inTimeshift)
								{
									// update timeshift progress
									Calendar timeshiftStartCalendar = _feature.Component.TIMEZONE.getCurrentTime();
									long timeshiftStart = timeshift.currentTime() - timeshift.getTimeshiftDuration();
									timeshiftStartCalendar.setTimeInMillis(1000 * timeshiftStart);

									// show as a secondary progress the empty buffer
									// check if beginning of the buffer is inside current
									// program
									if (currProgram.getStartTime().before(timeshiftStartCalendar)
									        && currProgram.getStopTime().after(timeshiftStartCalendar))
									{
										// show as a secondary progress the empty buffer
										// from
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
									float currentTimeFragment = getProgressInProgram(currProgram,
									        currentTime.getTimeInMillis() / 1000);
									ProgramProgress.setProgress((int) (1000 * currentTimeFragment));

									float cursorFragment = getProgressInProgram(currProgram, playTime.getTimeInMillis() / 1000);
									int progressBarWidth = ProgramProgress.getWidth();
									int offset = (int) (progressBarWidth * cursorFragment) - TimeshiftCursor.getWidth() / 2;
									RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) TimeshiftCursor
									        .getLayoutParams();
									params.setMargins(offset, 0, 0, 0);
									TimeshiftCursor.setLayoutParams(params);
									// show cursor
									TimeshiftCursor.setVisibility(View.VISIBLE);
								}
								else
								{
									ProgramProgress.setSecondaryProgress(0);
									float currentTimeFragment = getProgressInProgram(currProgram,
									        currentTime.getTimeInMillis() / 1000);
									ProgramProgress.setProgress((int) (1000 * currentTimeFragment));
								}
							}
						});

						// hide cursor now and show it later if necessary
						TimeshiftCursor.setVisibility(View.INVISIBLE);
					}
					else
					{
						Log.e(TAG, error.getMessage(), error);
					}
				}
			});
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

		private void setPreviousProgram(Program program)
		{
			setProgramToView(program, prevProgramTime, prevProgramTitle);
		}

		private void setCurrentProgram(Program program)
		{
			setProgramToView(program, currProgramTime, currProgramTitle);
		}

		private void setNextProgram(Program program)
		{
			setProgramToView(program, nextProgramTime, nextProgramTitle);
		}

		private void setProgramToView(Program program, TextView programTime, TextView programTitle)
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
