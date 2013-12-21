/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     Home
 * Filename:    FeatureStateWatchlist.java
 * Author:      alek
 * Date:        1 Dec 2013
 * Description: TV state feature
 */

package com.aviq.tv.android.home.feature.state.watchlist;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.ResultCode;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.core.feature.FeatureState;
import com.aviq.tv.android.home.core.state.IStateMenuItem;
import com.aviq.tv.android.home.feature.FeatureWatchlist;
import com.aviq.tv.android.home.feature.epg.Program;
import com.aviq.tv.android.home.feature.state.menu.FeatureStateMenu;

/**
 * Watchlist state feature
 */
public class FeatureStateWatchlist extends FeatureState implements IStateMenuItem
{
	public static final String TAG = FeatureStateWatchlist.class.getSimpleName();

	private ViewGroup _viewGroup;
	private FeatureWatchlist _watchlist;

	public FeatureStateWatchlist()
	{
		_dependencies.Components.add(FeatureName.Component.EPG);
		_dependencies.Components.add(FeatureName.Component.WATCHLIST);
		_dependencies.States.add(FeatureName.State.MENU);
	}

	@Override
	public FeatureName.State getStateName()
	{
		return FeatureName.State.WATCHLIST;
	}

	@Override
	public void initialize(final OnFeatureInitialized onFeatureInitialized)
	{
		Log.i(TAG, ".initialize");
		try
		{
			// insert in Menu
			FeatureStateMenu featureStateMenu = (FeatureStateMenu) Environment.getInstance().getFeatureState(
			        FeatureName.State.MENU);
			featureStateMenu.addMenuItemState(this);

			_watchlist = (FeatureWatchlist) Environment.getInstance().getFeatureComponent(FeatureName.Component.WATCHLIST);

			onFeatureInitialized.onInitialized(this, ResultCode.OK);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
			onFeatureInitialized.onInitialized(this, ResultCode.GENERAL_FAILURE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.i(TAG, ".onCreateView");
		_viewGroup = (ViewGroup) inflater.inflate(R.layout.state_watchlist, container, false);

		GridView gridView = (GridView) _viewGroup.findViewById(R.id.watchlist_grid);
		GridViewAdapter<Program> adapter = new GridViewAdapter<Program>(Environment.getInstance().getContext(),
		        _watchlist.getWatchedPrograms(), R.layout.item_watchlist);
		gridView.setAdapter(adapter);
		gridView.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id)
			{
				Log.d(TAG, "onItemSelected " + position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
				// TODO Auto-generated method stub
			}
		});
		gridView.requestFocus();

		return _viewGroup;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_ENTER:
				return true;
		}
		return false;
	}

	@Override
	public int getMenuItemResourceId()
	{
		return R.drawable.ic_menu_watchlist;
	}

	@Override
	public String getMenuItemCaption()
	{
		return getStateName().name();
	}
}
