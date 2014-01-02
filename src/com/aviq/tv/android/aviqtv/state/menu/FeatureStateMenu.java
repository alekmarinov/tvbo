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
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.Log;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureState;
import com.aviq.tv.android.sdk.core.state.BaseState;
import com.aviq.tv.android.sdk.core.state.IStateMenuItem;
import com.aviq.tv.android.sdk.core.state.StateException;

/**
 * Menu state implementation
 */
public class FeatureStateMenu extends FeatureState
{
	private static final String TAG = FeatureStateMenu.class.getSimpleName();
	private List<IStateMenuItem> _menuItemStates = new ArrayList<IStateMenuItem>();
	private ViewGroup _menuContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.state_menu, container, false);
		_menuContainer = (ViewGroup) viewGroup.findViewById(R.id.menu_container);

		for (final IStateMenuItem menuItemState : _menuItemStates)
		{
			ImageView imageView = new ImageButton(Environment.getInstance().getContext());
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
			TextView textView = new TextView(Environment.getInstance().getContext());
			textView.setText(menuItemState.getMenuItemCaption());
			textView.setTextAppearance(Environment.getInstance().getContext(), R.style.MenuItemCaption);

			// add item image and caption to a RelativeLayout container
			RelativeLayout buttonContainer = new RelativeLayout(Environment.getInstance().getContext());
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
				return true;
			case KeyEvent.KEYCODE_F2:
				return true;
		}
		return false;
	}
}
