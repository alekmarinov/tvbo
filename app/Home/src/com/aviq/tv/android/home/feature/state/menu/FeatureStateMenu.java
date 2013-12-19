/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureStateMenu.java
 * Author:      alek
 * Date:        14 Oct 2013
 * Description: Menu state implementation
 */

package com.aviq.tv.android.home.feature.state.menu;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.core.state.BaseState;
import com.aviq.tv.android.home.core.state.IStateMenuItem;
import com.aviq.tv.android.home.core.state.StateException;
import com.aviq.tv.android.home.utils.Log;

/**
 * Menu state implementation
 */
public class FeatureStateMenu extends FeatureState
{
	private static final String TAG = FeatureStateMenu.class.getSimpleName();
	private List<IStateMenuItem> _menuItemStates = new ArrayList<IStateMenuItem>();
	private ViewGroup _rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		_rootView = (ViewGroup) inflater.inflate(R.layout.state_menu, container, false);
		for (final IStateMenuItem menuItemState : _menuItemStates)
		{
			ImageView imageView = new ImageButton(Environment.getInstance().getContext());
			imageView.setImageResource(menuItemState.getMenuItemResourceId());
			imageView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						Environment.getInstance().getStateManager().setStateMain((BaseState) menuItemState, null);
					}
					catch (StateException e)
					{
						Log.e(TAG, e.getMessage(), e);
					}
				}
			});
			_rootView.addView(imageView);
			Log.i(TAG, "Created menu item " + menuItemState.getMenuItemCaption());
		}
		_rootView.requestFocus();
		return _rootView;
	}

	public void addMenuItemState(IStateMenuItem menuItemState)
	{
		Log.i(TAG, "Added menu item " + menuItemState.getMenuItemCaption());
		_menuItemStates.add(menuItemState);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.MENU;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				// Hide overlay state
                Environment.getInstance().getStateManager().hideStateOverlay();
			break;
		}
		return false;
	}
}
