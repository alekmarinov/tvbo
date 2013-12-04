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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.FeatureComponent;
import com.aviq.tv.android.home.core.FeatureName;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.FeatureName.Component;
import com.aviq.tv.android.home.utils.Param;
import com.aviq.tv.android.home.utils.Prefs;

/**
 * Component feature providing EPG data
 *
 */
public class FeatureEPG extends FeatureComponent
{
	public static final String TAG = FeatureEPG.class.getSimpleName();
	private JSONArray _channelsMeta;
	private JSONArray _channelsData;
	private int _metaChannelId;
	private int _metaChannelTitle;
	private int _metaChannelThumbnail;
	private Bitmap[] _channelLogos;
	private int _retrievedLogos = 0;
	private OnFeatureInitialized _onFeatureInitialized;

	public FeatureEPG(Environment environment)
	{
		super(environment);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		super.initialize(onFeatureInitialized);
		_onFeatureInitialized = onFeatureInitialized;
		final Prefs prefs = _environment.getPrefs();
		String epgVersion = prefs.getString(Param.System.EPG_VERSION);
		final String epgServer = prefs.getString(Param.System.EPG_SERVER);
		final String epgProvider = prefs.getString(Param.System.EPG_PROVIDER);
		Bundle bundle = new Bundle();
		bundle.putString("SERVER", epgServer);
		bundle.putString("VERSION", epgVersion);
		bundle.putString("PROVIDER", epgProvider);
		String channelsUrl = prefs.getString(Param.System.EPG_CHANNELS_URL, bundle);
		final RequestQueue queue = _environment.getRequestQueue();

		// Retrieve EPG channels
		Log.i(TAG, "Retrieving EPG channels from " + channelsUrl);
		JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, channelsUrl, null,
		        new Response.Listener<JSONObject>()
		        {
			        @Override
			        public void onResponse(JSONObject response)
			        {
			        	try
                        {
	                        _channelsMeta = response.getJSONArray("meta");
		        			for (int j=0; j<_channelsMeta.length(); j++)
		        			{
		        				String key = _channelsMeta.getString(j);
		        				if ("id".equals(key))
		        					_metaChannelId = j;
		        				else if ("title".equals(key))
		        					_metaChannelTitle = j;
		        				else if ("thumbnail".equals(key))
		        					_metaChannelThumbnail = j;
		        				else
		        					Log.w(TAG, "Unknown channel column `" + key + "'");
		        			}

				        	_channelsData = response.getJSONArray("data");

				        	// get all channel logos
				        	final int nChannels = getChannelCount();
				        	_channelLogos = new Bitmap[nChannels];
				        	for (int i = 0; i < getChannelCount(); i++)
				        	{
				        		final String channelId = getChannelId(i);
				        		String channelLogo = getChannelLogoName(i);
				        		Bundle bundle = new Bundle();
				        		bundle.putString("SERVER", epgServer);
				        		bundle.putString("PROVIDER", epgProvider);
				        		bundle.putString("CHANNEL", channelId);
				        		bundle.putString("LOGO", channelLogo);
				        		String channelLogoUrl = prefs.getString(Param.System.EPG_CHANNEL_LOGO_URL, bundle);

				        		Log.i(TAG, "Retrieving channel logo " + channelLogoUrl);
				        		ImageRequest imageRequest = new ImageRequest(channelLogoUrl,
				        				new LogoResponse(channelId, i),
				        				320, 240, // FIXME: Move to constants
				        				Config.ARGB_8888,
				        				new Response.ErrorListener()
										{
											@Override
	                                        public void onErrorResponse(VolleyError error)
	                                        {
				        						_retrievedLogos++;
				        						if (_retrievedLogos == nChannels)
				        						{
				        							Log.i(TAG, "Last channel logo retrieved with error: " + error.getMessage());
				        							_onFeatureInitialized.onInitialized(FeatureEPG.this, ResultCode.OK);
				        						}
				        						else
				        						{
				        							Log.i(TAG, "Retrieve channel logo " + channelId + " with error: " + error);
				        						}
	                                        }
										}
				        		);
				        		queue.add(imageRequest);
				        	}
                        }
                        catch (JSONException e)
                        {
    			        	Log.e(TAG, e.getMessage(), e);
    			        	_onFeatureInitialized.onInitialized(FeatureEPG.this, ResultCode.GENERAL_FAILURE);
                        }
			        }
		        },
		        new Response.ErrorListener()
		        {
			        @Override
			        public void onErrorResponse(VolleyError error)
			        {
			        	_onFeatureInitialized.onInitialized(FeatureEPG.this, error.networkResponse.statusCode);
			        }
		        }
		);
		queue.add(jsObjRequest);
	}

	private class LogoResponse implements Response.Listener<Bitmap>
	{
		private int _index;
		private String _channelId;

		LogoResponse(String channelId, int index)
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
    	return _channelsData.length();
    }

    /**
     * Return channel id for specified channel index
     *
     * @param index
     * @return String
     */
    public String getChannelId(int index)
    {
    	try
        {
        	JSONArray row = _channelsData.getJSONArray(index);
	        return row.getString(_metaChannelId);
        }
        catch (JSONException e)
        {
        	Log.e(TAG, e.getMessage(), e);
        }
    	return null;
    }

    /**
     * Return channel title for specified channel index
     *
     * @param index
     * @return String
     */
    public String getChannelTitle(int index)
    {
    	try
        {
        	JSONArray row = _channelsData.getJSONArray(index);
	        return row.getString(_metaChannelTitle);
        }
        catch (JSONException e)
        {
        	Log.e(TAG, e.getMessage(), e);
        }
    	return null;
    }

    /**
     * Return channel logo name for specified channel index
     *
     * @param index
     * @return String
     */
    public String getChannelLogoName(int index)
    {
    	try
        {
        	JSONArray row = _channelsData.getJSONArray(index);
	        return row.getString(_metaChannelThumbnail);
        }
        catch (JSONException e)
        {
        	Log.e(TAG, e.getMessage(), e);
        }
    	return null;
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
