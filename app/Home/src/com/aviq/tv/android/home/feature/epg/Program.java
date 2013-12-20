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

import android.os.Parcel;
import android.os.Parcelable;

import com.aviq.tv.android.home.utils.Log;

/**
 * Program data holder class
 */
public abstract class Program implements Parcelable, Comparable<Program>
{
	private static final String TAG = Program.class.getSimpleName();
	private static final DateFormat EPG_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());

	// Bean properties
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

	public Program()
	{
	}

	public Program(Parcel in)
	{
		this();

		try
		{
			readFromParcel(in);
		}
		catch (ParseException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public void readFromParcel(Parcel in) throws ParseException
	{
		setStartTime(in.readString());
		setStopTime(in.readString());
		setTitle(in.readString());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(_startTime);
		dest.writeString(_stopTime);
		dest.writeString(_title);
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public int compareTo(Program another)
	{
		return _startTime.compareTo(another._startTime);
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
    public abstract void setDetailAttributes(MetaData metaData, String[] attributes);
}
