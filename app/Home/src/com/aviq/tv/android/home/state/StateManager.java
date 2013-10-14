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

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.aviq.tv.android.home.MainActivity;
import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.state.epg.EPGState;
import com.aviq.tv.android.home.state.info.InfoState;
import com.aviq.tv.android.home.state.menu.MenuState;
import com.aviq.tv.android.home.state.settings.SettingsState;
import com.aviq.tv.android.home.state.tv.TVState;
import com.aviq.tv.android.home.state.watchlist.WatchlistState;
import com.aviq.tv.android.home.utils.Strings;

/**
 * Control visibility of one or two States on the screen.
 * The current states are represented as a stack of size limit 2.
 * The top state on the stack is the current active state.
 * The state at position 0 at the stack is called main State, the state at
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

		registerState(new MenuState(this));
		registerState(new TVState(this));
		registerState(new EPGState(this));
		registerState(new InfoState(this));
		registerState(new SettingsState(this));
		registerState(new WatchlistState(this));
		registerState(new MessageBox(this));

		Log.i(TAG, "StateManager initialized");
	}

	/**
	 * Register state to manager
	 * @param state
	 */
	private void registerState(BaseState state)
	{
		_states.put(state.getStateEnum(), state);
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
	private void setState(StateEnum stateEnum, Bundle params, boolean isOverlay) throws StateException
	{
		StringBuffer logMsg = new StringBuffer();
		logMsg.append(".setState: ").append(stateEnum.toString()).append('(');
		Strings.implodeBundle(logMsg, params, '=', ',').append("), overlay=").append(isOverlay);
		Log.i(TAG, logMsg.toString());

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
						showState(newState, params);
					}
				case 1:
					if (isOverlay)
					{
						_activeStates.add(newState);
						showState(newState, params);
					}
					else
					{
						hideState(_activeStates.pop());
						_activeStates.add(newState);
						showState(newState, params);
					}
				break;
				case 2:
					if (isOverlay)
					{
						hideState(_activeStates.pop());
						_activeStates.add(newState);
						showState(newState, params);
					}
					else
					{
						hideState(_activeStates.pop());
						hideState(_activeStates.pop());
						_activeStates.add(newState);
						showState(newState, params);
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
	 * Sets new State as active overlay.
	 *
	 * @param state
	 *            The new State to activate
	 * @param params
	 *            Bundle holding params to be sent to the State when showing
	 */
	public void setStateOverlay(StateEnum stateEnum, Bundle params) throws StateException
	{
		setState(stateEnum, params, true);
	}

	/**
	 * Displays state on screen
	 *
	 * @param state to be shown
	 * @param params Bundle with State params
	 */
	/* package */ void showState(BaseState state, Bundle params)
    {
		boolean isOverlay = _activeStates.size() > 1;
		int fragmentId;

    	StringBuffer logMsg = new StringBuffer();
		logMsg.append(".showState: ").append(state.getClass().getSimpleName()).append('(');
		Strings.implodeBundle(logMsg, params, '=', ',').append("), overlay=").append(isOverlay);
		Log.i(TAG, logMsg.toString());

		if (isOverlay)
		{
			fragmentId = R.id.overlay_fragment;
			getMainActivity().findViewById(R.id.overlay_fragment).setVisibility(View.VISIBLE);
		}
		else
		{
			fragmentId = R.id.main_fragment;
			getMainActivity().findViewById(R.id.main_fragment).setVisibility(View.VISIBLE);
		}

		state.setArguments(params);

		FragmentTransaction ft = getMainActivity().getFragmentManager().beginTransaction();
		ft.replace(fragmentId, state);
		// ft.setTransition(_openingTransition);
		ft.commit();
    }

	/**
	 * Hides state
	 *
	 * @param state to be hidden
	 */
	/* package */ void hideState(BaseState state)
    {
		boolean isOverlay = _activeStates.size() > 0;
		if (isOverlay)
		{
			getMainActivity().findViewById(R.id.overlay_fragment).setVisibility(View.VISIBLE);
		}
		else
		{
			getMainActivity().findViewById(R.id.main_fragment).setVisibility(View.VISIBLE);
		}
		Log.i(TAG, ".hideState: " + state.getClass().getSimpleName() + ", overlay=" + isOverlay);

		FragmentTransaction ft = getMainActivity().getFragmentManager().beginTransaction();
		ft.remove(state);
		// ft.setTransition(_closingTransition);
		ft.commit();
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

	public void showMessage(MessageBox.Type msgType, int stringId)
    {
		getMainActivity().findViewById(R.id.messagebox_fragment).setVisibility(View.VISIBLE);

		MessageBox messageBox = (MessageBox)getState(StateEnum.MESSAGEBOX);
		Bundle params = new Bundle();
		params.putString(MessageBox.PARAM_TYPE, msgType.name());
		params.putInt(MessageBox.PARAM_TEXT_ID, stringId);
		messageBox.setArguments(params);

		FragmentTransaction ft = getMainActivity().getFragmentManager().beginTransaction();
		ft.replace(R.id.messagebox_fragment, messageBox);
		// ft.setTransition(_openingTransition);
		ft.commit();
    }

	public void hideMessage()
    {
		FragmentTransaction ft = getMainActivity().getFragmentManager().beginTransaction();
		ft.remove(getState(StateEnum.MESSAGEBOX));
		// ft.setTransition(_closingTransition);
		ft.commit();
	}
}
