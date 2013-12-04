/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    StateEPG.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: Defines EPG Grid state
 */

package com.aviq.tv.android.home.state.epg;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.state.BaseState;
import com.aviq.tv.android.home.state.StateEnum;

/**
 * Defines EPG Grid state
 *
 */
public class StateEPG extends BaseState
{
	/**
	 * @param Environment
	 */
	public StateEPG(Environment environment)
	{
		super(environment, StateEnum.EPG);
	}
}
