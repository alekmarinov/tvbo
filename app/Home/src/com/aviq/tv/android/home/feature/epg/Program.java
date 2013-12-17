package com.aviq.tv.android.home.feature.epg;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.os.Parcel;
import android.os.Parcelable;

public class Program implements Parcelable, Comparable<Program>
{
	// Bean properties
	private String _startTime;
	private String _stopTime;
	private String _title;

	// Other internal properties
	private Calendar _startTimeCalendar;
	private Calendar _stopTimeCalendar;

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

	public static final Parcelable.Creator<Program> CREATOR = new Parcelable.Creator<Program>()
	{
		@Override
		public Program createFromParcel(Parcel in)
		{
			return new Program(in);
		}

		@Override
		public Program[] newArray(int size)
		{
			return new Program[size];
		}
	};

	@Override
    public int compareTo(Program another)
    {
	    return _startTime.compareTo(another._startTime);
    }

	public Calendar getStartTimeCalendar()
	{
		return _startTimeCalendar;
	}

	public Calendar getStopTimeCalendar()
	{
		return _stopTimeCalendar;
	}

	public static Calendar getEpgTime(String epgTime)
	{
		Calendar cal;
        try
        {
        	DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
	        Date dte = dateFormat.parse(epgTime);
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
    	DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    	String dateTime = dateFormat.format(cal.getTime());
        return dateTime;
	}

	public static String getEpgTime(long millis)
	{
    	DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    	String dateTime = dateFormat.format(new Date(millis));
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
        _startTimeCalendar = getEpgTime(_startTime);
	}

	public String getStopTime()
	{
		return _stopTime;
	}

	public void setStopTime(String stopTime) throws ParseException
	{
		_stopTime = stopTime;
		_stopTimeCalendar = getEpgTime(_stopTime);
	}

	public String getTitle()
	{
		return _title;
	}

	public void setTitle(String title)
	{
		_title = title;
	}
}
