package com.aviq.tv.android.aviqtv.state.epg;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.feature.epg.Program;

public class EpgRowView extends AdapterView<EpgRowAdapter>
{
	private static final String TAG = EpgRowView.class.getSimpleName();

	/** Represents an invalid child index */
	private static final int INVALID_INDEX = -1;

	/**
	 * Children added with this layout mode will be added to the right of the
	 * last child
	 */
	private static final int LAYOUT_MODE_RIGHT = 0;

	/**
	 * Children added with this layout mode will be added to the left of the
	 * first child
	 */
	private static final int LAYOUT_MODE_LEFT = 1;

	/** The current left of the first item */
	private int _listLeft;

	/**
	 * The offset from the left of the currently first visible item to the left
	 * of the first item
	 */
	private int _listLeftOffset;

	/** The adapter position of the first visible item */
	private int _firstItemPosition = 0;

	/** The adapter position of the last visible item */
	private int _lastItemPosition = -1;

	/** A list of cached (re-usable) item views */
	private final LinkedList<View> _cachedItemViews = new LinkedList<View>();

	/** Reusable rect */
	private Rect _rect;

	/** Selected item position */
	private int _position = -1;

	/** Previously selected item position */
	private int _prevPosition = -1;

	/** Selected child view */
	private View _selectedChild;

	/** Start time in milliseconds for the EPG row first item */
	private long _gridStartTimeMillis;

	/**
	 * Internal flag used when calling methods which trigger listeners. When set
	 * to true, listeners won't be called. Always reset to false in onLayout.
	 */
	private boolean _quiet = true;

	/**
	 * The background of the very first selected item after onLayout is complete
	 */
	private int _selectedItemBackgroundResourceIdOnLayout;

	/** Background for selected items */
	private int _selectedItemBackgroundResourceId;

	private final Map<View, Drawable> _viewsBackgrounds = new HashMap<View, Drawable>();

	private Context _context;
	private EpgRowAdapter _adapter;
	private OnItemSelectingListener _onItemSelectingListener;
	private OnItemSelectedListener _onItemSelectedListener;
	private OnItemUnsetSelectionListener _onItemUnsetSelectionListener;

	public EpgRowView(Context context)
	{
		super(context);
		init(context, context.obtainStyledAttributes(R.styleable.EpgListView));
	}

	public EpgRowView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, context.obtainStyledAttributes(attrs, R.styleable.EpgListView));
	}

	public EpgRowView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, context.obtainStyledAttributes(attrs, R.styleable.EpgListView, defStyle, 0));
	}

	/**
	 * Initialize EpgGridView object
	 *
	 * @param context
	 *            is the current Context
	 * @param attr
	 *            is a container with EpgGridView attributes
	 *            R.styleable.EpgGridView_*
	 */
	private void init(Context context, TypedArray attr)
	{
		_context = context;
		attr.recycle();
	}

	@Override
	public EpgRowAdapter getAdapter()
	{
		return _adapter;
	}

	@Override
	public View getSelectedView()
	{
		return _selectedChild != null ? _selectedChild : getViewAt(_position);
	}

	@Override
	public void setAdapter(EpgRowAdapter adapter)
	{
		_adapter = adapter;
		removeAllViewsInLayout();
		requestLayout();
	}

	@Override
	public void setSelection(final int position)
	{
		Log.v(TAG, ".setSelection: position = " + position);

		if (position < 0 || position > _adapter.getCount() - 1)
		{
			Log.w(TAG, ".setSelection: invalid position ignored; current position set at " + _position);
			return;
		}

		// Do pre-selection stuff
		_selectedChild = getSelectedView();
		notifyOnItemSelecting(position);

		// Undo selection stuff for old position
		unsetSelection(_position);

		// Re-set the position
		_prevPosition = _position;
		_position = position;

		// Do selection stuff
		_selectedChild = getSelectedView();
		if (_selectedChild != null)
		{
			// Change the background to mark the selection
			_selectedChild.setSelected(true);
			_selectedChild.setBackgroundResource(_selectedItemBackgroundResourceId);
		}
		notifyOnItemSelected();
	}

	@Override
	public int getSelectedItemPosition()
	{
		return _position;
	}

	public int getPrevItemPosition()
	{
		return _prevPosition;
	}

	public void unsetSelection(int position)
	{
		Log.v(TAG, ".unsetSelection: position = " + position);

		if (_selectedChild != null)
		{
			_selectedChild.setSelected(false);

			// Restore the original background before the selection
			Drawable background = _viewsBackgrounds.get(_selectedChild);
			_selectedChild.setBackgroundDrawable(background);
		}

		notifyOnItemUnselected(position);

		_selectedChild = null;
	}

	public void setOnItemSelectingListener(OnItemSelectingListener listener)
	{
		_onItemSelectingListener = listener;
	}

	@Override
	public void setOnItemSelectedListener(OnItemSelectedListener listener)
	{
		super.setOnItemSelectedListener(listener);
		_onItemSelectedListener = listener;
	}

	public void setOnItemUnsetSelectionListener(OnItemUnsetSelectionListener listener)
	{
		_onItemUnsetSelectionListener = listener;
	}

	/**
	 * This method sets the background of the initially selected view during the
	 * layout phase. It is useful when you want to have some sort of
	 * pre-selection of a specific item based on some criteria.
	 *
	 * @param bkgd
	 */
	public void setSelectedItemBackgroundResourceIdOnLayout(int backgroundResourceId)
	{
		_selectedItemBackgroundResourceIdOnLayout = backgroundResourceId;
	}

	/**
	 * This method sets the background of the selected item when the
	 * setSelected() method is used.
	 *
	 * @param bkgd
	 */
	public void setSelectedItemBackgroundResourceId(int backgroundResourceId)
	{
		_selectedItemBackgroundResourceId = backgroundResourceId;
	}

	private void notifyOnItemSelecting(int newPosition)
	{
		if (_onItemSelectingListener == null)
			return;

		if (_quiet)
			return;

		// post(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// _onItemSelectingListener.onItemSelecting(EpgRowView.this,
		// _selectedChild, _position, newPosition);
		// }
		// });

		_onItemSelectingListener.onItemSelecting(EpgRowView.this, _selectedChild, _position, newPosition);
	}

	private void notifyOnItemSelected()
	{
		if (_onItemSelectedListener == null)
			return;

		if (_quiet)
			return;

		// post(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// _onItemSelectedListener.onItemSelected(EpgRowView.this,
		// _selectedChild, _position, _adapter.getItemId(_position));
		// }
		// });

		_onItemSelectedListener.onItemSelected(EpgRowView.this, _selectedChild, _position,
		        _adapter.getItemId(_position));
	}

	private void notifyOnItemUnselected(int position)
	{
		if (_onItemUnsetSelectionListener == null)
			return;

		if (_quiet)
			return;

		// post(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// _onItemUnsetSelectionListener.onItemUnsetSelection(EpgRowView.this,
		// _selectedChild, position);
		// }
		// });

		_onItemUnsetSelectionListener.onItemUnsetSelection(EpgRowView.this, _selectedChild, position);
	}

	private void initPositionOnFirstLayout()
	{
		if (_position < 0)
		{
			// Initial initialization of _position
			Calendar now = Calendar.getInstance();
			long nowMillis = now.getTimeInMillis();
			for (int i = _firstItemPosition; i <= _lastItemPosition; i++)
			{
				Program p = (Program) _adapter.getItem(i);
				if (p.getStartTimeCalendar().getTimeInMillis() < nowMillis && p.getStopTimeCalendar().getTimeInMillis() > nowMillis)
				{
					_position = i;
					break;
				}
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		/*
		 * First a call to super and a null check are performed, and then we
		 * continue with the actual code. If we haven’t added any children yet,
		 * we start by doing that. The while statement loops through the adapter
		 * until we’ve added enough views to cover the screen. When we get a
		 * view from the adapter, we start by adding it as a child and then we
		 * need to measure it in order for the view to get it's correct size.
		 * After we’ve added all the views, we position them in the correct
		 * place.
		 */

		Log.v(TAG, ".onLayout: changed = " + changed + ", left = " + left + ", top = " + top + ", right = " + right
		        + ", bottom = " + bottom + ", _position = " + _position);

		super.onLayout(changed, left, top, right, bottom);

		// If we don't have an adapter, we don't need to do anything
		if (_adapter == null)
			return;

		if (!changed)
			return;

		// Find the element in the adapter whose end time is past the grid start
		// time
		int numItems = _adapter.getCount();
		for (int i = 0; i < numItems; i++)
		{
			Program p = (Program) _adapter.getItem(i);
			if (p.getStopTimeCalendar().getTimeInMillis() > _gridStartTimeMillis)
			{
				_firstItemPosition = i;
				_lastItemPosition = i - 1;
				break;
			}
		}

		if (getChildCount() == 0)
		{
			fillListRight(_listLeft, 0);
		}
		else
		{
			final int offset = _listLeft + _listLeftOffset - getChildAt(0).getLeft();
			removeNonVisibleViews(offset);
			fillList(offset);
		}

		positionItems();
		invalidate();

		// Do initial UI stuff. If any other layout stuff is necessary, do if
		// before the following code.

		// Clear any objects here; this is populated on every onLayout() call
		_viewsBackgrounds.clear();

		// setSelection(_position);
		initPositionOnFirstLayout();
		_selectedChild = getSelectedView();

		_quiet = false;

		if (_selectedChild != null && _selectedItemBackgroundResourceIdOnLayout > 0)
		{
			_selectedChild.setBackgroundResource(_selectedItemBackgroundResourceIdOnLayout);
		}

		// Keep references to the original backgrounds of the views

		for (int i = 0; i < getChildCount(); i++)
		{
			View child = getChildAt(i);
			_viewsBackgrounds.put(child, child.getBackground());
		}
	}

	/**
	 * Fills the list with child-views
	 *
	 * @param offset
	 *            Offset of the visible area
	 */
	private void fillList(final int offset)
	{
		// Log.v(TAG, ".fillList: offset = " + offset);

		final int rightEdge = getChildAt(getChildCount() - 1).getRight();
		fillListRight(rightEdge, offset);

		final int leftEdge = getChildAt(0).getLeft();
		fillListLeft(leftEdge, offset);
	}

	/**
	 * Starts at the right, and adds children until we've passed the list right
	 *
	 * @param rightEdge
	 *            The right edge of the currently last child
	 * @param offset
	 *            Offset of the visible area
	 */
	private void fillListRight(int rightEdge, final int offset)
	{
		// Log.v(TAG, ".fillListRight: rightEdge = " + rightEdge + ", offset = "
		// + offset + ", _lastItemPosition = "
		// + _lastItemPosition);

		int numItems = _adapter.getCount();
		while (rightEdge + offset < getWidth() && _lastItemPosition < numItems - 1)
		{
			_lastItemPosition++;
			final View newRightChild = _adapter.getView(_lastItemPosition, getCachedView(), this);
			addAndMeasureChild(newRightChild, LAYOUT_MODE_RIGHT);
			rightEdge += newRightChild.getMeasuredWidth();
		}
	}

	/**
	 * Starts at the left, and adds children until we've passed the list left
	 *
	 * @param leftEdge
	 *            The left edge of the currently first child
	 * @param offset
	 *            Offset of the visible area
	 */
	private void fillListLeft(int leftEdge, final int offset)
	{
		// Log.v(TAG, ".fillListLeft: leftEdge = " + leftEdge + ", offset = " +
		// offset + ", _firstItemPosition = "
		// + _firstItemPosition);

		while (leftEdge + offset > 0 && _firstItemPosition > 0)
		{
			_firstItemPosition--;

			final View newLeftCild = _adapter.getView(_firstItemPosition, getCachedView(), this);
			addAndMeasureChild(newLeftCild, LAYOUT_MODE_LEFT);
			final int childWidth = newLeftCild.getMeasuredWidth();
			leftEdge -= childWidth;

			// Update the list offset (since we added a view at the left)
			_listLeftOffset -= childWidth;
		}
	}

	/**
	 * Adds a view as a child view and takes care of measuring it
	 *
	 * @param child
	 *            The view to add
	 * @param layoutMode
	 *            Either LAYOUT_MODE_LEFT or LAYOUT_MODE_RIGHT
	 */
	private void addAndMeasureChild(View child, final int layoutMode)
	{
		// child.setBackgroundDrawable(_itemBackground);

		LayoutParams params = child.getLayoutParams();
		if (params == null)
		{
			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}

		final int index = layoutMode == LAYOUT_MODE_LEFT ? 0 : -1;
		addViewInLayout(child, index, params, true);

		child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST),
		        MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	}

	/**
	 * Positions the children at the "correct" positions
	 */
	private void positionItems()
	{
		// Log.v(TAG, ".positionItems");

		int left = 0;

		// Find what should be the "left" value of the first child displayed.
		// This is needed in order to know how much to offset the first item
		// to the left when it started before the grid's starting time.
		// So, we find the time difference from the start in minutes,
		// and convert it to pixels.
		TimeBasedRelativeLayout firstChild = (TimeBasedRelativeLayout) getChildAt(0);
		if (firstChild != null)
		{
			Program firstProgram = (Program) _adapter.getItem(_firstItemPosition);

			int firstChildWidth = firstChild.getMeasuredWidth();
			int thisWidth = getWidth();

			if (firstChildWidth > thisWidth)
			{
				// This is needed so that programs that are too long to fit the
				// visible area of the grid are resized to the grid's width.
				// Thus, the program title remains visible to the user at all
				// times even during horizontal pagination.

				firstChild.setDesiredWidth(thisWidth);
				firstChild.measure(MeasureSpec.AT_MOST, MeasureSpec.EXACTLY);
			}
			else
			{
				long deltaMillis = _gridStartTimeMillis - firstProgram.getStartTimeCalendar().getTimeInMillis();
				double roundingOffset = deltaMillis > 0 ? 0.5 : -0.5;
				long deltaMin = (long) (deltaMillis / 60000.0 + roundingOffset);
				left = -(int) (deltaMin * firstChild.getPixelsPerOneMinute());
			}
		}

		int top = 0;

		int childCount = getChildCount();
		for (int index = 0; index < childCount; index++)
		{
			View child = getChildAt(index);

			int width = child.getMeasuredWidth();
			int height = child.getMeasuredHeight();

			child.layout(left, top, left + width, top + height);
			left += width;
		}
	}

	/**
	 * Checks if there is a cached view that can be used
	 *
	 * @return A cached view or, if none was found, null
	 */
	private View getCachedView()
	{
		if (_cachedItemViews.size() != 0)
		{
			return _cachedItemViews.removeFirst();
		}
		return null;
	}

	/**
	 * Removes view that are outside of the visible part of the list. Will not
	 * remove all views.
	 *
	 * @param offset
	 *            Offset of the visible area
	 */
	private void removeNonVisibleViews(final int offset)
	{
		// Log.v(TAG, ".removeNonVisibleViews: offset = " + offset);

		// We need to keep close track of the child count in this function. We
		// should never remove all the views, because if we do, we loose track
		// of were we are.
		int childCount = getChildCount();

		// If we are not at the right of the list and have more than one child
		if (_lastItemPosition != _adapter.getCount() - 1 && childCount > 1)
		{
			// Check if we should remove any views from the left
			View firstChild = getChildAt(0);
			while (firstChild != null && firstChild.getRight() + offset < 0)
			{
				// Remove the left view
				removeViewInLayout(firstChild);
				childCount--;
				_cachedItemViews.addLast(firstChild);
				_firstItemPosition++;

				// Update the list offset (since we've removed the left child)
				_listLeftOffset += firstChild.getMeasuredWidth();

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
		}

		// If we are not at the left most part of the list and have more than
		// one child
		if (_firstItemPosition != 0 && childCount > 1)
		{
			// Check if we should remove any views from the right
			View lastChild = getChildAt(childCount - 1);
			while (lastChild != null && lastChild.getLeft() + offset > getWidth())
			{
				// Remove the right view
				removeViewInLayout(lastChild);
				childCount--;
				_cachedItemViews.addLast(lastChild);
				_lastItemPosition--;

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
	}

	/**
	 * Returns the index of the child that contains the coordinates given.
	 *
	 * @param x
	 *            X-coordinate
	 * @param y
	 *            Y-coordinate
	 * @return The index of the child that contains the coordinates. If no child
	 *         is found then it returns INVALID_INDEX
	 */
	private int getContainingChildIndex(final int x, final int y)
	{
		if (_rect == null)
			_rect = new Rect();

		for (int index = 0; index < getChildCount(); index++)
		{
			getChildAt(index).getHitRect(_rect);
			if (_rect.contains(x, y))
				return index;
		}
		return INVALID_INDEX;
	}

	/**
	 * Calls the item click listener for the child with at the specified
	 * coordinates
	 *
	 * @param x
	 *            The x-coordinate
	 * @param y
	 *            The y-coordinate
	 */
	private void clickChildAt(final int x, final int y)
	{
		final int index = getContainingChildIndex(x, y);
		if (index != INVALID_INDEX)
		{
			final View itemView = getChildAt(index);
			final int position = _firstItemPosition + index;
			final long id = _adapter.getItemId(position);
			performItemClick(itemView, position, id);
		}
	}

	/**
	 * Return View object only of the requested view is visible on the window.
	 * Otherwise, return null.
	 *
	 * @param positionInAdapter
	 * @return View or null
	 */
	public View getViewAt(int positionInAdapter)
	{
		Log.v(TAG, ".getViewAt: positionInAdapter = " + positionInAdapter + ", firstItemPosition = "
		        + _firstItemPosition);

		if (_adapter.getCount() > 0 && positionInAdapter >= 0)
		{
			return getChildAt(positionInAdapter - _firstItemPosition);
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.v(TAG, ".onKeyDown: keyCode = " + keyCode);

		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
		{
			int pos = getSelectedItemPosition();
			setSelection(pos == getCount() - 1 ? 0 : pos + 1);
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
		{
			int pos = getSelectedItemPosition();
			setSelection(pos == 0 ? getCount() - 1 : pos - 1);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public void setEpgRowStartTimeMillis(long gridStartTimeMillis)
	{
		_gridStartTimeMillis = gridStartTimeMillis;
	}

	/**
	 * This method returns the Drawable object representing the background of
	 * the given View when it was initially laid out. It is useful when
	 * changing back and forth from multiple selection backgrounds.
	 */
	public Drawable getChildLayoutBackground(View child)
	{
		return _viewsBackgrounds.get(child);
	}

	public static interface OnItemSelectingListener
	{
		/**
		 * Called before the selection of a new item completes
		 *
		 * @param parent
		 *            Parent view
		 * @param view
		 *            View selected before the selection occurs (refers to
		 *            oldPosition)
		 * @param oldPosition
		 *            Selected item's position before the new selection finishes
		 * @param newPosition
		 *            The target item's position after the selection completes
		 */
		public void onItemSelecting(AdapterView<?> parent, View view, int oldPosition, int newPosition);
	}

	public static interface OnItemUnsetSelectionListener
	{
		/**
		 * Called before the selection of a new item completes
		 *
		 * @param parent
		 *            Parent view
		 * @param view
		 *            View selected before the selection occurs (refers to
		 *            oldPosition)
		 * @param position
		 *            Selected item's position to be unset
		 */
		public void onItemUnsetSelection(AdapterView<?> parent, View view, int position);
	}
}
