/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureEPG.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Component feature providing EPG data
 */

package com.aviq.tv.android.home.feature.epg;

import java.util.HashMap;
import java.util.Map;

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
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.feature.FeatureComponent;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureName.Component;
import com.aviq.tv.android.home.core.feature.FeatureSet;

/**
 * Component feature providing EPG data
 */
public class FeatureEPG extends FeatureComponent
{
	public static final String TAG = FeatureEPG.class.getSimpleName();

	public enum Param
	{
		/**
		 * The name of the EPG provider, e.g. rayv, wilmaa, generic
		 */
		EPG_PROVIDER("rayv"),

		/**
		 * The main url to the EPG server
		 */
		EPG_SERVER("http://epg.aviq.bg"),

		/**
		 * The EPG service version
		 */
		EPG_VERSION(1),

		/**
		 * EPG channels url format
		 */
		EPG_CHANNELS_URL("${SERVER}/v${VERSION}/channels/${PROVIDER}"),

		/**
		 * EPG channel logo url format
		 */
		EPG_CHANNEL_LOGO_URL("${SERVER}/static/${PROVIDER}/${CHANNEL}/${LOGO}"),

		/**
		 * EPG programs url format
		 */
		EPG_PROGRAMS_URL("${SERVER}/v${VERSION}/programs/${PROVIDER}/${CHANNEL}"),

		/**
		 * Channel logo width
		 */
		CHANNEL_LOGO_WIDTH(80),

		/**
		 * Channel logo height
		 */
		CHANNEL_LOGO_HEIGHT(50);

		Param(int value)
		{
			Environment.getInstance().getFeaturePrefs(FeatureName.Component.EPG).put(name(), value);
		}

		Param(String value)
		{
			Environment.getInstance().getFeaturePrefs(FeatureName.Component.EPG).put(name(), value);
		}
	}

	private FeatureSet _dependencies = new FeatureSet();
	private String[][] _channelsData;
	private int _metaChannelId;
	private int _metaChannelTitle;
	private int _metaChannelThumbnail;
	private Bitmap[] _channelLogos;
	private int _retrievedLogos = 0;
	private OnFeatureInitialized _onFeatureInitialized;
	private int _epgVersion;
	private String _epgServer;
	private String _epgProvider;
	private Map<String, String[][]> _programsData = new HashMap<String, String[][]>();

	// FIXME: Decide how to use later when needed
	private int _metaProgramStart;
	private int _metaProgramStop;
	private int _metaProgramTitle;

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");

		super.initialize(onFeatureInitialized);
		_onFeatureInitialized = onFeatureInitialized;

		_epgVersion = getPrefs().getInt(Param.EPG_VERSION);
		_epgServer = getPrefs().getString(Param.EPG_SERVER);
		_epgProvider = getPrefs().getString(Param.EPG_PROVIDER);

		final RequestQueue queue = Environment.getInstance().getRequestQueue();

		// Retrieve EPG channels
		String channelsUrl = getChannelsUrl();
		GsonRequest<ChannelListResponse> channelListRequest = new GsonRequest<ChannelListResponse>(Request.Method.GET,
		        channelsUrl, ChannelListResponse.class, new ChannelListResponseCallback(),
		        new ChannelListResponseErrorCallback());
		queue.add(channelListRequest);
	}

	private class ChannelListResponseCallback implements Response.Listener<ChannelListResponse>
	{
		@Override
		public void onResponse(ChannelListResponse response)
		{
			parseChannelListMetaData(response.meta);
			parseChannelData(response.data);

			// Get all channel logos
			final RequestQueue queue = Environment.getInstance().getRequestQueue();
			final int nChannels = getChannelCount();
			_channelLogos = new Bitmap[nChannels];
			_retrievedLogos = 0;

			int channelLogoWidth = getPrefs().getInt(Param.CHANNEL_LOGO_WIDTH);
			int channelLogoHeight = getPrefs().getInt(Param.CHANNEL_LOGO_HEIGHT);

			for (int i = 0; i < nChannels; i++)
			{
				final String channelId = getChannelId(i);
				String channelLogo = getChannelLogoName(i);
				String channelLogoUrl = getChannelsLogoUrl(channelId, channelLogo);
				Log.i(TAG, "Retrieving channel logo " + channelLogoUrl);

				ImageRequest imageRequest = new ImageRequest(channelLogoUrl, new LogoResponseCallback(channelId, i),
						channelLogoWidth, channelLogoHeight, Config.ARGB_8888, new LogoResponseErrorCallback(channelId, nChannels));
				queue.add(imageRequest);

				// Retrieve EPG programs
				String programsUrl = getProgramsUrl(channelId);
				GsonRequest<ProgramsResponse> programsRequest = new GsonRequest<ProgramsResponse>(Request.Method.GET,
				        programsUrl, ProgramsResponse.class, new ProgramsResponseCallback(channelId),
				        new ProgramsResponseErrorCallback());
				queue.add(programsRequest);
			}
		}
	}

	private class ChannelListResponseErrorCallback implements Response.ErrorListener
	{
		@Override
		public void onErrorResponse(VolleyError error)
		{
			_onFeatureInitialized.onInitialized(FeatureEPG.this,
			        error.networkResponse != null ? error.networkResponse.statusCode : ResultCode.GENERAL_FAILURE);
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
		private String _channelId;

		ProgramsResponseCallback(String channelId)
		{
			_channelId = channelId;
		}

		@Override
		public void onResponse(ProgramsResponse response)
		{
			parseProgramsMetaData(response.meta);
			parseProgramsData(_channelId, response.data);
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

	private void parseProgramsData(String channelId, String[][] data)
	{
		_programsData.put(channelId, data);
	}

	private String getChannelsUrl()
	{
		Bundle bundle = new Bundle();
		bundle.putString("SERVER", _epgServer);
		bundle.putInt("VERSION", _epgVersion);
		bundle.putString("PROVIDER", _epgProvider);

		String channelsUrl = getPrefs().getString(Param.EPG_CHANNELS_URL, bundle);
		Log.i(TAG, "Retrieving EPG channels from " + channelsUrl);

		return channelsUrl;
	}

	private String getChannelsLogoUrl(String channelId, String channelLogo)
	{
		Bundle bundle = new Bundle();
		bundle.putString("SERVER", _epgServer);
		bundle.putString("CHANNEL", channelId);
		bundle.putString("PROVIDER", _epgProvider);
		bundle.putString("CHANNEL", channelId);
		bundle.putString("LOGO", channelLogo);

		String channelLogoUrl = getPrefs().getString(Param.EPG_CHANNEL_LOGO_URL, bundle);
		Log.i(TAG, "Retrieving channel logo from " + channelLogoUrl);

		return channelLogoUrl;
	}

	private String getProgramsUrl(String channelId)
	{
		Bundle bundle = new Bundle();
		bundle.putString("SERVER", _epgServer);
		bundle.putInt("VERSION", _epgVersion);
		bundle.putString("PROVIDER", _epgProvider);
		bundle.putString("CHANNEL", channelId);

		String programsUrl = getPrefs().getString(Param.EPG_PROGRAMS_URL, bundle);
		Log.i(TAG, "Retrieving programs from " + programsUrl);

		return programsUrl;
	}

	@Override
	public FeatureSet dependencies()
	{
		return _dependencies;
	}

	@Override
	public Component getComponentName()
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
