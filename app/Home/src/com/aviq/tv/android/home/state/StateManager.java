/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    StateManager.java
 * Author:      alek
 * Date:        Jul 16, 2013
 * Description: Control visibility of one or more States on the screen
 */

package com.aviq.tv.android.home.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.aviq.tv.android.home.MainActivity;
import com.aviq.tv.android.home.state.epg.EPGState;
import com.aviq.tv.android.home.state.info.InfoState;
import com.aviq.tv.android.home.state.menu.MenuState;
import com.aviq.tv.android.home.state.settings.SettingsState;
import com.aviq.tv.android.home.state.tv.TVState;
import com.aviq.tv.android.home.state.watchlist.WatchlistState;

/**
 * Control visibility of one or two States on the screen.
 * The current states are represented as a stack of size limit 2.
 * The top state on the stack is the current active state.
 * The state at position 0 at the stack is called background State, the state at
 * position 1 is Overlay
 */
public class StateManager
{
	private static final String TAG = StateManager.class.getSimpleName();
	private final Map<StateEnum, BaseState> _states = new HashMap<StateEnum, BaseState>();
	private final Stack<BaseState> _activeStates = new Stack<BaseState>();
	private final MainActivity _mainActivity;

	/**
	 * Initialize StateManager instance.
	 *
	 * @param mainActivity
	 *            The owner MainActivity of this StateManager
	 */
	public StateManager(MainActivity mainActivity)
	{
		_mainActivity = mainActivity;

		_states.put(StateEnum.MENU, new MenuState(this));
		_states.put(StateEnum.TV, new TVState(this));
		_states.put(StateEnum.EPG, new EPGState(this));
		_states.put(StateEnum.INFO, new InfoState(this));
		_states.put(StateEnum.SETTINGS, new SettingsState(this));
		_states.put(StateEnum.WATCHLIST, new WatchlistState(this));
	}

	/**
	 * Sets new State as active. If isOverlay is true than the new State appears
	 * over the current State.
	 *
	 * @param state
	 *            The new State to activate
	 * @param params
	 *            Bundle holding params to be sent to the State when showing
	 * @param isOverlay
	 *            If this State overlays the current
	 */
	public void setState(StateEnum stateEnum, Bundle params, boolean isOverlay) throws StateException
	{
		BaseState newState = _states.get(stateEnum);

		if (newState == null)
		{
			throw new StateException(null, "Undefined state `" + stateEnum + "'");
		}
		else
		{
			switch (_activeStates.size())
			{
				case 0:
					if (isOverlay)
					{
						throw new StateException(null, "Can't set overlay state `" + stateEnum
						        + "' without background State");
					}
					else
					{
						_activeStates.add(newState);
						newState.show(params);
					}
				case 1:
					if (isOverlay)
					{
						_activeStates.add(newState);
						newState.show(params);
					}
					else
					{
						_activeStates.pop().hide();
						_activeStates.add(newState);
						newState.show(params);
					}
				break;
				case 2:
					if (isOverlay)
					{
						_activeStates.pop().hide();
						_activeStates.add(newState);
						newState.show(params);
					}
					else
					{
						_activeStates.pop().hide();
						_activeStates.pop().hide();
						_activeStates.add(newState);
						newState.show(params);
					}
				break;
			}
		}
	}

	/**
	 * Sets new State as active. If isOverlay is true than the new State appears
	 * over the current State.
	 *
	 * @param state
	 *            The new State to activate
	 * @param params
	 *            Bundle holding params to be sent to the State when showing
	 */
	public void setState(StateEnum stateEnum, Bundle params) throws StateException
	{
		setState(stateEnum, params, false);
	}

	/**
	 * Gets State instance by enum
	 *
	 * @param stateEnum
	 * @return State instance corresponding to the specified enum
	 */
	public BaseState getState(StateEnum stateEnum)
	{
		return _states.get(stateEnum);
	}

	/**
	 * Gets current active state instance
	 *
	 * @return current state instance
	 */
	public BaseState getCurrentState()
	{
		if (_activeStates.size() > 0)
			return _activeStates.get(0);
		return null;
	}

	/**
	 * Gets current active overlay instance
	 *
	 * @return current overlay instance
	 */
	public BaseState getCurrentOverlay()
	{
		if (_activeStates.size() > 1)
			return _activeStates.get(1);
		return null;
	}

	/**
	 * Delegates key down event to the current active state or overlay
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
	public boolean onKeyDown(int keyCode, KeyEvent event) throws StateException
	{
		Log.i(TAG, ".onKeyDown: keyCode = " + keyCode + ", state = " + getCurrentState() + ", overlay = "
		        + getCurrentOverlay());
		if (_activeStates.size() > 0)
			return _activeStates.get(_activeStates.size() - 1).onKeyDown(keyCode, event);

		return false;
	}

	/**
	 * Delegates key up event to the current active state or overlay
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
	public boolean onKeyUp(int keyCode, KeyEvent event) throws StateException
	{
		Log.i(TAG, ".onKeyUp: keyCode = " + keyCode + ", state = " + getCurrentState() + ", overlay = "
		        + getCurrentOverlay());
		if (_activeStates.size() > 0)
			return _activeStates.get(_activeStates.size() - 1).onKeyUp(keyCode, event);

		return false;
	}

	// TODO: Add onLongKeyDown() method
	// TODO: Add onLongKeyUp() method

	/**
	 * Gets owner MainActivity
	 *
	 * @return The owner MainActivity of this StateManager
	 * @throws StateException
	 */
	public MainActivity getMainActivity()
	{
		return _mainActivity;
	}
}
