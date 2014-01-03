/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    ThumbnailsView.java
 * Author:      alek
 * Date:        21 Dec 2013
 * Description: View with thumbnails grid
 */

package com.aviq.tv.android.aviqtv.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.aviq.tv.android.sdk.core.Log;

/**
 * View with thumbnails grid
 */
public class ThumbnailsView extends GridView
{
	public static final String TAG = ThumbnailsView.class.getSimpleName();
	private ThumbAdapter _thumbAdapter = new ThumbAdapter();
	private boolean _isAttached = false;
	private ThumbItemCreater _thumbItemCreater;

	public interface ThumbItemCreater
	{
		View createView(ThumbnailsView parent, int position, LayoutInflater inflator);

		void updateView(ThumbnailsView parent, int position, View view, Object object);
	}

	/**
	 * @param context
	 */
	public ThumbnailsView(Context context)
	{
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ThumbnailsView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public ThumbnailsView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public void setThumbItemCreater(ThumbItemCreater thumbItemCreater)
	{
		_thumbItemCreater = thumbItemCreater;
	}

	public void addThumbItem(Object item, int position)
	{
		if (position >= 0)
		{
			_thumbAdapter._thumbItems.add(position, item);
			Log.i(TAG, "Added " + item + " at index " + position);
		}
		else
		{
			_thumbAdapter._thumbItems.add(item);
			Log.i(TAG, "Added " + item + " at index " + (_thumbAdapter._thumbItems.size() - 1));
		}

		if (_isAttached)
		{
			_thumbAdapter.notifyDataSetChanged();
			setSelection(position);
		}
	}

	public void addThumbItem(Object item)
	{
		addThumbItem(item, -1);
	}

	public Object removeThumbAt(int position)
	{
		Object item = _thumbAdapter._thumbItems.remove(position);
		Log.i(TAG, "Removed " + item + " from index " + position);
		if (_isAttached)
			_thumbAdapter.notifyDataSetChanged();
		return item;
	}

	public void swapPositions(int position1, int position2, boolean isNotifyDataSetChanged)
	{
		Collections.swap(_thumbAdapter._thumbItems, position1, position2);
		if (isNotifyDataSetChanged)
			_thumbAdapter.notifyDataSetChanged();
	}

	public void notifyDataSetChanged()
	{
		_thumbAdapter.notifyDataSetChanged();
	}

	private class ThumbAdapter extends BaseAdapter
	{
		private LayoutInflater _inflator = (LayoutInflater) getContext().getSystemService(
		        Context.LAYOUT_INFLATER_SERVICE);
		private List<Object> _thumbItems = new ArrayList<Object>();

		@Override
		public int getCount()
		{
			return _thumbItems.size();
		}

		@Override
		public Object getItem(int position)
		{
			return _thumbItems.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return _thumbItems.get(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
				convertView = _thumbItemCreater.createView(ThumbnailsView.this, position, _inflator);
			Object item = _thumbItems.get(position);
			convertView.setTag(item);
			Log.i(TAG, ".getView: " + item + " at index " + position);
			_thumbItemCreater.updateView(ThumbnailsView.this, position, convertView, item);
			return convertView;
		}
	}
	
	@Override
	protected void onAttachedToWindow()
	{
		if (getAdapter() == null)
		{
			setAdapter(_thumbAdapter);
			_isAttached = true;
		}
	}
}
