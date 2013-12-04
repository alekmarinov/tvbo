/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    WatchlistState.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description:
 */

package com.aviq.tv.android.home.state.watchlist;

import com.aviq.tv.android.home.state.BaseState;
import com.aviq.tv.android.home.state.StateEnum;
import com.aviq.tv.android.home.state.StateManager;

/**
 * @author alek
 *
 */
public class WatchlistState extends BaseState
{

	/**
	 * @param stateManager
	 */
	public WatchlistState(StateManager stateManager)
	{
		super(stateManager, StateEnum.WATCHLIST);
	}

}
