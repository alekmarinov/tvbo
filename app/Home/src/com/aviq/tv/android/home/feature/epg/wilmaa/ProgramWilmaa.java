/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    ProgramWilmaa.java
 * Author:      alek
 * Date:        19 Dec 2013
 * Description: Wilmaa specific program data holder class
 */

package com.aviq.tv.android.home.feature.epg.wilmaa;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.aviq.tv.android.home.feature.epg.Program;
import com.aviq.tv.android.home.feature.epg.ProgramAttribute;
import com.aviq.tv.android.home.utils.Log;

/**
 * RayV specific program data holder class
 */
public class ProgramWilmaa extends Program
{
	public static int MAX_SUMMARY_LENGTH = 100;
	public static int MIN_SUMMARY_LENGTH = 20;

	private static final String TAG = ProgramWilmaa.class.getSimpleName();
	private JSONObject _detailsResponse;

	public static final Parcelable.Creator<Program> CREATOR = new Parcelable.Creator<Program>()
	{
		@Override
		public Program createFromParcel(Parcel in)
		{
			return new ProgramWilmaa(in);
		}

		@Override
		public Program[] newArray(int size)
		{
			return new Program[size];
		}
	};

	public ProgramWilmaa()
	{
		super();
	}

	public ProgramWilmaa(Parcel in)
	{
		super(in);
	}

	@Override
	public boolean hasDetails()
	{
		return _detailsResponse != null;
	}

	@Override
	public void setDetails(JSONObject detailsResponse)
	{
		_detailsResponse = detailsResponse;
	}

	@Override
	public String getDetailAttribute(ProgramAttribute programAttribute)
	{
		if (_detailsResponse == null)
		{
			Log.w(TAG, "Requesting attribute " + programAttribute + " before setting program attributes");
			return null;
		}

		try
		{
			switch (programAttribute)
			{
				case SUBTITLE:
					return _detailsResponse.getString("subtitle");
				case SUMMARY:
				{
					String description = _detailsResponse.getString("description");
					if (description != null)
					{
						int endPos = MAX_SUMMARY_LENGTH;
						int pntPos = description.indexOf('.');
						if (pntPos > 0)
						{
							int bestPntPos = pntPos;
							while (pntPos < MAX_SUMMARY_LENGTH)
							{
								int newPos = description.indexOf('.', pntPos + 1);
								if (newPos < 0)
									break;
								bestPntPos = pntPos;
								pntPos = newPos;
							}
							endPos = Math.min(Math.max(bestPntPos, MIN_SUMMARY_LENGTH), MAX_SUMMARY_LENGTH);
						}
						endPos = Math.min(description.length() - 1, endPos);
						return description.substring(0, endPos);
					}
				}
				case DESCRIPTION:
					return _detailsResponse.getString("description");
				case GENRE:
				{
					return getAttributeList("category");
				}
				case IMAGE_URL:
				// not supported
				break;
				case SEASON:
				// not supported
				break;
				case SERIE:
				// not supported
				break;
				case DATE:
					return _detailsResponse.getString("date");
				case CREDITS_ACTORS:
					return getAttributeList("actor");
				case CREDITS_PRESENTER:
					return _detailsResponse.getString("credits.presenter");
				case CREDITS_DIRECTOR:
					return _detailsResponse.getString("credits.director");
				case ASPECT_RATIO:
					return _detailsResponse.getString("video.aspect");
			}
		}
		catch (JSONException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}

	private String getAttributeList(String key) throws JSONException
	{
		StringBuffer result = new StringBuffer();
		String element = _detailsResponse.getString(key + ".1");
		int index = 1;
		while (element != null)
		{
			if (index > 1)
				result.append(", ");
			result.append(element);
			index++;
			element = _detailsResponse.getString(key + "." + index);
		}
		return result.toString();
	}

	@Override
    public void setDetailAttributes(MetaData metaData, String[] attributes)
    {
		// no Wilmaa specific channel attributes
    }
}
