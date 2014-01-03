package com.aviq.tv.android.aviqtv.state.epg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

public class EpgHeaderView extends LinearLayout
{
	private static final String TAG = EpgHeaderView.class.getSimpleName();

	// FIXME: This could be made dynamic in prepareAdapterItems() or set in the element attributes
	public static final int TIME_SLOT_MINUTES = 30;

	/** A list of cached (re-usable) item views */
    private final LinkedList<View> _cachedItemViews = new LinkedList<View>();

    private Calendar _absoluteTimeMin = Calendar.getInstance();
    private Calendar _absoluteTimeMax = Calendar.getInstance();

	private Context _context;
	private Adapter _adapter;
	private int _position;

	public EpgHeaderView(Context context)
	{
		super(context);
		init(context, null /*context.obtainStyledAttributes(R.styleable.ZapperList3)*/);
	}

	public EpgHeaderView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, null /*context.obtainStyledAttributes(attrs, R.styleable.ZapperList3)*/);
	}

	private void init(Context context, TypedArray attr)
	{
		_context = context;

		if (attr != null)
			attr.recycle();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		//Log.v(TAG, ".onLayout: changed = " + changed + ", left = " + left + ", top = " + top + ", right = " + right
		//        + ", bottom = " + bottom);

	    super.onLayout(changed, left, top, right, bottom);

	    // If we don't have an adapter, we don't need to do anything
	    if (_adapter == null)
	        return;

	    removeNonVisibleViews(0);
	    invalidate();
	}

	private void removeNonVisibleViews(final int offset)
    {
        int childCount = getChildCount();

        // Check if we should remove any views from the left
        View firstChild = getChildAt(0);
        while (firstChild != null && firstChild.getRight() + offset < 0)
        {
            // Remove the left view
            removeViewInLayout(firstChild);
            childCount--;
            _cachedItemViews.addLast(firstChild);

            // Continue to check the next child only if we have more than
            // one child left
            if (childCount > 1)
            {
                firstChild = getChildAt(0);
            }
            else
            {
                firstChild = null;
            }
        }

        // Check if we should remove any views from the right
        View lastChild = getChildAt(childCount - 1);
        while (lastChild != null && lastChild.getLeft() + offset > getWidth())
        {
            // Remove the right view
            removeViewInLayout(lastChild);
            childCount--;
            _cachedItemViews.addLast(lastChild);

            // Continue to check the next child only if we have more than
            // one child left
            if (childCount > 1)
            {
                lastChild = getChildAt(childCount - 1);
            }
            else
            {
                lastChild = null;
            }
        }
    }

	private View getCachedView()
    {
        if (_cachedItemViews.size() != 0)
        {
            return _cachedItemViews.removeFirst();
        }
        return null;
    }

	public void setAdapter(Adapter adapter)
	{
		_adapter = adapter;
		removeAllViewsInLayout();
	    requestLayout();
	}

	public Adapter getAdapter()
	{
		return _adapter;
	}

	public int getMinutesForSinglePosition()
	{
		return TIME_SLOT_MINUTES;
	}

	public int getPixelsPerSinglePosition()
	{
		return (int) (getPixelsPerMinute() * getMinutesForSinglePosition() + 0.5);
	}

	public double getPixelsPerMinute()
	{
		TimeBasedRelativeLayout child = (TimeBasedRelativeLayout) getChildAt(0);
		if (child == null)
			return -1;

		return child.getPixelsPerOneMinute();
	}

	/** Set the minimum time bound */
	public void setAbsoluteTimeMin(Calendar timeMin)
	{
		_absoluteTimeMin = timeMin;
	}

	/** Set the maximum time bound */
	public void setAbsoluteTimeMax(Calendar timeMax)
	{
		_absoluteTimeMax = timeMax;
	}

	public int getPositionForTime(Calendar c)
	{
		int position = -1;

		if (c.before(_absoluteTimeMin))
			position = 0;
		else if (c.after(_absoluteTimeMax))
			position = _adapter.getCount() - 1;
		else
		{
			for (int i = 0; i < _adapter.getCount(); i++)
			{
				Calendar item = (Calendar) _adapter.getItem(i);

				boolean foundTime = c.get(Calendar.YEAR) == item.get(Calendar.YEAR)
						&& c.get(Calendar.MONTH) == item.get(Calendar.MONTH)
						&& c.get(Calendar.DAY_OF_MONTH) == item.get(Calendar.DAY_OF_MONTH)
						&& c.get(Calendar.HOUR_OF_DAY) == item.get(Calendar.HOUR_OF_DAY);

				if (foundTime)
				{
					position = c.get(Calendar.MINUTE) < 30 ? i : i + 1;
					break;
				}
			}
		}

		return position;
	}

	public Calendar getTimeForPosition(int position)
	{
		if (position < 0 || position > _adapter.getCount() - 1)
			return null;

		return (Calendar) _adapter.getItem(position);
	}

	public void setPosition(int position)
	{
		removeAllViewsInLayout();

		int num = _adapter.getCount();
		for (int i = position; i < num; i++)
		{
			View view = getCachedView();

			TimeBasedRelativeLayout child = view == null ? (TimeBasedRelativeLayout) _adapter.getView(i, null,
			        null) : (TimeBasedRelativeLayout) _adapter.getView(i, view, null);

			child.setTimeInMinutes(TIME_SLOT_MINUTES);
			addView(child);
		}

	    requestLayout();
	    invalidate();

	    _position = position;
	}

	public int getPosition()
	{
		return _position;
	}

	private List<Calendar> prepareAdapterItems()
	{
		long totalHours = (_absoluteTimeMax.getTimeInMillis() - _absoluteTimeMin.getTimeInMillis()) / (1000 * 60 * 60);

		List<Calendar> timeListItems = new ArrayList<Calendar>();

		Calendar timeTracker = Calendar.getInstance();
		timeTracker.setTimeInMillis(_absoluteTimeMin.getTimeInMillis());

		Calendar time;
		for (int i = 0; i < totalHours; i++)
		{
			int year = timeTracker.get(Calendar.YEAR);
			int month = timeTracker.get(Calendar.MONTH);
			int day = timeTracker.get(Calendar.DAY_OF_MONTH);
			int hour = timeTracker.get(Calendar.HOUR_OF_DAY);

			time = Calendar.getInstance();
			time.set(year, month, day, hour, 0, 0);
			timeListItems.add(time);

			time = Calendar.getInstance();
			time.set(year, month, day, hour, 30, 0);
			timeListItems.add(time);

			timeTracker.add(Calendar.HOUR_OF_DAY, 1);
		}

		return timeListItems;
	}

	public void useDefaultAdapter(Calendar c, int positionOffset)
	{
		List<Calendar> items = prepareAdapterItems();
		EpgHeaderAdapter adapter = new EpgHeaderAdapter(_context, items);
		setAdapter(adapter);

		int position = getPositionForTime(c);
		setPosition(position + positionOffset);
	}
}
