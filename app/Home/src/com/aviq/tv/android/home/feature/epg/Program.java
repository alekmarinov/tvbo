/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    Program.java
 * Author:      alek
 * Date:        19 Dec 2013
 * Description: Program data holder class
 */
package com.aviq.tv.android.home.feature.epg;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

/**
 * Program data holder class
 */
public abstract class Program implements Comparable<Program>
{
	private static final String TAG = Program.class.getSimpleName();
	private static final DateFormat EPG_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());

	// Bean properties
	private Channel _channel;
	private String _startTime;
	private String _stopTime;
	private String _title;

	// Other internal properties
	private Calendar _startTimeCalendar;
	private Calendar _stopTimeCalendar;

	public static class MetaData
	{
		public int metaStart;
		public int metaStop;
		public int metaTitle;
	}

	public static Calendar getEpgTime(String epgTime)
	{
		Calendar cal;
		try
		{
			Date dte = EPG_DATE_FORMAT.parse(epgTime);
			cal = Calendar.getInstance();
			cal.setTime(dte);
		}
		catch (ParseException e)
		{
			return null;
		}
		return cal;
	}

	public static String getEpgTime(Calendar cal)
	{
		String dateTime = EPG_DATE_FORMAT.format(cal.getTime());
		return dateTime;
	}

	public Program(Channel channel)
	{
		_channel = channel;
	}

	@Override
	public int compareTo(Program another)
	{
		return _startTime.compareTo(another._startTime);
	}

	public Channel getChannel()
	{
		return _channel;
	}

	public Calendar getStartTimeCalendar()
	{
		if (_startTimeCalendar == null)
		{
			_startTimeCalendar = getEpgTime(_startTime);
		}
		return _startTimeCalendar;
	}

	public Calendar getStopTimeCalendar()
	{
		if (_stopTimeCalendar == null)
		{
			_stopTimeCalendar = getEpgTime(_stopTime);
		}
		return _stopTimeCalendar;
	}

	public static String getEpgTime(long millis)
	{
		String dateTime = EPG_DATE_FORMAT.format(new Date(millis));
		return dateTime;
	}

	public String getId()
	{
		return _startTime;
	}

	public String getStartTime()
	{
		return _startTime;
	}

	public void setStartTime(String startTime) throws ParseException
	{
		_startTime = startTime;
	}

	public String getStopTime()
	{
		return _stopTime;
	}

	public void setStopTime(String stopTime) throws ParseException
	{
		_stopTime = stopTime;
	}

	public String getTitle()
	{
		return _title;
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	/**
	 * @return true if program detail attributes has been set
	 */
	public abstract boolean hasDetails();

	/**
	 * Set program detail attributes
	 *
	 * @param JSONObject with program detail attributes
	 */
	public abstract void setDetails(JSONObject details);

	/**
	 * @param programAttribute
	 *
	 * @return attribute string value
	 */
	public abstract String getDetailAttribute(ProgramAttribute programAttribute);

	/**
	 * Sets provider's specific program attributes
	 *
	 * @param metaData indexed meta data
	 * @param attributes String array with the essential data positioned according the meta data indices
	 */
    public abstract void setAttributes(MetaData metaData, String[] attributes);
}
