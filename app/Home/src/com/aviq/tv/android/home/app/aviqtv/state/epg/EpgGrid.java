package com.aviq.tv.android.home.app.aviqtv.state.epg;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.feature.epg.Channel;
import com.aviq.tv.android.home.feature.epg.IEpgDataProvider;
import com.aviq.tv.android.home.feature.epg.Program;

public class EpgGrid
{
	private static final String TAG = EpgGrid.class.getSimpleName();

	private static final int HEADER_POSITION_OFFSET = 0; //-1; //TODO:ZZ:set to 0 for testing

	public static enum NAVIGATION
	{
		LEFT, RIGHT, UP, DOWN
	};

	/** The activity that displays the EPG grid */
	private final Activity _activity;

	/** The EPG grid header component */
	private EpgHeaderView _gridHeader;

	/** View used to display a date or some time-related string */
	private TextView _dateTimeView;

	/** A calendar pointing to the EPG start time */
	private Calendar _gridStartTimeCalendar;

	/** The grid list (vertical component) */
	private EpgListView _gridList;

	/** The grid list adapter (vertical component) */
	private EpgListViewAdapter _gridListAdapter;

	/* Screen dimensions */
	private int _screenWidth;
	private int _screenHeight;

	/** Data provider that needs to fulfill the interface contract */
	private IEpgDataProvider _dataProvider;

	/** Current channel where the EPG selection is */
	private Channel _channel;

	/**
	 * Used to pre-select the correct program when navigating up/down the
	 * program list
	 */
	private Program _prevEpgRowSelectedProgram;

	/** Horizontal movement direction: left, right */
	private NAVIGATION _navigation;

	private int _currentVerticalPageNumber = -1;

	private boolean _loadingEpgData = false;

	private OnEpgGridEventListener _onEpgGridItemSelectionListener;

	public EpgGrid(Activity activity)
	{
		_activity = activity;
	}

	public void setEpgHeaderView(EpgHeaderView header)
	{
		_gridHeader = header;

		Calendar now = Calendar.getInstance();
		_gridHeader.useDefaultAdapter(now, HEADER_POSITION_OFFSET);

		int pos = _gridHeader.getPositionForTime(now);
		_gridStartTimeCalendar = _gridHeader.getTimeForPosition(pos + HEADER_POSITION_OFFSET);
	}

	private Calendar getNextPageStartingTime()
	{
		int pos = _gridHeader.getPositionForTime(_gridStartTimeCalendar);

		double pixelsPerMinute = ((TimeBasedRelativeLayout) _gridHeader.getChildAt(0)).getPixelsPerOneMinute();
		int numMinutesInSinglePosition = _gridHeader.getMinutesForSinglePosition();
		int numVisibleOnScreen = (int) Math.round((_gridHeader.getWidth() / (pixelsPerMinute * numMinutesInSinglePosition)));
		int nextPositionToShow = pos + numVisibleOnScreen;
		if (nextPositionToShow > _gridHeader.getAdapter().getCount())
			return null;

		// Log.v(TAG,
		// ".getNextPageStartingTime: Move EPG header from position = " + pos +
		// " to " + nextPositionToShow);

		return _gridHeader.getTimeForPosition(nextPositionToShow);
	}

	private Calendar getPrevPageStartingTime()
	{
		int pos = _gridHeader.getPositionForTime(_gridStartTimeCalendar);

		double pixelsPerMinute = ((TimeBasedRelativeLayout) _gridHeader.getChildAt(0)).getPixelsPerOneMinute();
		int numMinutesInSinglePosition = _gridHeader.getMinutesForSinglePosition();
		int numVisibleOnScreen = (int) Math.round((_gridHeader.getWidth() / (pixelsPerMinute * numMinutesInSinglePosition)));
		int prevPositionToShow = pos - numVisibleOnScreen;
		if (prevPositionToShow < 0)
			prevPositionToShow = 0;

		// Log.v(TAG,
		// ".getPrevPageStartingTime: Move EPG header from position = " + pos +
		// " to " + prevPositionToShow);

		return _gridHeader.getTimeForPosition(prevPositionToShow);
	}

	public void setEpgListView(EpgListView list)
	{
		_gridList = list;

		WindowManager wm = (WindowManager) _activity.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		_screenWidth = display.getWidth();
		_screenHeight = display.getHeight();

		_gridList.setFocusable(true);
		_gridList.setFocusableInTouchMode(true);
		_gridList.setOnItemSelectingListener(_gridOnItemSelecting);
		_gridList.setOnItemSelectedListener(_gridOnItemSelected);
	}

	public void setDateTimeView(TextView tv)
	{
		_dateTimeView = tv;
		updateDateTimeView(_gridStartTimeCalendar);
	}

	private void updateDateTimeView(Calendar cal)
	{
		// If today, set it and return (most likely scenario).
		if (DateUtils.isToday(cal.getTimeInMillis()))
		{
			String text = _activity.getString(R.string.today);
			_dateTimeView.setText(text);
			return;
		}

		long calMillis = cal.getTimeInMillis();

		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		long todayMillis = today.getTimeInMillis();

		// Tomorrow? Second more likely scenario.
		Calendar twoDaysTime = Calendar.getInstance();
		twoDaysTime.setTimeInMillis(todayMillis);
		twoDaysTime.add(Calendar.HOUR_OF_DAY, 48);
		long twoDaysTimeMillis = twoDaysTime.getTimeInMillis();

		// Yesterday? Third more likely scenario.
		Calendar yesterday = Calendar.getInstance();
		yesterday.setTimeInMillis(todayMillis);
		yesterday.add(Calendar.HOUR_OF_DAY, -24);
		long yesterdayMillis = yesterday.getTimeInMillis();

		if (yesterdayMillis <= calMillis && calMillis < todayMillis)
		{
			String text = _activity.getString(R.string.yesterday);
			_dateTimeView.setText(text);
			return;
		}

		if (calMillis < twoDaysTimeMillis)
		{
			String text = _activity.getString(R.string.tomorrow);
			_dateTimeView.setText(text);
			return;
		}

		// Any other date
		String pattern = _activity.getString(R.string.scheduleHeaderDateTimeFormat);
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
		String text = sdf.format(cal.getTime());
		_dateTimeView.setText(text);
	}

	public void setDataProvider(IEpgDataProvider provider)
	{
		_dataProvider = provider;
	}

	/**
	 * The object being set holds the current channel selection of the EPG grid.
	 *
	 * @param channel
	 */
	public void setSelectedChannel(Channel channel)
	{
		_channel = channel;
	}

	public Channel getSelectedChannel()
	{
		return _channel;
	}

	public EpgRowView getSelectedProgramList()
	{
		ViewGroup selectedGridRow = (ViewGroup) _gridList.getSelectedView();
		if (selectedGridRow == null)
			return null;

		EpgRowView programList = (EpgRowView) selectedGridRow.findViewById(R.id.program_list);
		return programList;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.d(TAG, "handleKey: key = " + keyCode);

		if (_loadingEpgData)
			return true;

		switch (keyCode)
		{
			case KeyEvent.KEYCODE_DPAD_UP:
			{
				_navigation = NAVIGATION.UP;

				EpgRowView programList = getSelectedProgramList();
				if (programList != null)
				{
					programList.unsetSelection(programList.getSelectedItemPosition());
				}

				if (_gridList.getSelectedItemPosition() == 0)
				{
					_currentVerticalPageNumber--;
					if (_currentVerticalPageNumber < 0)
						_currentVerticalPageNumber = getEpgPagesCount() - 1;

					fillEpgData(_gridStartTimeCalendar, null);
				}
				else
				{
					_gridList.onKeyDown(keyCode, event);
					_channel = (Channel) _gridListAdapter.getItem(_gridList.getSelectedItemPosition());
				}
				return true;
			}

			case KeyEvent.KEYCODE_DPAD_DOWN:
			{
				_navigation = NAVIGATION.DOWN;

				EpgRowView programList = getSelectedProgramList();
				if (programList != null)
				{
					programList.unsetSelection(programList.getSelectedItemPosition());
				}

				if (_gridList.getSelectedItemPosition() == _gridListAdapter.getCount() - 1)
				{
					_currentVerticalPageNumber++;
					if (_currentVerticalPageNumber >= getEpgPagesCount())
						_currentVerticalPageNumber = 0;

					fillEpgData(_gridStartTimeCalendar, null);
				}
				else
				{
					_gridList.onKeyDown(keyCode, event);
					_channel = (Channel) _gridListAdapter.getItem(_gridList.getSelectedItemPosition());
				}
				return true;
			}
//			case CHANNEL_UP:
//			{
//				_navigation = NAVIGATION.DOWN;
//				_currentVerticalPageNumber++;
//				if (_currentVerticalPageNumber >= getEpgPagesCount())
//					_currentVerticalPageNumber = 0;
//
//				fillEpgData(_gridStartTimeCalendar, null);
//				return true;
//			}
//			case CHANNEL_DOWN:
//			{
//				_navigation = NAVIGATION.UP;
//				_currentVerticalPageNumber--;
//				if (_currentVerticalPageNumber < 0)
//					_currentVerticalPageNumber = getEpgPagesCount() - 1;
//
//				fillEpgData(_gridStartTimeCalendar, null);
//				return true;
//			}
			case KeyEvent.KEYCODE_DPAD_LEFT:
			{
				_navigation = NAVIGATION.LEFT;

				EpgRowView programList = getSelectedProgramList();

				View selectedView1 = programList.getSelectedView();
				programList.onKeyDown(keyCode, event);
				View selectedView2 = programList.getSelectedView();

				_prevEpgRowSelectedProgram = null;

				// Determine whether we need to paginate to the left
				boolean isAtEpgHeaderStart = _gridStartTimeCalendar.equals(_gridHeader.getAdapter().getItem(0));

				boolean needToPaginate = !isAtEpgHeaderStart
				        && (selectedView2 == null || (selectedView1 != null && selectedView2 != null && selectedView1
				                .equals(selectedView2)));

				if (needToPaginate)
				{
					_gridStartTimeCalendar = getPrevPageStartingTime();

					int headerPos = _gridHeader.getPositionForTime(_gridStartTimeCalendar);
					_gridHeader.setPosition(headerPos);

					fillEpgData(_gridStartTimeCalendar, _channel);
				}
				return true;
			}

			case KeyEvent.KEYCODE_DPAD_RIGHT:
			{
				_navigation = NAVIGATION.RIGHT;

				EpgRowView programList = getSelectedProgramList();

				View selectedView1 = programList.getSelectedView();
				programList.onKeyDown(keyCode, event);
				View selectedView2 = programList.getSelectedView();

				_prevEpgRowSelectedProgram = null;

				// Determine whether we need to paginate to the right
				// estimate if last visible header time is within the available
				// data range
				long timeStartSec = _gridStartTimeCalendar.getTimeInMillis() / 1000;
				long timeEndSec = timeStartSec
				        + (long) ((_gridHeader.getWidth() / _gridHeader.getPixelsPerMinute()) * 60);
				Calendar visibleHeaderEndTime = Calendar.getInstance();
				visibleHeaderEndTime.setTimeInMillis(timeEndSec * 1000);

				boolean isAtEpgHeaderEnd = (visibleHeaderEndTime.getTimeInMillis() > _dataProvider.getMaxEpgStopTime().getTimeInMillis());
				boolean needToPaginate = !isAtEpgHeaderEnd
				        && (selectedView2 == null || (selectedView1 != null && selectedView2 != null && selectedView1
				                .equals(selectedView2)));

				Log.d(TAG, ".onKeyDown: isAtEpgHeaderEnd = " + isAtEpgHeaderEnd + " needToPaginate = " + needToPaginate);
				if (needToPaginate)
				{
					Calendar newGridStartTime = getNextPageStartingTime();
					if (newGridStartTime != null)
					{
						_gridStartTimeCalendar = newGridStartTime;

						int headerPos = _gridHeader.getPositionForTime(_gridStartTimeCalendar);
						_gridHeader.setPosition(headerPos);

						fillEpgData(_gridStartTimeCalendar, _channel);
					}
				}

				return true;
			}

			default:
		}

		return false;
	}

	/**
	 * After this method is called, the EPG component is filled out with data
	 * and is ready for use by the end user.
	 */
	public void prepareEpg()
	{
		if (_gridStartTimeCalendar == null)
			throw new NullPointerException("Please use setEpgHeaderView() before calling this method.");

		if (_channel == null)
			throw new NullPointerException("Please use setSelectedChannel() to set the initial channel selection.");

		_currentVerticalPageNumber = calcVerticalPageNum(_channel);
		if (_currentVerticalPageNumber < 0)
			_currentVerticalPageNumber = 0;

		fillEpgData(_gridStartTimeCalendar, _channel);
	}

	public void jumpToTime(Calendar time)
	{
		// find header cell position and set page start time if different from
		// current
		int pos = _gridHeader.getPositionForTime(time);
		_navigation = NAVIGATION.RIGHT;
		if (pos != _gridHeader.getPosition())
		{
			_gridHeader.setPosition(pos);
			_gridStartTimeCalendar = _gridHeader.getTimeForPosition(pos);

			fillEpgData(time, _channel);
		}
		else
			getSelectedProgramList().setSelection(0);
	}

	/**
	 * Re-set the grid
	 *
	 * @param startTime
	 * @param channel
	 */
	private void fillEpgData(final Calendar startTime, final Channel channel)
	{
		_loadingEpgData = true;

		// Get the list of channels that will become visible
		int startIndex = _currentVerticalPageNumber * _gridList.getVisibleItems();
		int endIndex = startIndex + _gridList.getVisibleItems();
		if (endIndex > _dataProvider.getChannelCount())
			endIndex = _dataProvider.getChannelCount();

		long timeStartMillis = startTime.getTimeInMillis();
		double minutes = _gridHeader.getWidth() / _gridHeader.getPixelsPerMinute();
		minutes = minutes - minutes % EpgHeaderView.TIME_SLOT_MINUTES;
		long timeEndMillis = timeStartMillis + (long) (minutes * 60000);

		String timeStart = Program.getEpgTime(timeStartMillis);
		String timeEnd = Program.getEpgTime(timeEndMillis);

		LinkedHashMap<Channel, List<Program>> data = new LinkedHashMap<Channel, List<Program>>();
		for (int i = startIndex; i < endIndex; i++)
		{
			Channel channelTmp = _dataProvider.getChannel(i);
			List<Program> programList = _dataProvider.getProgramList(channelTmp.getChannelId(), timeStart, timeEnd);
			data.put(channelTmp, programList);
		}

		// Re-init channel object on page up/down
		Channel selectedChannel = channel;
		if (selectedChannel == null)
		{
			if (NAVIGATION.UP.equals(_navigation))
			{
				// Re-init with last element in list on page up
				for (Channel ch : data.keySet())
				{
					selectedChannel = ch;
				}
			}
			else if (NAVIGATION.DOWN.equals(_navigation))
			{
				// Re-init with first element in list on page down
				for (Channel ch : data.keySet())
				{
					selectedChannel = ch;
					break;
				}
			}
		}

		_gridListAdapter = new EpgListViewAdapter(_activity, _dataProvider, data);
		_gridListAdapter.setEpgRowStartTime(startTime.getTimeInMillis());
		_gridListAdapter.setOnProgramItemSelectingListener(_onProgramItemSelectingListener);
		_gridListAdapter.setOnProgramItemSelectedListener(_onProgramItemSelectedListener);
		_gridList.setAdapter(_gridListAdapter);

		int position = _gridListAdapter.getItemPosition(selectedChannel);
		_gridList.setSelection(position);

		_loadingEpgData = false;
		updateDateTimeView(startTime);
	}

	/**
	 * Calculate the vertical page number where the current channel resides.
	 */
	private int calcVerticalPageNum(Channel currentChannel)
	{
		return _dataProvider.getChannelIndex(currentChannel) / _gridList.getVisibleItems();
	}

	private int getEpgPagesCount()
	{
		return (int) Math.ceil((double) _dataProvider.getChannelCount() / _gridList.getVisibleItems());
	}

	private boolean isViewOnScreen(View view)
	{
		if (view == null)
			return false;

		int[] locOnScreen = new int[2];
		view.getLocationOnScreen(locOnScreen);

		if (locOnScreen[0] < 0)
			return false;
		else if (locOnScreen[0] > _screenWidth || locOnScreen[0] + view.getWidth() > _screenWidth)
			return false;

		return true;
	}

	public void setOnEpgGridItemSelection(OnEpgGridEventListener listener)
	{
		_onEpgGridItemSelectionListener = listener;
	}

	private final EpgListView.OnItemSelectingListener _gridOnItemSelecting = new EpgListView.OnItemSelectingListener()
	{
		@Override
		public void onItemSelecting(AdapterView<?> parent, View view, int oldPosition, int newPosition)
		{
			Log.v(TAG, "EpgListView.onItemSelecting: oldPosition = " + oldPosition + ", newPosition = " + newPosition);

			if (view == null)
				return;

			// Here "view" is the same as "_gridList.getViewAt(oldPosition)",
			// i.e. the EPG grid row before the selection

			ViewGroup logo = (ViewGroup) view.findViewById(R.id.logo_container);
			logo.setBackgroundResource(R.color.transparent);

			EpgRowView epgRow = (EpgRowView) view.findViewById(R.id.program_list);
			Program p = (Program) epgRow.getSelectedItem();
			_prevEpgRowSelectedProgram = p;
		}
	};

	private final EpgListView.OnItemSelectedListener _gridOnItemSelected = new EpgListView.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			Log.v(TAG, "EpgListView.onItemSelected: position = " + position);

			if (view == null)
				return;

			// Here "view" is the same as "_gridList.getViewAt(oldPosition)",
			// i.e. the EPG grid row before the selection

			ViewGroup logo = (ViewGroup) view.findViewById(R.id.logo_container);
			logo.setBackgroundResource(R.drawable.epg_grid_cell_selected);

			// Set the proper row selection position when moving between rows

			EpgRowView epgRow = (EpgRowView) view.findViewById(R.id.program_list);
			resetEpgRowSelection(epgRow);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent)
		{
		}

		private void resetEpgRowSelection(final EpgRowView epgRow)
		{
			EpgRowAdapter epgRowAdapter = epgRow.getAdapter();
			if (epgRowAdapter == null)
				return;

			int numPrograms = epgRowAdapter.getCount();

			int position = -1;
			long nowMillis = Calendar.getInstance().getTimeInMillis();

			if (_prevEpgRowSelectedProgram != null)
			{
				long prevStartTime = _prevEpgRowSelectedProgram.getStartTimeCalendar().getTimeInMillis();
				long prevEndTime = _prevEpgRowSelectedProgram.getStopTimeCalendar().getTimeInMillis();
				boolean isPrevProgramPlayingNow = prevStartTime <= nowMillis && prevEndTime > nowMillis;
				long commonLengthLeft = -1;
				int leftPosition = -1;
				long commonLengthRight = -1;
				int rightPosition = -1;
				int positionWeight = -1;

				for (int i = 0; i < numPrograms; i++)
				{
					Program p = (Program) epgRowAdapter.getItem(i);
					long startTime = p.getStartTimeCalendar().getTimeInMillis();
					long endTime = p.getStopTimeCalendar().getTimeInMillis();
					boolean isProgramPlayingNow = startTime <= nowMillis && endTime > nowMillis;

					// Ignore program objects that are not interesting in terms
					// of start/end times
					if ((startTime <= prevStartTime && endTime < prevStartTime)
					        || (startTime >= prevEndTime && endTime > prevEndTime))
						continue;

					// Switching among currently played programs
					if (isPrevProgramPlayingNow && isProgramPlayingNow)
					{
						// Good enough, break the loop
						// Log.v(TAG, ".resetEpgRowSelection: CASE PLAYING");

						final int ASSIGNED_WEIGHT = 400;

						if (positionWeight >= ASSIGNED_WEIGHT)
							continue;

						positionWeight = ASSIGNED_WEIGHT;
						position = i;
						continue;
					}

					// New cell is larger than the previous one and is either
					// above or below it
					if (startTime <= prevStartTime && endTime >= prevEndTime)
					{
						// Good enough, break the loop
						// Log.v(TAG, ".resetEpgRowSelection: CASE EXPLODE");

						final int ASSIGNED_WEIGHT = 300;

						if (positionWeight >= ASSIGNED_WEIGHT)
							continue;

						positionWeight = ASSIGNED_WEIGHT;
						position = i;
						continue;
					}

					// New cell is smaller than the previous one and is either
					// above or below it
					if (startTime >= prevStartTime && endTime <= prevEndTime)
					{
						// Good enough, break the loop
						// Log.v(TAG, ".resetEpgRowSelection: CASE IMPLODE");

						final int ASSIGNED_WEIGHT = 300;

						if (positionWeight >= ASSIGNED_WEIGHT)
							continue;

						positionWeight = ASSIGNED_WEIGHT;
						position = i;
						continue;
					}

					// New cell overlaps the previous so that its startTime is
					// before the previous cell's start time
					if (startTime <= prevStartTime && endTime <= prevEndTime)
					{
						// OK, but keep looping for something better
						// Log.v(TAG, ".resetEpgRowSelection: LEFT SHIFTED");

						final int ASSIGNED_WEIGHT = 100;

						if (positionWeight > ASSIGNED_WEIGHT)
							continue;

						positionWeight = ASSIGNED_WEIGHT;
						position = i;

						// Calc how much common area the new program and the
						// previous program have
						commonLengthLeft = endTime - prevStartTime;
						leftPosition = i;
						continue;
					}

					// New cell overlaps the previous so that its startTime is
					// past the previous cell's start time
					if (startTime >= prevStartTime && endTime >= prevEndTime)
					{
						// OK, but keep looping for something better
						// Log.v(TAG, ".resetEpgRowSelection: RIGHT SHIFTED");

						final int ASSIGNED_WEIGHT = 100;

						if (positionWeight > ASSIGNED_WEIGHT)
							continue;

						positionWeight = ASSIGNED_WEIGHT;
						position = i;

						// Calc how much common area the new program and the
						// previous program have
						commonLengthRight = prevEndTime - startTime;
						rightPosition = i;
						continue;
					}
				}

				// Reset position when program items are overlapping (based on
				// how much common area they share)
				if (leftPosition > -1 && rightPosition > -1)
				{
					position = commonLengthLeft >= commonLengthRight ? leftPosition : rightPosition;
				}
			}
			else
			{
				if (NAVIGATION.LEFT.equals(_navigation))
				{
					// This takes care of any EPG gaps that break the selection
					// on the right after pagination to the left
					int numItems = epgRow.getAdapter().getCount();
					int numViews = epgRow.getChildCount();
					if (numViews < numItems)
						position = numViews - 1;
					else
						position = numItems - 1;
				}
				else
				{
					position = 0;
				}
			}

			epgRow.setSelection(position);
		}
	};

	private final EpgRowView.OnItemSelectingListener _onProgramItemSelectingListener = new EpgRowView.OnItemSelectingListener()
	{
		@Override
		public void onItemSelecting(AdapterView<?> parent, View view, int oldPosition, int newPosition)
		{
			Log.d(TAG, "EpgRowView.onItemSelecting: oldPosition = " + oldPosition + ", newPosition = " + newPosition);

			if (view == null)
			{
				return;
			}

			if (_onEpgGridItemSelectionListener != null)
			{
				// Get selected Channel object
				Channel newChannel = (Channel) _gridList.getSelectedItem();

				// Get selected Program object
				// EpgRowAdapter adapter = ((EpgRowView) parent).getAdapter();
				// Program newProgram = (Program) adapter.getItem(newPosition);
				Program newProgram = (Program) parent.getSelectedItem();

				_onEpgGridItemSelectionListener.onEpgGridItemSelecting(newChannel, newProgram);
			}
		}
	};

	private final EpgRowView.OnItemSelectedListener _onProgramItemSelectedListener = new EpgRowView.OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
		{
			Log.d(TAG, "EpgRowView.onItemSelected: position = " + position);

			if (view == null)
			{
				Log.d(TAG, "EpgRowView.onItemselected: view is null");
				return;
			}

			// Get selected Channel object
			Channel channel = (Channel) _gridList.getSelectedItem();

			// Get selected Program object
			Program program = (Program) parent.getSelectedItem();

			// Get previously selected Program object
			Program prevProgram = null;
			if (NAVIGATION.UP.equals(_navigation) || NAVIGATION.DOWN.equals(_navigation))
			{
				prevProgram = _prevEpgRowSelectedProgram;
			}
			else
			{
				int prevPosition = ((EpgRowView) parent).getPrevItemPosition();
				prevProgram = prevPosition < 0 ? null : (Program) parent.getAdapter().getItem(prevPosition);
			}

			// Only update border cases in order not to slow down the UI
			
			final int programStartDay = program.getStartTimeCalendar().get(Calendar.DAY_OF_MONTH);
			final int prevProgramStartDay = prevProgram != null ? prevProgram.getStartTimeCalendar().get(Calendar.DAY_OF_MONTH)
			        : programStartDay;
			if (programStartDay != prevProgramStartDay)
				updateDateTimeView(program.getStartTimeCalendar());

			if (_onEpgGridItemSelectionListener != null)
				_onEpgGridItemSelectionListener.onEpgGridItemSelected(channel, program);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0)
		{
		}
	};

	public interface OnEpgGridEventListener
	{
		public void onEpgGridItemSelecting(Channel channel, Program program);

		public void onEpgGridItemSelected(Channel channel, Program program);
		
		public void onEpgPageScroll(NAVIGATION direction);
	}
}
