/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    BaseState.java
 * Author:      alek
 * Date:        Jul 16, 2013
 * Description: Base class of all application visible states
 */

package com.aviq.tv.android.home.state;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.aviq.tv.android.home.R;

/**
 * Base class of all application visible states.
 * A state may be set as background or overlay. A state is active if appears on
 * top of the States stack.
 */
public class BaseState
{
	protected StateManager _stateManager;
	protected FrameLayout _mainFragmentView;
	protected FrameLayout _overlayFragmentView;
	protected Fragment _fragment;
	protected boolean _overlay;
	private boolean _shown;
	private int _openingTransition;
	private int _closingTransition;

	/**
	 * Initialize State instance.
	 *
	 * @param stateManager
	 *            The owner of this State instance.
	 */
	public BaseState(StateManager stateManager)
	{
		_stateManager = stateManager;
		
		_mainFragmentView = (FrameLayout) _stateManager.getMainActivity().findViewById(R.id.fragment);
		_overlayFragmentView = (FrameLayout) _stateManager.getMainActivity().findViewById(R.id.overlay_fragment);
		_openingTransition = FragmentTransaction.TRANSIT_FRAGMENT_OPEN;
		_closingTransition = FragmentTransaction.TRANSIT_FRAGMENT_CLOSE;
	}
	
	/**
	 * Called when a State is about to appear on the screen. The method can be
	 * overwritten in order to initialize visualization.
	 *
	 * @param params
	 *            The params set to this State when showing
	 */
	public void show(Bundle params)
	{
		_shown = true;

		FrameLayout view = _overlay ? _overlayFragmentView : _mainFragmentView;
		view.setVisibility(View.VISIBLE);

		int fragmentId = _overlay ? R.id.overlay_fragment : R.id.fragment;
		
		FragmentTransaction ft = _stateManager.getMainActivity().getFragmentManager().beginTransaction();
		ft.replace(fragmentId, _fragment);
		ft.setTransition(_openingTransition);
		ft.commit();
	}

	/**
	 * Called when a State is about to disappear from the screen.
	 */
	public void hide()
	{
		_shown = false;
		
		FragmentTransaction ft = _stateManager.getMainActivity().getFragmentManager().beginTransaction();
		ft.remove(_fragment);
		ft.setTransition(_closingTransition);
		ft.commit();
		
		_fragment = null;
	
		FrameLayout view = _overlay ? _overlayFragmentView : _mainFragmentView;
		view.setVisibility(View.INVISIBLE);
	}

	/**
	 * Check if this State is visible on the screen
	 *
	 * @return true if this State is visible on the screen
	 */
	public boolean isShown()
	{
		return _shown;
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
	public boolean onKeyDown(int keyCode, KeyEvent event) throws StateException
	{
		if (keyCode == KeyEvent.KEYCODE_F2)
		{
			_stateManager.setState(StateEnum.MENU, null);
			return true;
		}

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
	public boolean onKeyUp(int keyCode, KeyEvent event) throws StateException
	{
		return false;
	}
	
	public void setOverlay(boolean overlay)
	{
		_overlay = overlay;
	}
	
	public boolean isOverlay()
	{
		return _overlay;
	}
	
	public void setFragment(Fragment fragment)
	{
		_fragment = fragment;
	}
	
	public Fragment setFragment()
	{
		return _fragment;
	}
	
	public void setOpeningTransition(int transition)
	{
		_openingTransition = transition;
	}
	
	public void setClosingTransition(int transition)
	{
		_closingTransition = transition;
	}
}
