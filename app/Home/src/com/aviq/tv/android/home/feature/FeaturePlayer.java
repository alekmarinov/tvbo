/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeaturePlayer.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: Component feature providing player
 */

package com.aviq.tv.android.home.feature;

import android.widget.VideoView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.FeatureComponent;
import com.aviq.tv.android.home.core.FeatureName;
import com.aviq.tv.android.home.core.FeatureName.Component;
import com.aviq.tv.android.home.player.AndroidPlayer;
import com.aviq.tv.android.home.player.IPlayer;

/**
 * Component feature providing player
 */
public class FeaturePlayer extends FeatureComponent
{
	public static final String TAG = FeaturePlayer.class.getSimpleName();
	protected AndroidPlayer _player;

	public FeaturePlayer(Environment environment)
	{
		super(environment);

		VideoView videoView = (VideoView) environment.getMainActivity().findViewById(R.id.player);
		_player = new AndroidPlayer(videoView);

		_dependencies.Components.add(FeatureName.Component.EPG);
	}

	@Override
	public Component getId()
	{
		return FeatureName.Component.PLAYER;
	}

	public IPlayer getPlayer()
	{
		return _player;
	}
}
