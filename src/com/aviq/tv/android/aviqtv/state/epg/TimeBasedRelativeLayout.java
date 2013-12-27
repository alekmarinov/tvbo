package com.aviq.tv.android.aviqtv.state.epg;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.aviq.tv.android.aviqtv.R;

public class TimeBasedRelativeLayout extends RelativeLayout
{
	private static final String TAG = TimeBasedRelativeLayout.class.getSimpleName();

	private Context _context;
	private float _hours;
	private int _pixelsPerOneHour;
	private int _desiredWidth = 0;
	private int _desiredHeight = 0;

	public TimeBasedRelativeLayout(Context context)
	{
		super(context);
		init(context, null, context.obtainStyledAttributes(R.styleable.TimeBasedRelativeLayout));
	}

	public TimeBasedRelativeLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs, context.obtainStyledAttributes(attrs, R.styleable.TimeBasedRelativeLayout));
	}

	public TimeBasedRelativeLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, attrs, context.obtainStyledAttributes(attrs, R.styleable.TimeBasedRelativeLayout, defStyle, 0));
	}

	private void init(Context context, AttributeSet attrs, TypedArray attr)
	{
		_context = context;
		setPixelsPerOneHour((int) attr.getDimension(R.styleable.TimeBasedRelativeLayout_pixelsPerOneHour, 0));
		_desiredHeight = (int) attr.getDimension(R.styleable.TimeBasedRelativeLayout_viewHeight, 0);
		attr.recycle();

		// This compiles on PC, but Jenkins complains
		//TypedArray attrAndroid = context.obtainStyledAttributes(attrs, android.R.styleable.ViewGroup_Layout);
		//_desiredHeight = (int) attrAndroid.getDimension(android.R.styleable.ViewGroup_Layout_layout_height, 0);
		//attrAndroid.recycle();
	}

	public void setTimeInMinutes(int minutes)
	{
		_hours = minutes / 60.0f;
		//Log.v(TAG, "_hours = " + _hours);

		calcDimensions();
	}

	public void setTimeInSeconds(long seconds)
	{
		setTimeInMinutes((int) (seconds / 60));
	}

	public void setTimeInMillis(long millis)
	{
		setTimeInMinutes((int) (millis / 60000));
	}

	public int getTimeInMunutes()
	{
		return (int) (_hours * 60);
	}

	public void setPixelsPerOneHour(int pixelsPerOneHour)
	{
		_pixelsPerOneHour = pixelsPerOneHour;
		//Log.v(TAG, "_pixelsPerOneHour = " + _pixelsPerOneHour);

		calcDimensions();
	}

	public int getPixelsPerOneHour()
	{
		return _pixelsPerOneHour;
	}

	public double getPixelsPerOneMinute()
	{
		return _pixelsPerOneHour / 60.0;
	}

	public void setDesiredWidth(int width)
	{
		_desiredWidth = width;
	}

	public int getDesiredWidth()
	{
		return _desiredWidth;
	}

	public int getDesiredHeight()
	{
		return _desiredHeight;
	}

	private void calcDimensions()
	{
		_desiredWidth = (int) (_hours * _pixelsPerOneHour);

		//Log.v(TAG, "_desiredWidth = " + _desiredWidth + ", _desiredHeight = " + _desiredHeight);

		// AbsListView vs ViewGroup vs com.sileria.android.view.HorzListView
		setLayoutParams(new AbsListView.LayoutParams(_desiredWidth, _desiredHeight));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		//final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		//final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		//final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int desiredWidth = _desiredWidth > 0 ? _desiredWidth : widthSize;
		int desiredHeight = _desiredHeight;

		//Log.v(TAG, ".onMeasure: desiredWidth = " + desiredWidth + ", desiredHeight = " + desiredHeight);

		widthMeasureSpec = MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.AT_MOST);
	    heightMeasureSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.AT_MOST);

	    getLayoutParams().width = desiredWidth;
	    getLayoutParams().height = desiredHeight;

	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
