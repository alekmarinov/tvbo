/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    FeatureStateMenu.java
 * Author:      alek
 * Date:        14 Oct 2013
 * Description: Menu state implementation
 */

package com.aviq.tv.android.aviqtv.state.menu;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.AVKeyEvent;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.Key;
import com.aviq.tv.android.sdk.core.Log;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.BaseState;
import com.aviq.tv.android.sdk.core.state.IStateMenuItem;
import com.aviq.tv.android.sdk.core.state.StateException;
import com.aviq.tv.android.sdk.feature.rcu.FeatureRCU;

/**
 * Menu state implementation
 */
public class FeatureStateMenu extends FeatureState
{
	private static final String TAG = FeatureStateMenu.class.getSimpleName();
	private List<IStateMenuItem> _menuItemStates = new ArrayList<IStateMenuItem>();
	private ViewGroup _menuContainer;

	public FeatureStateMenu() throws FeatureNotFoundException
	{
		require(FeatureName.Component.RCU);
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		_feature.Component.RCU.getEventMessenger().register(this, FeatureRCU.ON_KEY_PRESSED);
		super.initialize(onFeatureInitialized);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_menu, container, false);
		_menuContainer = (ViewGroup) viewGroup.findViewById(R.id.menu_container);

		for (final IStateMenuItem menuItemState : _menuItemStates)
		{
			ImageView imageView = new ImageButton(Environment.getInstance());
			imageView.setImageResource(menuItemState.getMenuItemResourceId());
			imageView.setId(menuItemState.getMenuItemResourceId());
			// FIXME: add background selector
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

			// create TextView with item caption
			TextView textView = new TextView(Environment.getInstance());
			textView.setText(menuItemState.getMenuItemCaption());
			textView.setTextAppearance(Environment.getInstance(), R.style.MenuItemCaption);

			// add item image and caption to a RelativeLayout container
			RelativeLayout buttonContainer = new RelativeLayout(Environment.getInstance());
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
			        LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			params.addRule(RelativeLayout.BELOW, imageView.getId());
			buttonContainer.addView(imageView);
			buttonContainer.addView(textView, params);

			_menuContainer.addView(buttonContainer);
			Log.i(TAG, "Created menu item " + menuItemState.getMenuItemCaption());
		}
		_menuContainer.requestFocus();
		return viewGroup;
	}

	@Override
	public void onEvent(int msgId, Bundle bundle)
	{
		Log.i(TAG, ".onEvent: msgId = " + msgId);
		if (FeatureRCU.ON_KEY_PRESSED == msgId)
		{
			// Don't reopen the MENU if already shown.
			if (isShown())
				return;

			Key key = Key.valueOf(bundle.getString(Environment.EXTRA_KEY));
			boolean isConsumed = bundle.getBoolean(Environment.EXTRA_KEYCONSUMED);
			if (Key.MENU.equals(key) && !isConsumed)
			{
				// show this Menu state
				try
				{
					Environment.getInstance().getStateManager().setStateOverlay(this, null);
				}
				catch (StateException e)
				{
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
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
	public boolean onKeyDown(AVKeyEvent event)
	{
		if (event.Event.getKeyCode() == KeyEvent.KEYCODE_F2)
			return true;
		switch (event.Code)
		{
			case BACK:
				// Hide overlay state
				Environment.getInstance().getStateManager().hideStateOverlay();
				return true;
		}
		return false;
	}
}
