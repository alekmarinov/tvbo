/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    StatusBar.java
 * Author:      alek
 * Date:        29 Dec 2013
 * Description: Control Status Bar view element states
 */

package com.aviq.tv.android.aviqtv.state;

import android.view.View;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.Log;

/**
 * Control Status Bar element states
 */
public class StatusBar
{
	public static final String TAG = StatusBar.class.getSimpleName();

	public enum Element
	{
		NAVIGATION,
		DETAILS,
		ADD,
		REMOVE,
		START_REORDER,
		STOP_REORDER,
		CHANNEL_ORDER_INDEX
	}

	private View _rootView;

	public StatusBar(View rootView)
	{
		_rootView = rootView;
		for (Element element: Element.values())
		{
			getElementView(element).setVisibility(View.GONE);
		}
	}

	public StatusBar enable(Element element)
	{
		View viewItem = getElementView(element);
		if (viewItem != null)
			viewItem.setVisibility(View.VISIBLE);
		else
			Log.w(TAG, "Can't find element " + element);
		return this;
	}

	private View getElementView(Element element)
	{
		switch (element)
		{
			case NAVIGATION:
				return _rootView.findViewById(R.id.navigation);
			case DETAILS:
				return _rootView.findViewById(R.id.details);
			case ADD:
				return _rootView.findViewById(R.id.add);
			case REMOVE:
				return _rootView.findViewById(R.id.remove);
			case START_REORDER:
				return _rootView.findViewById(R.id.start_reorder);
			case STOP_REORDER:
				return _rootView.findViewById(R.id.stop_reorder);
			case CHANNEL_ORDER_INDEX:
				return _rootView.findViewById(R.id.channel_order_index);
		}
		return null;
	}
}
