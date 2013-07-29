/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    BasePlayer.java
 * Author:      alek
 * Date:        17 Jul 2013
 * Description: Abstract player class
 */

package com.aviq.tv.android.home.player;

/**
 * Defines abstract player
 *
 */
public class BasePlayer implements IPlayer
{

	/**
	 *
	 */
	public BasePlayer()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see com.aviq.tv.android.home.player.IPlayer#play(java.lang.String)
	 */
	@Override
	public void play(String url)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.aviq.tv.android.home.player.IPlayer#stop()
	 */
	@Override
	public void stop()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.aviq.tv.android.home.player.IPlayer#pause()
	 */
	@Override
	public void pause()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.aviq.tv.android.home.player.IPlayer#addEventListener(com.aviq.tv.android.home.player.IPlayer.EventListener)
	 */
	@Override
	public void addEventListener(EventListener eventListener)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.aviq.tv.android.home.player.IPlayer#removeEventListener(com.aviq.tv.android.home.player.IPlayer.EventListener)
	 */
	@Override
	public void removeEventListener(EventListener eventListener)
	{
		// TODO Auto-generated method stub

	}
}
