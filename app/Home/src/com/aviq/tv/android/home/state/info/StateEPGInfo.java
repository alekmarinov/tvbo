/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    StateEPGInfo.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: Defines EPG Info state
 */

package com.aviq.tv.android.home.state.info;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.state.BaseState;
import com.aviq.tv.android.home.state.StateEnum;

/**
 * Defines EPG Info state
 *
 */
public class StateEPGInfo extends BaseState
{

	/**
	 * @param Environment
	 */
	public StateEPGInfo(Environment environment)
	{
		super(environment, StateEnum.INFO);
	}
}
