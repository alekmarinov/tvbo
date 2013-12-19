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

	private SimpleDateFormat _dateFormat;
	private SimpleDateFormat _timeFormat;
	
	public EpgProgramInfo(Context context, ViewGroup container)
	{
		_thumbnail = (ImageView) container.findViewById(R.id.thumbnail);
		_date = (TextView) container.findViewById(R.id.date);
		_time = (TextView) container.findViewById(R.id.time);
		_primaryTitle = (TextView) container.findViewById(R.id.title_primary);
		_secondaryTitle = (TextView) container.findViewById(R.id.title_secondary);
		_summary = (TextView) container.findViewById(R.id.summary);
		
		String dateFormat = context.getString(R.string.programInfoDateFormat);
		_dateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
		
		String timeFormat = context.getString(R.string.programInfoTimeFormat);
		_timeFormat = new SimpleDateFormat(timeFormat, Locale.getDefault());
	}
	
	public void update(Program program)
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
}
