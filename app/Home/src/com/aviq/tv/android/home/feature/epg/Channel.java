package com.aviq.tv.android.home.feature.epg;

import android.os.Parcel;
import android.os.Parcelable;

public class Channel implements Parcelable
{
	private String _channelId;
	private String _title;
	private String _thumbnail;
	
	public Channel()
	{
	}
	
	public Channel(Parcel in)
	{
		readFromParcel(in);
	}
	
	public void readFromParcel(Parcel in)
	{
		_channelId = in.readString();
		_title = in.readString();
		_thumbnail = in.readString();
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(_channelId);
		dest.writeString(_title);
		dest.writeString(_thumbnail);
	}
	
	@Override
	public int describeContents()
	{
		return 0;
	}
	
	public static final Parcelable.Creator<Channel> CREATOR = new Parcelable.Creator<Channel>()
	{
		@Override
		public Channel createFromParcel(Parcel in)
		{
			return new Channel(in);
		}
		
		@Override
		public Channel[] newArray(int size)
		{
			return new Channel[size];
		}
	};
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Channel))
			return false;
		
		Channel other = (Channel) obj;
		return (_channelId != null && _channelId.equals(other._channelId))
		        && (_title != null && _title.equals(other._title))
		        && (_thumbnail != null && _thumbnail.equals(other._thumbnail));
	}
	
	@Override
	public int hashCode()
	{
		int result = 0;
		result = 31 * result + (_channelId != null ? _channelId.hashCode() : 0);
		result = 31 * result + (_title != null ? _title.hashCode() : 0);
		result = 31 * result + (_thumbnail != null ? _thumbnail.hashCode() : 0);
		return result;
	}
	
	public String getChannelId()
	{
		return _channelId;
	}
	
	public void setChannelId(String channelId)
	{
		_channelId = channelId;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
	
	public String getThumbnail()
	{
		return _thumbnail;
	}
	
	public void setThumbnail(String thumbnail)
	{
		_thumbnail = thumbnail;
	}
}
