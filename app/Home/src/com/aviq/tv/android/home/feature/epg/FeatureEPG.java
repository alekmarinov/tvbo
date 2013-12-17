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

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageRequest;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.feature.FeatureComponent;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureName.Component;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Component feature providing EPG data
 */
public abstract class FeatureEPG extends FeatureComponent
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

	private RequestQueue _httpQueue;
	private OnFeatureInitialized _onFeatureInitialized;
	private int _epgVersion;
	private String _epgServer;
	private String _epgProvider;
	private int _channelLogoWidth;
	private int _channelLogoHeight;

	// used to detect when all channel logos are retrieved with success or error
	private int _retrievedChannelLogos;

	// used to detect when all channel programs are retrieved with success or
	// error
	private int _retrievedChannelPrograms;

	private EpgData _epgData;
	private EpgData _epgDataBeingLoaded;
	private ChannelMetaData _channelsMeta = new ChannelMetaData();
	private ProgramMetaData _programsMeta = new ProgramMetaData();

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");

		super.initialize(onFeatureInitialized);
		_onFeatureInitialized = onFeatureInitialized;

		_epgVersion = getPrefs().getInt(Param.EPG_VERSION);
		_epgServer = getPrefs().getString(Param.EPG_SERVER);
		_epgProvider = getPrefs().getString(Param.EPG_PROVIDER);
		_channelLogoWidth = getPrefs().getInt(Param.CHANNEL_LOGO_WIDTH);
		_channelLogoHeight = getPrefs().getInt(Param.CHANNEL_LOGO_HEIGHT);

		_httpQueue = Environment.getInstance().getRequestQueue();

		retrieveChannels();
	}

	public abstract String getChannelStreamUrl(int channelIndex);

	private void retrieveChannels()
	{
		String channelsUrl = getChannelsUrl();
		Log.i(TAG, "Retrieving EPG channels from " + channelsUrl);
		ChannelListResponseCallback responseCallback = new ChannelListResponseCallback();

		GsonRequest<ChannelListResponse> channelListRequest = new GsonRequest<ChannelListResponse>(Request.Method.GET,
		        channelsUrl, ChannelListResponse.class, responseCallback, responseCallback);

		_httpQueue.add(channelListRequest);
	}

	private void retrieveChannelLogo(Channel channel, int channelIndex)
	{
		String channelId = channel.getChannelId();
		String channelLogo = channel.getThumbnail();

		String channelLogoUrl = getChannelsLogoUrl(channelId, channelLogo);
		Log.d(TAG, "Retrieving channel logo from " + channelLogoUrl);

		LogoResponseCallback responseCallback = new LogoResponseCallback(channelId, channelIndex);

		ImageRequest imageRequest = new ImageRequest(channelLogoUrl, responseCallback, _channelLogoWidth,
		        _channelLogoHeight, Config.ARGB_8888, responseCallback);

		_httpQueue.add(imageRequest);
	}

	private void retrievePrograms(Channel channel)
	{
		String channelId = channel.getChannelId();
		String programsUrl = getProgramsUrl(channelId);
		Log.d(TAG, "Retrieving programs from " + programsUrl);

		ProgramsResponseCallback responseCallback = new ProgramsResponseCallback(channelId);

		GsonRequest<ProgramsResponse> programsRequest = new GsonRequest<ProgramsResponse>(Request.Method.GET,
		        programsUrl, ProgramsResponse.class, responseCallback, responseCallback);

		_httpQueue.add(programsRequest);
	}

	private class ChannelListResponseCallback implements Response.Listener<ChannelListResponse>, Response.ErrorListener
	{
		@Override
		public void onResponse(ChannelListResponse response)
		{
			parseChannelListMetaData(response.meta);
			parseChannelData(response.data);

			final int nChannels = _epgDataBeingLoaded.getChannelCount();
			Log.i(TAG, "Response with " + nChannels + " channels received");
			_retrievedChannelLogos = 0;
			_retrievedChannelPrograms = 0;

			for (int i = 0; i < nChannels; i++)
			{
				Channel channel = _epgDataBeingLoaded.getChannel(i);
				retrieveChannelLogo(channel, i);
				retrievePrograms(channel);
			}
		}

		@Override
		public void onErrorResponse(VolleyError error)
		{
			int statusCode = error.networkResponse != null ? error.networkResponse.statusCode
			        : ResultCode.GENERAL_FAILURE;
			Log.e(TAG, "Error retrieving channels with code " + statusCode + ": " + error);
			_onFeatureInitialized.onInitialized(FeatureEPG.this, statusCode);
		}
	}

	private class LogoResponseCallback implements Response.Listener<Bitmap>, Response.ErrorListener
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
			Log.d(TAG, "Received bitmap " + response.getWidth() + "x" + response.getHeight());
			_epgDataBeingLoaded.setChannelLogo(_index, response);
			logoProcessed();
		}

		@Override
		public void onErrorResponse(VolleyError error)
		{
			Log.d(TAG, "Retrieve channel logo " + _channelId + " with error: " + error);
			logoProcessed();
		}

		private void logoProcessed()
		{
			_retrievedChannelLogos++;
			checkInitializeFinished();
		}
	};

	private class ProgramsResponseCallback implements Response.Listener<ProgramsResponse>, Response.ErrorListener
	{
		private String _channelId;

		ProgramsResponseCallback(String channelId)
		{
			_channelId = channelId;
		}

		@Override
		public void onResponse(ProgramsResponse response)
		{
			Log.d(TAG, "Received programs for channel " + _channelId);
			parseProgramsMetaData(response.meta);
			parseProgramsData(_channelId, response.data);
			programsProcessed();
		}

		@Override
		public void onErrorResponse(VolleyError error)
		{
			Log.w(TAG, "Error " + error + " retrieving programs for " + _channelId);
			programsProcessed();
		}

		private void programsProcessed()
		{
			_retrievedChannelPrograms++;
			checkInitializeFinished();
		}
	}

	private void checkInitializeFinished()
	{
		int numChannels = _epgDataBeingLoaded.getChannelCount();
		if (_retrievedChannelPrograms == numChannels && _retrievedChannelLogos == numChannels)
		{
			// Forget the old EpgData object, from now on work with the new
			// one. Anyone else holding a reference to the old object will
			// be able to finish its job. Then the garbage collector will
			// free up the memory.

			_epgData = _epgDataBeingLoaded;
// TODO: Uncomment this if "parseProgramData()" is going to work without the AsyncTask logic
			_epgDataBeingLoaded = null;
			_retrievedChannelPrograms = 0;
			_retrievedChannelLogos = 0;

			_onFeatureInitialized.onInitialized(FeatureEPG.this, ResultCode.OK);
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
				_channelsMeta.metaChannelId = j;
			else if ("title".equals(key))
				_channelsMeta.metaChannelTitle = j;
			else if ("thumbnail".equals(key))
				_channelsMeta.metaChannelThumbnail = j;
			else
				Log.w(TAG, "Unknown channel column `" + key + "`");
		}
	}

	private void parseChannelData(String[][] data)
	{
		List<Channel> newChannelList = new ArrayList<Channel>();
		for (int i = 0; i < data.length; i++)
		{
			Channel channel = new Channel();
			channel.setChannelId(data[i][_channelsMeta.metaChannelId]);
			channel.setTitle(data[i][_channelsMeta.metaChannelTitle]);
			channel.setThumbnail(data[i][_channelsMeta.metaChannelThumbnail]);
			newChannelList.add(channel);
		}

		_epgDataBeingLoaded = new EpgData(newChannelList);
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
				_programsMeta.metaStart = j;
			else if ("stop".equals(key))
				_programsMeta.metaStop = j;
			else if ("title".equals(key))
				_programsMeta.metaTitle = j;
			else
				Log.w(TAG, "Unknown program column `" + key + "`");
		}
	}

	private void parseProgramsData(final String channelId, final String[][] data)
	{
//		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>()
//		{
//			private NavigableMap<String, Integer> _programMap = new TreeMap<String, Integer>();
//			private List<Program> _programList = new ArrayList<Program>();
//			private long _processStart;
//			private long _processEnd;
//
//			@Override
//            protected Void doInBackground(Void... params)
//            {
//				_processStart = System.nanoTime();
//
//				for (int i = 0; i < data.length; i++)
//				{
//					Program program = new Program();
//					program.setTitle(data[i][_programsMeta.metaTitle]);
//
//					try
//					{
//						program.setStartTime(data[i][_programsMeta.metaStart]);
//					}
//					catch (ParseException e)
//					{
//						Log.w(TAG, "Undefined start time for program: " + program.getTitle() + " on channel: " + channelId);
//					}
//
//					try
//					{
//						program.setStopTime(data[i][_programsMeta.metaStop]);
//					}
//					catch (ParseException e)
//					{
//						Log.w(TAG, "Undefined stop time for program: " + program.getTitle() + " on channel: " + channelId);
//					}
//
//					_programList.add(program);
//					_programMap.put(program.getStartTime(), i);
//				}
//
//	            return null;
//            }
//
//			@Override
//			protected void onPostExecute(Void result)
//			{
//				if (_epgDataBeingLoaded == null)
//				{
//					Log.e(TAG, "_epgDataBeingLoaded is NULL");
//					return;
//				}
//				_epgDataBeingLoaded.addProgramNavigableMap(channelId, _programMap);
//				_epgDataBeingLoaded.addProgramList(channelId, _programList);
//
//				_processEnd = System.nanoTime();
//				double processTime = (_processEnd - _processStart) / 1000000000.0;
//				Log.e(TAG, "Parsed " + data.length + " program items for channel " + channelId + " for " + processTime + " sec");
//			}
//		};
//
//		task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, (Void) null);

		long processStart = System.nanoTime();

		NavigableMap<String, Integer> programMap = new TreeMap<String, Integer>();
		List<Program> programList = new ArrayList<Program>();

		for (int i = 0; i < data.length; i++)
		{
			Program program = new Program();
			program.setTitle(data[i][_programsMeta.metaTitle]);

			try
			{
				program.setStartTime(data[i][_programsMeta.metaStart]);
			}
			catch (ParseException e)
			{
				Log.w(TAG, "Undefined start time for program: " + program.getTitle() + " on channel: " + channelId);
			}

			try
			{
				program.setStopTime(data[i][_programsMeta.metaStop]);
			}
			catch (ParseException e)
			{
				Log.w(TAG, "Undefined stop time for program: " + program.getTitle() + " on channel: " + channelId);
			}

			programList.add(program);
			programMap.put(program.getStartTime(), i);
		}

		_epgDataBeingLoaded.addProgramNavigableMap(channelId, programMap);
		_epgDataBeingLoaded.addProgramList(channelId, programList);

		long processEnd = System.nanoTime();
		double processTime = (processEnd - processStart) / 1000000000.0;
		Log.d(TAG, "Parsed " + data.length + " program items for channel " + channelId + " for " + processTime + " sec");
	}

	private String getChannelsUrl()
	{
		Bundle bundle = new Bundle();
		bundle.putString("SERVER", _epgServer);
		bundle.putInt("VERSION", _epgVersion);
		bundle.putString("PROVIDER", _epgProvider);

		return getPrefs().getString(Param.EPG_CHANNELS_URL, bundle);
	}

	private String getChannelsLogoUrl(String channelId, String channelLogo)
	{
		Bundle bundle = new Bundle();
		bundle.putString("SERVER", _epgServer);
		bundle.putString("CHANNEL", channelId);
		bundle.putString("PROVIDER", _epgProvider);
		bundle.putString("CHANNEL", channelId);
		bundle.putString("LOGO", channelLogo);

		return getPrefs().getString(Param.EPG_CHANNEL_LOGO_URL, bundle);
	}

	private String getProgramsUrl(String channelId)
	{
		Bundle bundle = new Bundle();
		bundle.putString("SERVER", _epgServer);
		bundle.putInt("VERSION", _epgVersion);
		bundle.putString("PROVIDER", _epgProvider);
		bundle.putString("CHANNEL", channelId);

		return getPrefs().getString(Param.EPG_PROGRAMS_URL, bundle);
	}

	public EpgData getEpgData()
	{
		return _epgData;
	}

	@Override
	public Component getComponentName()
	{
		return FeatureName.Component.EPG;
	}

	private static class ChannelMetaData
	{
		public int metaChannelId;
		public int metaChannelTitle;
		public int metaChannelThumbnail;
	}

	private static class ProgramMetaData
	{
		public int metaStart;
		public int metaStop;
		public int metaTitle;
	}

	// GSON entity class of channel list response
	private class ChannelListResponse
	{
		public String[] meta;
		public String[][] data;
	}

	// GSON entity class of programs response
	private class ProgramsResponse
	{
		public String[] meta;
		public String[][] data;
	}

	// GSON volley request
	private class GsonRequest<T> extends Request<T>
	{
		private final Gson mGson;
		private final Class<T> mClazz;
		private final Listener<T> mListener;

		public GsonRequest(int method, String url, Class<T> clazz, Listener<T> listener, ErrorListener errorListener)
		{
			super(Method.GET, url, errorListener);
			this.mClazz = clazz;
			this.mListener = listener;
			mGson = new Gson();
		}

		public GsonRequest(int method, String url, Class<T> clazz, Listener<T> listener, ErrorListener errorListener,
		        Gson gson)
		{
			super(Method.GET, url, errorListener);
			this.mClazz = clazz;
			this.mListener = listener;
			mGson = gson;
		}

		@Override
		protected void deliverResponse(T response)
		{
			mListener.onResponse(response);
		}

		@Override
		protected Response<T> parseNetworkResponse(NetworkResponse response)
		{
			try
			{
				String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
				return Response.success(mGson.fromJson(json, mClazz), HttpHeaderParser.parseCacheHeaders(response));
			}
			catch (UnsupportedEncodingException e)
			{
				return Response.error(new ParseError(e));
			}
			catch (JsonSyntaxException e)
			{
				return Response.error(new ParseError(e));
			}
		}
	}
}
