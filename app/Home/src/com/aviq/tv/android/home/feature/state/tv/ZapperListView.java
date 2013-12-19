/**
 * Copyright (c) 2007-2013, AVIQ Bulgaria Ltd
 *
 * Project:     AVIQTV
 * Filename:    ZapperListView.java
 * Author:      alek
 * Date:        18 Nov 2013
 * Description: Channel bar widget
 */

package com.aviq.tv.android.home.feature.state.tv;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.OverScroller;
import android.widget.ScrollView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.utils.Log;

/**
 * Channel bar widget
 */
public class ZapperListView extends ScrollView
{
	private static final String TAG = ZapperListView.class.getSimpleName();
	private ScrollItem _scrollItem;
	private OverScroller _myScroller;
	private int _position = 0;
	private int _activeItemIndex;
	private int _bmpMaxHeight;
	private int _topMargin;
	private int _visibleItemsCount;
	private int _hpadding;
	private int _vpadding;
	private float _fontSize;

	/**
	 * View defining the content area to be scrolled by the encapsulating
	 * ScrollView
	 */
	private class ScrollItem extends View
	{
		private Paint _backPaint = new Paint();
		private Paint _textPaint = new Paint();
		private Paint _paint = new Paint();
		private List<Bitmap> _bitmaps = new ArrayList<Bitmap>();
		private int _textWidth;
		private int _textHeight;
		private boolean _hasNumbers = true;
		private int _fontColor = Color.parseColor("#EEEEEE");
		private int _backColor = Color.parseColor("#01000000");

		public ScrollItem(Context context)
		{
			super(context);

			_backPaint.setStyle(Paint.Style.FILL);
			_backPaint.setColor(_backColor);

			if (_hasNumbers)
			{
				_textPaint.setColor(_fontColor);
				_textPaint.setTextAlign(Align.LEFT);
				_textPaint.setTextSize(_fontSize);
				_textPaint.setTypeface(Typeface.MONOSPACE);
				Rect bounds = new Rect();
				_textPaint.getTextBounds("999. ", 0, 2, bounds);
				_textWidth = 2 * bounds.width();
				_textHeight = bounds.height();
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			int virtualItemsCount = _bitmaps.size() + _activeItemIndex + _visibleItemsCount - _activeItemIndex;
			int h = _topMargin + virtualItemsCount * _bmpMaxHeight;
			h += 2 * virtualItemsCount * _vpadding;

			int w =  MeasureSpec.getSize(widthMeasureSpec);
			setMeasuredDimension(w, h);
			Log.d(TAG, ".onMeasure: widthMeasureSpec = " + widthMeasureSpec + ", heightMeasureSpec = "
			        + heightMeasureSpec + ", w = " + w + ", h = " + h);
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			Log.d(TAG, ".onDraw: w = " + getWidth() + ", h = " + getHeight());
			int itemHeight = getItemHeight();
			int y = _topMargin + _vpadding + _activeItemIndex * itemHeight;
			int yText = (_textHeight + itemHeight) >> 1;
			canvas.drawRect(0, 0, getWidth(), getHeight(), _backPaint);
			int num = 1;
			int bmpOffset = _hpadding;
			if (_hasNumbers)
				bmpOffset = _hpadding + _textWidth;
			for (Bitmap bmp : _bitmaps)
			{
				if (_hasNumbers)
					canvas.drawText(String.format("%3d", num), _hpadding, y + yText, _textPaint);
				if (bmp != null)
				{
					int bmpY = y + _vpadding + (_bmpMaxHeight - bmp.getHeight()) / 2;
					Log.d(TAG, "draw bmp " + bmp.getWidth() + "x" + bmp.getHeight() + " at x=" + bmpOffset + ", y = " + bmpY);
					canvas.drawBitmap(bmp, bmpOffset, bmpY, _paint);
				}
				y += itemHeight;
				//canvas.drawLine(0, y, getWidth(), y, _textPaint);
				num++;
			}
		};

		public int getItemHeight()
		{
			return _bmpMaxHeight + 2 * _vpadding;
		}

		public void addBitmap(Bitmap bmp)
		{
			// if (_bmpWidth == 0)
			// {
			// _bmpWidth = bmp.getWidth();
			// _bmpHeight = bmp.getHeight();
			// }
			_bitmaps.add(bmp);
		}

		public int getCount()
		{
			return _bitmaps.size();
		}
	}

	public ZapperListView(Context context)
	{
		super(context);
		init(context, context.obtainStyledAttributes(R.styleable.ZapperListView));
	}

	public ZapperListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, context.obtainStyledAttributes(attrs, R.styleable.ZapperListView));
	}

	public ZapperListView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, context.obtainStyledAttributes(attrs, R.styleable.ZapperListView, defStyle, 0));
	}

	private void init(Context ctx, TypedArray attr)
	{
		_activeItemIndex = attr.getInteger(R.styleable.ZapperListView_selectedPosition, 0);
		_visibleItemsCount = attr.getInteger(R.styleable.ZapperListView_visibleItems, 10);
		_bmpMaxHeight = (int) attr.getDimension(R.styleable.ZapperListView_itemsHeight, 50);
		_topMargin = (int) attr.getDimension(R.styleable.ZapperListView_topMargin, 0);
		_hpadding = (int) attr.getDimension(R.styleable.ZapperListView_itemHorizontalPadding, 10);
		_vpadding = (int) attr.getDimension(R.styleable.ZapperListView_itemVerticalPadding, 10);
		_fontSize = attr.getDimension(R.styleable.ZapperListView_itemTextSize, 20f);

		attr.recycle();

		_scrollItem = new ScrollItem(ctx);
		addView(_scrollItem);

		// Get a reference to the scroller object through reflection since it's
		// private
		try
		{
			Class<?> parent = this.getClass();
			int attempts = 0;
			do
			{
				parent = parent.getSuperclass();
				attempts++;
			} while (!parent.getName().equals("android.widget.ScrollView") && attempts < 20);

			Field field = parent.getDeclaredField("mScroller");
			field.setAccessible(true);
			_myScroller = (OverScroller) field.get(this);
			_myScroller.setFriction(0.025f);
			Log.i(TAG, "Smooth scroller available for use");
		}
		catch (Exception e)
		{
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public void addDrawable(int resId)
	{
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), resId);
		addBitmap(bmp);
	}

	public void addBitmap(Bitmap bitmap)
	{
		_scrollItem.addBitmap(bitmap);
	}

	public void selectIndex(int index)
	{
		int prevPosition = _position;
		if (index < 0)
			index = 0;
		else if (index > getCount() - 1)
		{
			index = getCount() - 1;
		}
		_position = index;
		if (_position != prevPosition)
		{
			int startY = (int) getY() + prevPosition * _scrollItem.getItemHeight();
			int deltaY = (_position - prevPosition) * _scrollItem.getItemHeight();
			if (Math.abs(_position - prevPosition) == 1 && _myScroller != null)
			{
				_myScroller.startScroll((int) getX(), startY, 0, deltaY, 1000);
				invalidate();
			}
			else
			{
				smoothScrollBy(0, deltaY);
			}
			Log.d(TAG, ".selectIndex: " + prevPosition + " -> " + _position + ", startY = " + startY + ", deltaY = "
			        + deltaY);
		}
	}

	public int getSelectIndex()
	{
		return _position;
	}

	public int getCount()
	{
		return _scrollItem.getCount();
	}

	public void scrollUp()
	{
		if (_position > 0)
			selectIndex(_position - 1);
		else
			selectIndex(getCount() - 1);
	}

	public void scrollDown()
	{
		if (_position < getCount() - 1)
			selectIndex(_position + 1);
		else
			selectIndex(0);
	}
}
