/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    Grid.java
 * Author:      alek
 * Date:        21 Dec 2013
 * Description: Grid view
 */

package com.aviq.tv.android.aviqtv.state;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;

/**
 * Grid view
 */
public class ThumbnailsView extends GridView
{
	public static final String TAG = ThumbnailsView.class.getSimpleName();
	private GridAdapter _gridAdapter = new GridAdapter();
	private int _gridItemResourceLayout;

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

	public void setGridItemResourceLayout(int gridItemResourceLayout)
	{
		_gridItemResourceLayout = gridItemResourceLayout;
	}

	public void addGridItem(Bitmap bitmap, String text)
    {
		_gridAdapter.addGridItem(bitmap, text);
    }

	private class GridAdapter extends BaseAdapter
	{
		private List<GridItem> _gridItems = new ArrayList<GridItem>();

		@Override
        public int getCount()
        {
	        return _gridItems.size();
        }

		@Override
        public Object getItem(int position)
        {
	        return _gridItems.get(position);
        }

		@Override
        public long getItemId(int position)
        {
	        return _gridItems.get(position).hashCode();
        }

		@Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
			ViewHolder holder;
			if (convertView == null)
			{
				LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflator.inflate(_gridItemResourceLayout, null);

				holder = new ViewHolder();
				holder.textView = (TextView) convertView.findViewById(R.id.title);
				holder.imageView = (ImageView) convertView.findViewById(R.id.thumbnail);
				convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder) convertView.getTag();
			}

			GridItem item = _gridItems.get(position);
			holder.textView.setText(item.title);
			holder.imageView.setImageBitmap(item.thumbnail);

			return convertView;
        }

		private void addGridItem(Bitmap bitmap, String text)
	    {
			_gridItems.add(new GridItem(bitmap, text));
	    }
	}

	@Override
    protected void onAttachedToWindow()
	{
		if (getAdapter() == null)
			setAdapter(_gridAdapter);
		else
			_gridAdapter.notifyDataSetChanged();
	}

	private class ViewHolder
	{
		TextView textView;
		ImageView imageView;
	}

	private class GridItem
	{
		Bitmap thumbnail;
		String title;

		GridItem(Bitmap thumbnail, String title)
		{
			this.thumbnail = thumbnail;
			this.title = title;
		}
	}
}
