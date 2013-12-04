/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    TVState.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: Defines the fullscreen TV state
 */

package com.aviq.tv.android.home.state.tv;

import com.aviq.tv.android.home.state.BaseState;
import com.aviq.tv.android.home.state.StateEnum;
import com.aviq.tv.android.home.state.StateManager;

/**
 * Defines the fullscreen TV state
 *
 */
public class TVState extends BaseState
{
	private static final String TAG = TVState.class.getSimpleName();

	/**
	 * @param stateManager
	 */
	public TVState(StateManager stateManager)
	{
		super(stateManager, StateEnum.TV);
	}
}
