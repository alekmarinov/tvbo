package com.aviq.tv.android.home.feature.epg;

import android.os.Parcel;
import android.os.Parcelable;

public class Program implements Parcelable, Comparable<Program>
{
	private String _startTime;
	private String _stopTime;
	private String _title;
	
	public Program()
	{
	}
	
	public Program(Parcel in)
	{
		readFromParcel(in);
	}
	
	public void readFromParcel(Parcel in)
	{
		_startTime = in.readString();
		_stopTime = in.readString();
		_title = in.readString();
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
	
	public String getStartTime()
	{
		return _startTime;
	}
	
	public void setStartTime(String startTime)
	{
		_startTime = startTime;
	}
	
	public String getStopTime()
	{
		return _stopTime;
	}
	
	public void setStopTime(String stopTime)
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

	@Override
    public int compareTo(Program another)
    {
	    return _startTime.compareTo(another._startTime);
    }
}
