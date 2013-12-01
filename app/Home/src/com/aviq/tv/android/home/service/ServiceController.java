/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    ServiceManager.java
 * Author:      alek
 * Date:        15 Oct 2013
 * Description: Controls services starting and result handling
 */

package com.aviq.tv.android.home.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;
import com.aviq.tv.android.home.MainApplication;

/**
 * Controls service starting and result handling
 */
public class ServiceController
{
	private static final String TAG = ServiceController.class.getSimpleName();
	private final MainApplication _mainApplication;
	private Handler _handler = new Handler();
	private RequestQueue _requestQueue;
	private ImageLoader _imageLoader;

	/**
	 * Interface to be implemented by service caller when the service execution
	 * finishes
	 */
	public interface OnResultReceived
	{
		void onReceiveResult(int resultCode, Bundle resultData);
	}

	/**
	 * Implements the asynchronous control of IntentService call
	 */
	public class Promise extends ResultReceiver
	{
		private OnResultReceived _then;
		private Intent _intentService;

		/**
		 * Creates the promise object
		 *
		 * @param intentService
		 *            the service this promise is responsible to control
		 * @param handler
		 *            the looper shared by the ResultReceiver and target
		 *            intentService
		 */
		public Promise(Intent intentService, Handler handler)
		{
			super(handler);
			_intentService = intentService;
		}

		/**
		 * Starts the intentService and sets a callback handling the result when
		 * the last finishes execution
		 *
		 * @param then
		 *            the callback handling the result returned by the
		 *            intentService
		 */
		public void then(OnResultReceived then)
		{
			_then = then;
			_mainApplication.startService(_intentService);
		}

		/**
		 * Starts the intentService periodically every intervalSecs seconds
		 * delayed with delaySecs seconds and sets a callback handling the
		 * result when the last finishes execution
		 *
		 * @param delaySecs
		 *            the number of seconds before starting the intentService
		 *            periodically
		 * @param intervalSecs
		 *            the time interval in seconds
		 * @param then
		 *            the callback handling the result returned by the
		 *            intentService
		 */
		public void every(int delaySecs, int intervalSecs, OnResultReceived then)
		{
			_then = then;
			PendingIntent pintent = PendingIntent.getService(_mainApplication, 0, _intentService,
			        PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarm = (AlarmManager) _mainApplication.getSystemService(Context.ALARM_SERVICE);
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delaySecs * 1000,
			        intervalSecs * 1000, pintent);
		}

		/**
		 * Starts the intentService periodically every intervalSecs seconds
		 * delayed with delaySecs seconds
		 *
		 * @param delaySecs
		 *            the number of seconds before starting the intentService
		 *            periodically
		 * @param intervalSecs
		 *            the time interval in seconds
		 */
		public void every(int delaySecs, int intervalSecs)
		{
			every(delaySecs, intervalSecs, null);
		}

		/**
		 * Starts the intentService periodically every intervalSecs seconds
		 * and sets a callback handling the result when the last finishes
		 * execution
		 *
		 * @param intervalSecs
		 *            the time interval in seconds
		 * @param then
		 *            the callback handling the result returned by the
		 *            intentService
		 */
		public void every(int intervalSecs, OnResultReceived then)
		{
			every(0, intervalSecs, then);
		}

		/**
		 * Starts the intentService periodically every intervalSecs seconds
		 *
		 * @param intervalSecs
		 *            the time interval in seconds
		 */
		public void every(int intervalSecs)
		{
			every(0, intervalSecs, null);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData)
		{
			if (_then != null)
			{
				_then.onReceiveResult(resultCode, resultData);
			}
		}
	}

	/**
	 * Initialize ServiceManager instance.
	 *
	 * @param mainApplication
	 *            The owner MainApplication of this ServiceManager
	 */
	public ServiceController(MainApplication mainApplication)
	{
		_mainApplication = mainApplication;
		_requestQueue = Volley.newRequestQueue(mainApplication);

		int memClass = ((ActivityManager) mainApplication.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		// Use 1/8th of the available memory for this memory cache.
		int cacheSize = 1024 * 1024 * memClass / 8;
		_imageLoader = new ImageLoader(_requestQueue, new BitmapLruCache(cacheSize));
	}

	/**
	 * Starts an intent service specified by its class
	 *
	 * @param serviceClass
	 *            the class of the IntentService
	 * @param params
	 *            Bundle
	 * @return Promise
	 */
	public Promise startService(Class<?> serviceClass, Bundle params)
	{
		Intent intent = new Intent(_mainApplication, serviceClass);
		Promise onServiceComplete = new Promise(intent, _handler);
		intent.putExtra(BaseService.EXTRA_RESULT_RECEIVER, onServiceComplete);
		if (params != null)
			intent.putExtras(params);
		return onServiceComplete;
	}

	/**
	 * Starts an intent service specified by its class
	 *
	 * @param serviceClass
	 *            the class of the IntentService
	 * @return Promise
	 */
	public Promise startService(Class<?> serviceClass)
	{
		return startService(serviceClass, null);
	}

	/**
	 * Least recently used cache
	 */
	private class BitmapLruCache extends LruCache<String, Bitmap> implements ImageCache
	{
		public BitmapLruCache(int maxSize)
		{
			super(maxSize);
		}

		@Override
		protected int sizeOf(String key, Bitmap value)
		{
			return value.getRowBytes() * value.getHeight();
		}

		@Override
		public Bitmap getBitmap(String url)
		{
			return get(url);
		}

		@Override
		public void putBitmap(String url, Bitmap bitmap)
		{
			put(url, bitmap);
		}
	}
}