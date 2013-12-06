/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeaturePlayer.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Component feature providing player
 */

package com.aviq.tv.android.home.feature.player;

import android.widget.VideoView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.feature.FeatureComponent;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureName.Component;
import com.aviq.tv.android.home.core.player.IPlayer;

/**
 * Component feature providing player
 */
public class FeaturePlayer extends FeatureComponent
{
	public static final String TAG = FeaturePlayer.class.getSimpleName();
	protected AndroidPlayer _player;

	public FeaturePlayer()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
	}

	@Override
	public void initialize(OnFeatureInitialized onFeatureInitialized)
	{
		VideoView videoView = (VideoView) Environment.getInstance().getActivity().findViewById(R.id.player);
		_player = new AndroidPlayer(videoView);
	}

	@Override
	public Component getComponentName()
	{
		return FeatureName.Component.PLAYER;
	}

	public IPlayer getPlayer()
	{
		return _player;
	}
}
