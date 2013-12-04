/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    StateWatchlist.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: Defines Settings state
 */

package com.aviq.tv.android.home.state.watchlist;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.state.BaseState;
import com.aviq.tv.android.home.state.StateEnum;

/**
 * Defines Settings state
 *
 */
public class StateWatchlist extends BaseState
{

	/**
	 * @param Environment
	 */
	public StateWatchlist(Environment environment)
	{
		super(environment, StateEnum.WATCHLIST);
	}

}
