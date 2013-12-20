package com.aviq.tv.android.home.feature.state.epg;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.feature.epg.Program;

public class EpgProgramInfo
{
	private ImageView _thumbnail;
	private TextView _date;
	private TextView _time;
	private TextView _primaryTitle;
	private TextView _secondaryTitle;
	private TextView _summary;
	private TextView _remainingTime;
	private TextView _timeRange;
	private TextView _pager;
	
	private SimpleDateFormat _dateFormat;
	private SimpleDateFormat _timeFormat;
	private String _remainingTimeTemplate;
	private String _timeRangeTemplate;
	private String _pagerTemplate;
	
	public EpgProgramInfo(Context context, ViewGroup container)
	{
		_thumbnail = (ImageView) container.findViewById(R.id.thumbnail);
		_date = (TextView) container.findViewById(R.id.date);
		_time = (TextView) container.findViewById(R.id.time);
		_primaryTitle = (TextView) container.findViewById(R.id.title_primary);
		_secondaryTitle = (TextView) container.findViewById(R.id.title_secondary);
		_summary = (TextView) container.findViewById(R.id.summary);
		_remainingTime = (TextView) container.findViewById(R.id.remaining_time);
		_timeRange = (TextView) container.findViewById(R.id.time_range);
		_pager = (TextView) container.findViewById(R.id.pager);
		
		String dateFormat = context.getString(R.string.programInfoDateFormat);
		_dateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
		
		String timeFormat = context.getString(R.string.programInfoTimeFormat);
		_timeFormat = new SimpleDateFormat(timeFormat, Locale.getDefault());
		
		_remainingTimeTemplate = context.getString(R.string.programInfoRemainingTime);
		_timeRangeTemplate = context.getString(R.string.programInfoTimeRange);
		_pagerTemplate = context.getString(R.string.programInfoPager);
	}
	
	public void updateBrief(Program program)
	{
		Calendar startTimeCal = program.getStartTimeCalendar();

		String startDate = _dateFormat.format(startTimeCal.getTime());
		_date.setText(startDate);
		
		String startTime = _timeFormat.format(startTimeCal.getTime());
		_time.setText(startTime);
		
		_primaryTitle.setText(program.getTitle());
		_secondaryTitle.setText(null);
		_summary.setText(null);
	}
	
	public void updateDetails(Program program)
	{
		Calendar startTimeCal = program.getStartTimeCalendar();
		String startTime = _timeFormat.format(startTimeCal.getTime());
		Calendar stopTimeCal = program.getStopTimeCalendar();
		String stopTime = _timeFormat.format(stopTimeCal.getTime());
		_timeRange.setText(String.format(_timeRangeTemplate, startTime, stopTime));
		
		_primaryTitle.setText(program.getTitle());
		_secondaryTitle.setText(null);
		_summary.setText(null);
		
		_pager.setText(String.format(_pagerTemplate, 1, 1));
		
//		Calendar.getInstance().getTimeInMillis() - startTi 
//		_remainingTime.setText(String.format(_remainingTimeTemplate, remainingTime));
	}
}
