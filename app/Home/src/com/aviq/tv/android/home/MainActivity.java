/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    MainActivity.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: The main activity managing all application screens
 */

package com.aviq.tv.android.home;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.aviq.tv.android.home.player.AndroidPlayer;
import com.aviq.tv.android.home.service.InternetCheckService;
import com.aviq.tv.android.home.service.ServiceController;
import com.aviq.tv.android.home.state.MessageBox;
import com.aviq.tv.android.home.state.StateEnum;
import com.aviq.tv.android.home.state.StateException;
import com.aviq.tv.android.home.state.StateManager;
import com.aviq.tv.android.home.utils.Param;
import com.aviq.tv.android.home.utils.Params;
import com.aviq.tv.android.home.utils.Prefs;
import com.aviq.tv.android.home.utils.Strings;

/**
 * The main activity managing all application screens
 */
public class MainActivity extends Activity
{
	public static final String TAG = MainActivity.class.getSimpleName();

	private MainApplication _mainApplication;
	private StateManager _stateManager;
	private ServiceController _serviceController;
	private ViewGroup _rootLayout;
	private Handler _handler;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i(TAG, ".onCreate");

		setContentView(R.layout.activity_main);

		_rootLayout = (ViewGroup) findViewById(R.id.root_layout);

		_mainApplication = (MainApplication) getApplication();
		_stateManager = new StateManager(this);
		_serviceController = new ServiceController(this);

		Prefs prefs = _mainApplication.getPrefs();

		Bundle bundle = new Bundle();
		bundle.putString("USER", prefs.getString(Param.User.RAYV_USER));
		bundle.putString("PASS", prefs.getString(Param.User.RAYV_PASS));
		bundle.putString("STREAM_ID", "vtx_sf1");
		bundle.putInt("BITRATE", 1200);
		String url = prefs.getString(Param.System.RAYV_STREAM_URL_PATTERN, bundle);

		AndroidPlayer androidPlayer = new AndroidPlayer((VideoView) findViewById(R.id.player));
		androidPlayer.play(url);

		// initAlarms();
		_serviceController.startService(InternetCheckService.class).then(new ServiceController.OnResultReceived()
		{
			@Override
			public void onReceiveResult(int resultCode, Bundle resultData)
			{
				Log.i(TAG,
				        ".onReceiveResult: resultCode = " + resultCode + ", resultData= "
				                + Strings.implodeBundle(resultData));
			}
		});

		_serviceController.startService(InternetCheckService.class).every(10, new ServiceController.OnResultReceived()
		{
			@Override
			public void onReceiveResult(int resultCode, Bundle resultData)
			{
				Log.i(TAG,
				        ".onReceiveResult: resultCode = " + resultCode + ", resultData= "
				                + Strings.implodeBundle(resultData));
			}
		});

		testVolley();
	}

	private void testVolley()
	{
		RequestQueue queue = Volley.newRequestQueue(this);
		String url = "http://192.168.1.17:9090/v1/channels/rayv";

		JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
		        new Response.Listener<JSONObject>()
		        {
			        @Override
			        public void onResponse(JSONObject response)
			        {
			        	Log.i(TAG, "Response => " + response.toString());
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
			        				buffer.append(meta.getString(j) + ": " + row.getString(j) + ", ");
			        			}
			        			buffer.append("},\n");
			        			Log.i(TAG, buffer.toString());
			        		}
                        }
                        catch (JSONException e)
                        {
    			        	Log.i(TAG, e.getMessage(), e);
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

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.i(TAG, ".onDestroy");
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.i(TAG, ".onResume");

		// Set TV state as initial state
		try
		{
			_stateManager.setStateMain(StateEnum.TV, null);
		}
		catch (StateException e)
		{
			Log.e(TAG, "Error", e);
		}

		// Add a test overlay state
		_rootLayout.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				_stateManager.showMessage(MessageBox.Type.ERROR, R.string.connection_lost);
			}
		}, 3000);

		// Add a test overlay state
		_rootLayout.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				_stateManager.showMessage(MessageBox.Type.WARN, R.string.contentDescription);
			}
		}, 5000);

		// Hide the test overlay state
		_rootLayout.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				_stateManager.hideMessage();
			}
		}, 10000);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.i(TAG, ".onPause");
	}

	/**
	 * @return the main application owning this activity
	 */
	public MainApplication getApp()
	{
		return _mainApplication;
	}

	public void initAlarms()
	{
		Log.i(TAG, ".initAlarms");

		initInternetCheckAlarm();
	}

	/**
	 * Prepare a repeating alarm that starts InternetCheckService.
	 */
	private void initInternetCheckAlarm()
	{
		Log.i(TAG, ".initInternetCheckAlarm");

		int repeatingInterval = 1000 * Params.getInt(Param.System.INTERNET_CHECK_INTERVAL);

		Calendar cal = Calendar.getInstance();

		Intent intent = new Intent(this, InternetCheckService.class);
		intent.putExtra(Constants.EXTRA_RESULT_RECEIVER, new InternetCheckResultReceiver(_handler));

		PendingIntent pintent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), repeatingInterval, pintent);
	}

	private class InternetCheckResultReceiver extends ResultReceiver
	{
		public InternetCheckResultReceiver(Handler handler)
		{
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData)
		{
			Log.i(TAG, ".onReceiveResult: resultCode = " + resultCode);
		}
	};
}
