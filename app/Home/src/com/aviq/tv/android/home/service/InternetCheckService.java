package com.aviq.tv.android.home.service;

import java.net.HttpURLConnection;
import java.net.URL;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.aviq.tv.android.home.Constants;
import com.aviq.tv.android.home.utils.Log;

public class InternetCheckService extends IntentService
{
	public static final String TAG = InternetCheckService.class.getSimpleName();
	
	public static final int INTERNET_NOK = 0;
	public static final int INTERNET_OK = 1;
	
	// FIXME: move out of here
	private static final String[] URLS = new String[]
	{ "http://www.google.com", "http://www.yahoo.com", "http://www.bing.com", "http://www.apple.com" };
	
	private Messenger _messenger;
	private int _numCompleted = 0;
	private int _numSuccessful = 0;
	
	public InternetCheckService()
	{
		super(TAG);
	}
	
	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.i(TAG, ".onHandleIntent");
		
		if (intent == null)
			return;
		
		_messenger = (Messenger) intent.getExtra(Constants.EXTRA_RESULT_RECEIVER);
		
		// If there is no one to notify about the Internet connectivity, then
		// there is no point in running this service.
		if (_messenger == null)
			return;
		
		// Check for network connectivity 
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())
		{
			_numSuccessful = 0;
			notifyInterestedParty();
			return;
		}
		
		// Check URLs for connectivity
		for (int i = 0; i < URLS.length; i++)
		{
			CheckUrlRunnable runnable = new CheckUrlRunnable(URLS[i], new MyOnThreadCompletedListener());
			new Thread(runnable, "InternetCheckThread$" + i).start();
		}
	}
	
	private void notifyInterestedParty()
	{
		if (_numSuccessful > 0)
		{
			Log.v(TAG, ".notifyInterestedParty: success (" + _numSuccessful + ")");
			try 
			{
				Message msg = Message.obtain();
				msg.what = INTERNET_OK;
	            _messenger.send(msg);
	        } 
			catch (RemoteException e) 
			{
	            Log.i(TAG, "Error", e);
	        }
		}
		else
		{
			Log.v(TAG, ".notifyInterestedParty: failure");
			try 
			{
				Message msg = Message.obtain();
				msg.what = INTERNET_NOK;
	            _messenger.send(msg);
	        } 
			catch (RemoteException e) 
			{
	            Log.i(TAG, "Error", e);
	        }
		}
		
		_numCompleted = 0;
		_numSuccessful = 0;
	}
	
	private static interface OnThreadCompletedListener
	{
		public void onSuccess();
		
		public void onFailure();
	}
	
	private class MyOnThreadCompletedListener implements OnThreadCompletedListener
	{
		@Override
		public void onSuccess()
		{
			_numCompleted++;
			_numSuccessful++;

			if (_numCompleted == URLS.length) {
				notifyInterestedParty();
			}
		}
		
		@Override
		public void onFailure()
		{
			_numCompleted++;
			
			if (_numCompleted == URLS.length) {
				notifyInterestedParty();
			}
		}
	};
	
	private class CheckUrlRunnable implements Runnable
	{
		private String url;
		private OnThreadCompletedListener listener;
		
		public CheckUrlRunnable(String url, OnThreadCompletedListener listener)
		{
			this.url = url;
			this.listener = listener;
		}
		
		@Override
		public void run()
		{
			try
			{
				URL remoteUrl = new URL(url);
				
				HttpURLConnection conn = (HttpURLConnection) remoteUrl.openConnection();
				conn.setRequestMethod("HEAD");
				conn.setRequestProperty("Accept-Encoding", "");
				conn.setRequestProperty("User-Agent", "Android Application");
				conn.setRequestProperty("Connection", "close");
				conn.setConnectTimeout(2000); // TODO move out of here
				conn.setReadTimeout(2000); // TODO move out of here
				conn.setInstanceFollowRedirects(false);
				conn.connect();

				// We don't care about "conn.getResponseCode() == 200". So long as the
				// server returns any code, the Internet connection exists.
				
				if (listener != null)
					listener.onSuccess();
				
				conn.disconnect();
			}
			catch (Exception e)
			{
				//e.printStackTrace();
				
				Log.w(TAG, "Failed Internet check for [" + url +"].", e);
				
				if (listener != null)
					listener.onFailure();
			}
		}
	}
}
