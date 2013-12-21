/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    StateManager.java
 * Author:      alek
 * Date:        Jul 16, 2013
 * Description: Control visibility of one or more States on the screen
 */

package com.aviq.tv.android.home.core.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.feature.state.MessageBox;
import com.aviq.tv.android.home.utils.Strings;

/**
 * Control visibility of one or two States on the screen and optional message
 * box state on the top. The current states are represented as a stack of size
 * limit 2 occupying layers MAIN and OVERLAY (StateLayer)
 */
public class StateManager
{
	private static final String TAG = StateManager.class.getSimpleName();
	private final Map<StateEnum, BaseState> _states = new HashMap<StateEnum, BaseState>();
	private final Stack<BaseState> _activeStates = new Stack<BaseState>();
	private final Activity _activity;
	private final Handler _handler = new Handler();
	private int _overlayBackgroundColor;
	private int _overlayBackgroundImage = 0;

	public enum StateLayer
	{
		MAIN, OVERLAY, MESSAGE
	}

	/**
	 * Initialize StateManager instance.
	 *
	 * @param mainActivity
	 *            The owner MainActivity of this StateManager
	 */
	public StateManager(Activity activity)
	{
		_activity = activity;
		Log.i(TAG, "StateManager created");
	}

	/**
	 * Sets new State as active. If isOverlay is true than the new State appears
	 * over the current main State.
	 *
	 * @param state
	 *            The new State to activate
	 * @param params
	 *            Bundle holding params to be sent to the State when showing
	 * @param isOverlay
	 *            If this State overlays the current
	 */
	private void setState(BaseState newState, Bundle params, boolean isOverlay) throws StateException
	{
		StringBuffer logMsg = new StringBuffer();
		String stateName;
		if (newState != null)
			stateName = newState.getClass().getSimpleName();
		else
			stateName = "null";

		logMsg.append(".setState: ").append(stateName).append('(');
		Strings.implodeBundle(logMsg, params, '=', ',').append("), overlay=").append(isOverlay);
		Log.i(TAG, logMsg.toString());

		switch (_activeStates.size())
		{
			case 0:
				if (isOverlay)
				{
					throw new StateException(null, "Can't set overlay state `" + stateName
					        + "' without background State");
				}
				else
				{
					if (newState != null)
					{
						_activeStates.add(newState);
						showState(newState, StateLayer.MAIN, params);
					}
				}
			break;
			case 1:
				if (isOverlay)
				{
					if (newState != null)
					{
						_activeStates.add(newState);
						showState(newState, StateLayer.OVERLAY, params);
					}
				}
				else
				{
					hideState(_activeStates.pop());
					if (newState != null)
					{
						_activeStates.add(newState);
						showState(newState, StateLayer.MAIN, params);
					}
				}
			break;
			case 2:
				if (isOverlay)
				{
					hideState(_activeStates.pop());
					if (newState != null)
					{
						_activeStates.add(newState);
						showState(newState, StateLayer.OVERLAY, params);
					}
				}
				else
				{
					hideState(_activeStates.pop());
					hideState(_activeStates.pop());
					if (newState != null)
					{
						_activeStates.add(newState);
						showState(newState, StateLayer.MAIN, params);
					}
				}
			break;
		}
	}

	/**
	 * Sets new main State as active.
	 *
	 * @param state
	 *            The new State to activate
	 * @param params
	 *            Bundle holding params to be sent to the State when showing
	 */
	public void setStateMain(BaseState state, Bundle params) throws StateException
	{
		setState(state, params, false);
	}

	/**
	 * Sets new State as active overlay.
	 *
	 * @param state
	 *            The new State to activate
	 * @param params
	 *            Bundle holding params to be sent to the State when showing
	 */
	public void setStateOverlay(BaseState state, Bundle params) throws StateException
	{
		setState(state, params, true);
	}

	/**
	 * Hides overlay state
	 */
	public void hideStateOverlay()
	{
		try
		{
			setState(null, null, true);
		}
		catch (StateException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	/**
	 * Displays state on screen at specified state layer (see StateLayer)
	 *
	 * @param state
	 *            to be shown
	 * @param stateLayer
	 *            the layer which this state will occupy
	 * @param params
	 *            Bundle with State params
	 */
	/* package */void showState(final BaseState state, final StateLayer stateLayer, final Bundle params)
	{
		StringBuffer logMsg = new StringBuffer();
		logMsg.append(".showState: ").append(state.getClass().getSimpleName()).append('(');
		Strings.implodeBundle(logMsg, params, '=', ',').append("), layer=").append(stateLayer.name());
		Log.i(TAG, logMsg.toString());

		// Workaround of setting fragment arguments when the fragment is already
		// added
		Runnable showFragmentChunk = new Runnable()
		{
			@Override
			public void run()
			{
				state.setArguments(params);
				FragmentTransaction ft = _activity.getFragmentManager().beginTransaction();
				int fragmentId = 0;
				switch (stateLayer)
				{
					case MAIN:
						fragmentId = R.id.main_fragment;
					break;
					case OVERLAY:
						fragmentId = R.id.overlay_fragment;
					break;
					case MESSAGE:
						fragmentId = R.id.message_fragment;
					break;
				}
				ft.add(fragmentId, state);
				// FIXME: make transition effect depending on state's StateLayer
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				ft.commit();

				_handler.post(new Runnable()
				{
					@Override
					public void run()
					{
						if (stateLayer.equals(StateLayer.OVERLAY))
						{
							if (_overlayBackgroundImage != 0)
								state.getView().setBackgroundResource(_overlayBackgroundImage);
							else
								state.getView().setBackgroundColor(_overlayBackgroundColor);
						}

						// notify state is shown
						state.onShow();
					}
				});
			}
		};
		if (state.isAdded())
		{
			hideState(state);
			_handler.post(showFragmentChunk);
		}
		else
		{
			showFragmentChunk.run();
		}
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
	 * Gets current active main state instance
	 *
	 * @return current state instance
	 */
	public BaseState getMainState()
	{
		if (_activeStates.size() > 0)
			return _activeStates.get(0);
		return null;
	}

	/**
	 * Gets current active overlay state instance
	 *
	 * @return current overlay instance
	 */
	public BaseState getOverlayState()
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
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.i(TAG, ".onKeyDown: keyCode = " + keyCode + ", state = " + getMainState() + ", overlay = "
		        + getOverlayState());
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
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		Log.i(TAG, ".onKeyUp: keyCode = " + keyCode + ", state = " + getMainState() + ", overlay = "
		        + getOverlayState());
		if (_activeStates.size() > 0)
			return _activeStates.get(_activeStates.size() - 1).onKeyUp(keyCode, event);

		return false;
	}

	// TODO: Add onLongKeyDown() method
	// TODO: Add onLongKeyUp() method

	/**
	 * Show message box
	 *
	 * @param msgType
	 *            determine the kind of message (see MessageBox.Type)
	 * @param stringId
	 *            string resource identifier for the message text
	 */
	public void showMessage(MessageBox.Type msgType, int stringId)
	{
		try
		{
			MessageBox messageBox = (MessageBox) Environment.getInstance().getFeatureState(
			        FeatureName.State.MESSAGE_BOX);
			final Bundle params = new Bundle();
			params.putString(MessageBox.PARAM_TYPE, msgType.name());
			params.putInt(MessageBox.PARAM_TEXT_ID, stringId);
			showState(messageBox, StateLayer.MESSAGE, params);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	/**
	 * Hides message box
	 */
	public void hideMessage()
	{
		try
		{
			MessageBox messageBox = (MessageBox) Environment.getInstance().getFeatureState(
			        FeatureName.State.MESSAGE_BOX);
			hideState(messageBox);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	/**
	 * Removes state fragment from screen
	 *
	 * @param state
	 *            to be removed from screen
	 */
	/* package */void hideState(BaseState state)
	{
		Log.i(TAG, ".hideState: " + state.getClass().getSimpleName());

		if (state.isAdded())
		{
			FragmentTransaction ft = _activity.getFragmentManager().beginTransaction();
			ft.remove(state);
			ft.commit();

			// notify state is hidden
			state.onHide();
		}
	}

	public void setOverlayBackgroundColor(int overlayBackgroundColor)
	{
		_overlayBackgroundColor = overlayBackgroundColor;
	}

	public void setOverlayBackgroundImage(int overlayBackgroundImage)
	{
		_overlayBackgroundImage = overlayBackgroundImage;
	}
}
