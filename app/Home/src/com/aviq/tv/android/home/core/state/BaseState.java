/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    BaseState.java
 * Author:      alek
 * Date:        Jul 16, 2013
 * Description: Base class of all application visible states
 */

package com.aviq.tv.android.home.core.state;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;

import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.utils.Log;

/**
 * Base class of all application visible states.
 * A state may be set as background or overlay. A state is active if appears on
 * top of the States stack.
 */
public class BaseState extends Fragment
{
	private static final String TAG = BaseState.class.getSimpleName();

	/**
	 * Initialize State instance.
	 *
	 */
    public BaseState()
	{
		Log.i(TAG, getClass().getSimpleName() + " created");
	}

	/**
	 * Shows State on screen. The method can be overwritten in order to initialize visualization.
	 *
	 * @param params
	 *            The params set to this State when showing
	 * @throws StateException
	 */
    public void show(Bundle params) throws StateException
	{
    	Environment.getInstance().getStateManager().setStateMain(this, params);
	}

	/**
	 * Shows State on screen. The method can be overwritten in order to initialize visualization.
	 *
	 * @param params
	 *            The params set to this State when showing
	 * @throws StateException
	 */
    public void showOverlay(Bundle params) throws StateException
	{
    	Environment.getInstance().getStateManager().setStateOverlay(this, params);
	}

	/**
	 * Hides State from screen
	 */
    public void hide()
	{
    	Environment.getInstance().getStateManager().hideState(this);
	}

	/**
	 * Returns true if the current state is visible on screen
	 */
    public boolean isShown()
    {
	    return isAdded();
    }

	/**
	 * Method consuming key down event if the State is currently active
	 *
	 * @param keyCode
	 *            The value in event.getKeyCode().
	 * @param event
	 *            Description of the key event.
	 * @return Return true to prevent this event from being propagated further,
	 *         or false to indicate that you have not handled this event and it
	 *         should continue to be propagated.
	 * @throws StateException
	 */
    public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		return false;
	}

	/**
	 * Method consuming key up event if the State is currently active
	 *
	 * @param keyCode
	 *            The value in event.getKeyCode().
	 * @param event
	 *            Description of the key event.
	 * @return Return true to prevent this event from being propagated further,
	 *         or false to indicate that you have not handled this event and it
	 *         should continue to be propagated.
	 * @throws StateException
	 */
    public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		return false;
	}
}