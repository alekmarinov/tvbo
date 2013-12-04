/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    StateMenu.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: Defines Main Menu state
 */

package com.aviq.tv.android.home.state.menu;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.state.BaseState;
import com.aviq.tv.android.home.state.StateEnum;

/**
 * Defines Main Menu state
 *
 */
public class StateMenu extends BaseState
{
	/**
	 * @param Environment
	 */
	public StateMenu(Environment environment)
	{
		super(environment, StateEnum.MENU);
	}

}
