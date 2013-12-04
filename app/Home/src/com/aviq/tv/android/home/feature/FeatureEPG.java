/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureEPG.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Component feature providing EPG data
 */

package com.aviq.tv.android.home.feature;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.FeatureComponent;
import com.aviq.tv.android.home.core.FeatureName;
import com.aviq.tv.android.home.core.FeatureName.Component;
import com.aviq.tv.android.home.core.FeatureSet;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.feature.epg.api.ChannelListResponse;
import com.aviq.tv.android.home.feature.epg.api.GsonRequest;
import com.aviq.tv.android.home.feature.epg.api.ProgramsResponse;
import com.aviq.tv.android.home.utils.Param;
import com.aviq.tv.android.home.utils.Prefs;

/**
 * Component feature providing EPG data
 *
 */
public class FeatureEPG extends FeatureComponent
{
	public static final String TAG = FeatureEPG.class.getSimpleName();
	private FeatureSet _dependencies = new FeatureSet();
	private String[][] _channelsData;
	private int _metaChannelId;
	private int _metaChannelTitle;
	private int _metaChannelThumbnail;
	private Bitmap[] _channelLogos;
	private int _retrievedLogos = 0;
	private OnFeatureInitialized _onFeatureInitialized;
	private Prefs _prefs;
	private String _epgVersion;
	private String _epgServer;
	private String _epgProvider;
	private String[][] _programsData;
	private int _metaProgramStart;
	private int _metaProgramStop;
	private int _metaProgramTitle;

	public FeatureEPG(Environment environment)
	{
		super(environment);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		super.initialize(onFeatureInitialized);

		_onFeatureInitialized = onFeatureInitialized;

		_prefs = _environment.getPrefs();
		_epgVersion = _prefs.getString(Param.System.EPG_VERSION);
		_epgServer = _prefs.getString(Param.System.EPG_SERVER);
		_epgProvider = _prefs.getString(Param.System.EPG_PROVIDER);

		final RequestQueue queue = _environment.getRequestQueue();

		// Retrieve EPG channels

		String channelsUrl = getChannelsUrl();
		GsonRequest<ChannelListResponse> channelListRequest = new GsonRequest<ChannelListResponse>(Request.Method.GET,
		        channelsUrl, ChannelListResponse.class, new ChannelListResponseCallback(),
		        new ChannelListResponseErrorCallback());
		queue.add(channelListRequest);

		// Retrieve EPG programs

		String programsUrl = getChannelsUrl();
		GsonRequest<ProgramsResponse> programsRequest = new GsonRequest<ProgramsResponse>(Request.Method.GET,
		        programsUrl, ProgramsResponse.class, new ProgramsResponseCallback(),
		        new ProgramsResponseErrorCallback());
		queue.add(programsRequest);
	}

	private class ChannelListResponseCallback implements Response.Listener<ChannelListResponse>
    {
        @Override
        public void onResponse(ChannelListResponse response)
        {
        	parseChannelListMetaData(response.meta);
        	parseChannelData(response.data);

	        // Get all channel logos
	        final RequestQueue queue = _environment.getRequestQueue();
	        final int nChannels = getChannelCount();
	        _channelLogos = new Bitmap[nChannels];
	        _retrievedLogos = 0;

	        for (int i = 0; i < nChannels; i++)
	        {
		        final String channelId = getChannelId(i);
		        String channelLogo = getChannelLogoName(i);
		        String channelLogoUrl = getChannelsLogoUrl(channelId, channelLogo);
		        Log.i(TAG, "Retrieving channel logo " + channelLogoUrl);

		        // FIXME: Move image size (320x240) to constants
		        ImageRequest imageRequest = new ImageRequest(channelLogoUrl,
		                new LogoResponseCallback(channelId, i), 320, 240, Config.ARGB_8888, new LogoResponseErrorCallback(
		                        channelId, nChannels));
		        queue.add(imageRequest);
	        }
        }
    }

	private class ChannelListResponseErrorCallback implements Response.ErrorListener
    {
        @Override
        public void onErrorResponse(VolleyError error)
        {
	        _onFeatureInitialized.onInitialized(FeatureEPG.this, error.networkResponse.statusCode);
        }
    }

	private class LogoResponseCallback implements Response.Listener<Bitmap>
	{
		private int _index;
		private String _channelId;

		LogoResponseCallback(String channelId, int index)
		{
			_channelId = channelId;
			_index = index;
		}

		@Override
        public void onResponse(Bitmap response)
        {
			Log.i(TAG, "Received bitmap " + response.getWidth() + "x" + response.getHeight());
			_channelLogos[_index] = response;
			_retrievedLogos++;
			if (_retrievedLogos == getChannelCount())
			{
				Log.i(TAG, "Last channel logo with idx " + _index + " retrieved successfully");
				_onFeatureInitialized.onInitialized(FeatureEPG.this, ResultCode.OK);
			}
			else
			{
				Log.i(TAG, "Retrieved channel logo with idx " + _index);
			}
        }
	};

	private class LogoResponseErrorCallback implements Response.ErrorListener
	{
		private String _channelId;
		private int _totalChannels;

		LogoResponseErrorCallback(String channelId, int totalChannels)
		{
			_channelId = channelId;
			_totalChannels = totalChannels;
		}

		@Override
        public void onErrorResponse(VolleyError error)
        {
			_retrievedLogos++;
			if (_retrievedLogos == _totalChannels)
			{
				Log.i(TAG, "Last channel logo retrieved with error: " + error.getMessage());
				_onFeatureInitialized.onInitialized(FeatureEPG.this, ResultCode.OK);
			}
			else
			{
				Log.i(TAG, "Retrieve channel logo " + _channelId + " with error: " + error);
			}
        }
	}

	private class ProgramsResponseCallback implements Response.Listener<ProgramsResponse>
    {
        @Override
        public void onResponse(ProgramsResponse response)
        {
        	parseProgramsMetaData(response.meta);
        	parseProgramsData(response.data);
        }
    }

	private class ProgramsResponseErrorCallback implements Response.ErrorListener
    {
        @Override
        public void onErrorResponse(VolleyError error)
        {
	        _onFeatureInitialized.onInitialized(FeatureEPG.this, error.networkResponse.statusCode);
        }
    }

	private void parseChannelListMetaData(String[] meta)
	{
		if (meta == null)
		{
			Log.e(TAG, "Channel meta data is NULL.");
			return;
		}

		for (int j = 0; j < meta.length; j++)
        {
	        String key = meta[j];

	        if ("id".equals(key))
		        _metaChannelId = j;
	        else if ("title".equals(key))
		        _metaChannelTitle = j;
	        else if ("thumbnail".equals(key))
		        _metaChannelThumbnail = j;
	        else
		        Log.w(TAG, "Unknown channel column `" + key + "`");
        }
	}

	private void parseChannelData(String[][] data)
	{
		_channelsData = data;
	}

	private void parseProgramsMetaData(String[] meta)
	{
		if (meta == null)
		{
			Log.e(TAG, "Programs meta data is NULL.");
			return;
		}

		for (int j = 0; j < meta.length; j++)
        {
	        String key = meta[j];

	        if ("start".equals(key))
		        _metaProgramStart = j;
	        else if ("stop".equals(key))
	        	_metaProgramStop = j;
	        else if ("title".equals(key))
	        	_metaProgramTitle = j;
	        else
		        Log.w(TAG, "Unknown program column `" + key + "`");
        }
	}

	private void parseProgramsData(String[][] data)
	{
		_programsData = data;
	}

	private String getChannelsUrl()
	{
		Bundle bundle = new Bundle();
		bundle.putString("SERVER", _epgServer);
		bundle.putString("VERSION", _epgVersion);
		bundle.putString("PROVIDER", _epgProvider);

		String channelsUrl = _prefs.getString(Param.System.EPG_CHANNELS_URL, bundle);
		Log.i(TAG, "Retrieving EPG channels from " + channelsUrl);

		return channelsUrl;
	}

	private String getChannelsLogoUrl(String channelId, String channelLogo)
	{
		Bundle bundle = new Bundle();
		bundle.putString("SERVER", _epgServer);
		bundle.putString("VERSION", _epgVersion);
		bundle.putString("PROVIDER", _epgProvider);
		bundle.putString("CHANNEL", channelId);
		bundle.putString("LOGO", channelLogo);

		String channelLogoUrl = _prefs.getString(Param.System.EPG_CHANNEL_LOGO_URL, bundle);
        Log.i(TAG, "Retrieving channel logo from " + channelLogoUrl);

		return channelLogoUrl;
	}

	private String getProgramsUrl(String channelId)
	{
		Bundle bundle = new Bundle();
		bundle.putString("SERVER", _epgServer);
		bundle.putString("VERSION", _epgVersion);
		bundle.putString("PROVIDER", _epgProvider);
		bundle.putString("CHANNEL", channelId);

		String programsUrl = _prefs.getString(Param.System.EPG_PROGRAMS_URL, bundle);
        Log.i(TAG, "Retrieving programs from " + programsUrl);

		return programsUrl;
	}

	@Override
	public FeatureSet dependencies()
	{
		return _dependencies;
	}

	@Override
    public Component getId()
    {
	    return FeatureName.Component.EPG;
    }

    /**
     * @return the number of channels
     */
    public int getChannelCount()
    {
    	return _channelsData.length;
    }

    /**
     * Return channel id for specified channel index
     *
     * @param index
     * @return String
     */
    public String getChannelId(int index)
    {
    	return _channelsData[index][_metaChannelId];
    }

    /**
     * Return channel title for specified channel index
     *
     * @param index
     * @return String
     */
    public String getChannelTitle(int index)
    {
    	return _channelsData[index][_metaChannelTitle];
    }

    /**
     * Return channel logo name for specified channel index
     *
     * @param index
     * @return String
     */
    public String getChannelLogoName(int index)
    {
        return _channelsData[index][_metaChannelThumbnail];
    }

    /**
     * Return channel logo bitmap for specified channel index
     *
     * @param index
     * @return Bitmap
     */
    public Bitmap getChannelLogoBitmap(int index)
    {
    	if (_channelLogos != null && index < _channelLogos.length)
    		return _channelLogos[index];
    	return null;
    }
}
