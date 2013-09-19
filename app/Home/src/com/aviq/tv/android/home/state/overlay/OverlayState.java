/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    TVState.java
 * Author:      alek
 * Date:        16 Jul 2013
 * Description: Defines the fullscreen TV state
 */

package com.aviq.tv.android.home.state.overlay;

import android.app.FragmentTransaction;
import android.os.Bundle;

import com.aviq.tv.android.home.Constants;
import com.aviq.tv.android.home.state.BaseState;
import com.aviq.tv.android.home.state.StateManager;
import com.aviq.tv.android.home.utils.Log;

/**
 * Defines a generic overlay message state
 */
public class OverlayState extends BaseState
{
	private static final String TAG = OverlayState.class.getSimpleName();
	
	/**
	 * @param stateManager
	 */
	public OverlayState(StateManager stateManager)
	{
		super(stateManager);
	}
	
	@Override
	public void show(Bundle params)
	{
		Log.i(TAG, ".show");
		
		if (params == null)
			throw new IllegalArgumentException("Invalid arguments for " + TAG);
		
		int msgResId = params.getInt(Constants.PARAM_MESSAGE_RES_ID);
		int bkgdResId = params.getInt(Constants.PARAM_BACKGROUND_RES_ID);
		
		if (msgResId < 1 || bkgdResId < 1)
			throw new IllegalArgumentException("Invalid arguments for " + TAG);
		
		OverlayFragment overlayFragment = OverlayFragment.newInstance(msgResId, bkgdResId);
		
		setFragment(overlayFragment);
		setOverlay(true);
		setOpeningTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		setClosingTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		super.show(params);
	}
	
	@Override
	public void hide()
	{
		Log.i(TAG, ".hide");
		super.hide();
	}
}
