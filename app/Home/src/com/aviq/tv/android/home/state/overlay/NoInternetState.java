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

import android.os.Bundle;

import com.aviq.tv.android.home.Constants;
import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.state.StateManager;
import com.aviq.tv.android.home.utils.Log;

/**
 * Defines a "No Internet" overlay state
 */
public class NoInternetState extends OverlayState
{
	private static final String TAG = NoInternetState.class.getSimpleName();
	
	/**
	 * @param stateManager
	 */
	public NoInternetState(StateManager stateManager)
	{
		super(stateManager);
	}
	
	@Override
	public void show(Bundle params)
	{
		Log.i(TAG, ".show");

		if (params == null)
			params = new Bundle();

		params.putInt(Constants.PARAM_MESSAGE_RES_ID, R.string.connection_lost);
		params.putInt(Constants.PARAM_BACKGROUND_RES_ID, R.drawable.problem);

		super.show(params);
	}
	
	@Override
	public void hide()
	{
		Log.i(TAG, ".hide");
		super.hide();
	}
}
