package com.aviq.tv.android.test.volley;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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
import com.android.volley.toolbox.Volley;

public class VolleyActivity extends Activity
{
	private static final String TAG = VolleyActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		testVolley();
	}

	private void testVolley()
	{
		final RequestQueue queue = Volley.newRequestQueue(this);
		String url = "http://epg.aviq.bg/v1/channels/rayv";
		final String urlLogo = "http://epg.aviq.bg/static/rayv";

		final long started = System.currentTimeMillis();

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
		        new Response.Listener<JSONObject>()
		        {
			        @Override
			        public void onResponse(JSONObject response)
			        {
			        	List<String> channelIds = new ArrayList<String>();
			    		long received = System.currentTimeMillis() - started;

			        	Log.i(TAG, "Response in " + received + " ms => " + response.toString());
			        	try
                        {
			        		JSONArray meta = response.getJSONArray("meta");
			        		JSONArray data = response.getJSONArray("data");

			        		for (int i=0; i<data.length(); i++)
			        		{
			        			JSONArray row = data.getJSONArray(i);
			        			StringBuffer buffer = new StringBuffer();
			        			buffer.append("{");
			        			for (int j=0; j<meta.length(); j++)
			        			{
			        				String key = meta.getString(j);
			        				String val = row.getString(j);
			        				buffer.append(key + ": " + val + ", ");
			        				if ("id".equals(key))
			        				{
			        					channelIds.add(val);
			        				}
			        			}
			        			buffer.append("},\n");
			        			Log.i(TAG, buffer.toString());
			        		}
                        }
                        catch (JSONException e)
                        {
    			        	Log.i(TAG, e.getMessage(), e);
                        }

			        	// Load channel icons
			        	for (String channelId: channelIds)
			        	{
			        		String channelLogoUrl = urlLogo + "/" + channelId + "/logo.png";
    						Log.i(TAG, "Loading bitmap " + channelLogoUrl);
			        		ImageRequest imageRequest = new ImageRequest(channelLogoUrl,
			        				new Response.Listener<Bitmap>()
			        				{
			        					@Override
                                        public void onResponse(Bitmap response)
			        					{
			        						Log.i(TAG, "Received bitmap " + response.getWidth() + "x" + response.getHeight());
			        					}
			        				},
			        				320, 240,
			        				Config.ARGB_8888,
			        				new Response.ErrorListener()
									{
										@Override
                                        public void onErrorResponse(VolleyError error)
                                        {
								        	Log.i(TAG, "VolleyError: " + error);
                                        }
									}
			        		);
			        		queue.add(imageRequest);
			        	}
			        }
		        }, new Response.ErrorListener()
		        {
			        @Override
			        public void onErrorResponse(VolleyError error)
			        {
			        	Log.i(TAG, "VolleyError: " + error);
			        }
		        });
		queue.add(jsObjRequest);
	}

}
