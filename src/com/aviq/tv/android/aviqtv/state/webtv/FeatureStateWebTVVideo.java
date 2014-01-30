/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureTV.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.aviqtv.state.webtv;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.AVKeyEvent;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.ResultCode;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.feature.player.FeaturePlayer;

/**
 * EPG state feature
 */
public class FeatureStateWebTVVideo extends FeatureState
{
	public static final String TAG = FeatureStateWebTVVideo.class.getSimpleName();

	public static final String ARGS_URL = "url";
	public static final String ARGS_CHANNEL_NAME = "channelName";
	public static final String ARGS_SUICIDE_TIMEOUT = "suicideTimeout";

	private FeaturePlayer _featurePlayer;
	private View _rootView;
	private Handler _handler = new Handler();
	private VideoView _videoView;
	private MediaController _mediaController;
	private Runnable _bufferingTimer;
	private String _channelName;
	private int _errorCounter = 0;
	private RelativeLayout.LayoutParams _videoViewLayoutParams;

	public FeatureStateWebTVVideo()
	{
		_dependencies.Components.add(FeatureName.Component.PLAYER);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");

		try
		{
			_featurePlayer = (FeaturePlayer) Environment.getInstance().getFeatureComponent(FeatureName.Component.PLAYER);

			onFeatureInitialized.onInitialized(this, ResultCode.OK);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
			onFeatureInitialized.onInitialized(this, ResultCode.GENERAL_FAILURE);
		}
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.WEBTV_VIDEO;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");

		_rootView = inflater.inflate(R.layout.state_webtv_video, container, false);
		_videoView = _featurePlayer.getVideoView();

		_videoView.setOnCompletionListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				Log.i(TAG, "******* Movie completed");
				stopPlayback();
				close();
			}
		});

		// Extract fragment parameters

		Bundle params = getArguments();
		String url = params.getString(ARGS_URL);
		_channelName = params.getString(ARGS_CHANNEL_NAME);
		Log.v(TAG, "Video URL = " + url + ", channel = " + _channelName);

		// test url
		//url = "http://www.nasa.gov/multimedia/nasatv/NTV-Public-IPS.m3u8";
		//url = "http://194.230.85.52/MOVIEPATH1/Planes_SD_54062_EF_WMA_fr.wmv";

		if (_featurePlayer.getPlayer().isPlaying())
			_featurePlayer.getPlayer().stop();

		_mediaController = _featurePlayer.createMediaController(false);
		_featurePlayer.play(url);

//		_videoView = _featurePlayer.getVideoView();
//
//		_bufferingTimer = new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				Log.d(TAG, "duration = " + _videoView.getDuration() + ", position = " + _videoView.getCurrentPosition());
//				if (_videoView.getDuration() < 0)
//				{
//					_errorCounter++;
//					if (_errorCounter == 3)
//					{
//						Log.i(TAG, "Duration -1 for " + _errorCounter + " times. Assume error playing video.");
//						stopPlayback();
//						destroy();
//					}
//					else
//					{
//						_handler.postDelayed(_bufferingTimer, 1000);
//					}
//				}
//				else
//				{
//					_errorCounter = 0;
//
//					if (_videoView.getCurrentPosition() <= 0)
//					{
//						_handler.postDelayed(_bufferingTimer, 100);
//					}
//					else
//					{
//						_handler.postDelayed(_bufferingTimer, 1000);
//					}
//				}
//			}
//		};
//		_handler.postDelayed(_bufferingTimer, 500);
//
//		_errorCounter = 0;

		return _rootView;
	}

	@Override
	public boolean onKeyDown(AVKeyEvent event)
	{
		Log.i(TAG, ".onKeyDown: key = " + event);

		switch (event.Code)
		{
			case PLAY_STOP:
			case BACK: // stop
				stopPlayback();
				close();
				return true;

			case UP:
			case DOWN:
			case LEFT:
			case RIGHT:
				return true;

			case OK:
				if (_mediaController.isShowing() || _mediaController.isShown())
					_mediaController.hide();
				else
					_mediaController.show();
				return true;

			default:
				break;
		}
		return super.onKeyDown(event);
	}



	@Override
	public void onShow(boolean isViewUncovered)
	{
		super.onShow(isViewUncovered);
		_videoView.setVisibility(View.VISIBLE);
		_videoView.setZOrderOnTop(true);
		_videoViewLayoutParams = (RelativeLayout.LayoutParams) _videoView.getLayoutParams();
		_featurePlayer.setVideoViewFullScreen();
	}

	@Override
	public void onHide(boolean isViewUncovered)
	{
		super.onHide(isViewUncovered);
		stopPlayback();
		_videoView.setZOrderOnTop(false);

		int x = _videoViewLayoutParams.leftMargin;
		int y = _videoViewLayoutParams.topMargin;
		int w = _videoViewLayoutParams.width;
		int h = _videoViewLayoutParams.height;
		_featurePlayer.setVideoViewPositionAndSize(x, y, w, h);
	}

	private void stopPlayback()
	{
		_handler.removeCallbacks(_bufferingTimer);
		_featurePlayer.removeMediaController();
		_featurePlayer.getPlayer().stop();
	}
}
