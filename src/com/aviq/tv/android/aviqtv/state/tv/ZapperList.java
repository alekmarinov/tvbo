/**
 * Copyright (c) 2007-2014, AVIQ Bulgaria Ltd
 *
 * Project:     Bulsatcom
 * Filename:    ZapperList.java
 * Author:      alek
 * Date:        Jul 7, 2014
 * Description: Vertical scrolling ScrollView extension showing the selected element at
 *              specified visible position.
 */

package com.aviq.tv.android.aviqtv.state.tv;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.ScrollView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.AVKeyEvent;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.Key;
import com.aviq.tv.android.sdk.core.Log;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.feature.rcu.FeatureRCU;

/**
 * Vertical scrolling ScrollView extension showing the selected element at
 * specified visible position.
 */
public class ZapperList extends ScrollView
{
	private static final String TAG = ZapperList.class.getSimpleName();

	/**
	 * Callback interface when item selection changes
	 */
	public interface OnItemSelected
	{
		void onItemSelected(int oldPosition, int newPosition);
	}

	public static interface UnhandledKeysListener
	{
		public boolean onKeyDown(AVKeyEvent event);
	}

	/**
	 * Callback interface when item selection is about to change
	 */
	public interface OnItemSelecting
	{
		void onItemSelecting(int oldPosition, int newPosition);
	}

	/**
	 * This widget's context
	 */
	private Context _context;

	/**
	 * Layout inflater
	 */
	private LayoutInflater _inflater;

	/**
	 * Adapter generating the child views
	 */
	private Adapter _adapter;

	/**
	 * Current selected position
	 */
	private int _position = -1;

	/**
	 * Previously selected position
	 */
	private int _prevPosition = -1;

	/**
	 * Element position starting from the 1st visible element on screen to be
	 * the selected one
	 */
	private float _selectedVisiblePosition;

	/**
	 * Number of visible items on the screen
	 */
	private int _visibleItems;

	/**
	 * The height of one item in the list
	 */
	private int _itemsHeight;

	/**
	 * Delay in milliseconds before calling OnItemSelected callback
	 */
	private int _selectedDelay;

	/**
	 * Whether to hide the current selected element or not
	 */
	private boolean _hideSelected = true;

	/**
	 * On item selected callback
	 */
	private OnItemSelected _onItemSelected;

	/**
	 * On selecting item callback
	 */
	private OnItemSelecting _onItemSelecting;

	/**
	 * Handler for registering OnItemSelected timer
	 */
	private final Handler _handler = new Handler();

	/**
	 * Timer callback used to postpone calling the registered OnItemSelected
	 * callback
	 */
	private final SelectedItemCallback _delaySelectedItem = new SelectedItemCallback();

	/**
	 * Set new position callback
	 */
	private final SetPositionCallback _setPositionCallback = new SetPositionCallback();

	/**
	 * Whether the user has shown or hide the ZapperList explicitly
	 */
	private boolean _isShown = true;

	/**
	 * Number of items to be pre-inflated from the top of the current item.
	 */
	private int _topInflated;

	/**
	 * Number of items to be pre-inflated from the bottom of the current item.
	 */
	private int _bottomInflated;

	/**
	 * Height of partially displayed top item
	 */
	private float _itemsOffset;

	/**
	 * Internal reference to the scroll view scroller.
	 */
	private OverScroller _myScroller;

	/**
	 * True when zapping continuously (long key press); false otherwise.
	 */
	private boolean _zapping = false;

	/**
	 * True when view optimization is used.
	 */
	private boolean _isZapperOptimized = false;

	/**
	 * RCU key mapper
	 */
	private FeatureRCU _rcu;

	/**
	 * A callback interface for keys unhandled by the ZapperList
	 */
	private UnhandledKeysListener _unhandledKeysListener;

	/**
	 * The class for the timer callback used to postpone calling the registered
	 * OnItemSelected callback
	 */
	private class SelectedItemCallback implements Runnable
	{
		private int _oldPosition;
		private int _newPosition;

		void setPositions(int oldPosition, int newPosition)
		{
			_oldPosition = oldPosition;
			_newPosition = newPosition;
		}

		@Override
		public void run()
		{
			if (_onItemSelected != null)
			{
				Log.d(TAG, "SelectedItemCallback: " + _oldPosition + " -> " + _newPosition);
				_onItemSelected.onItemSelected(_oldPosition, _newPosition);
			}
		}
	}

	/**
	 * The class for the setPosition callback
	 */
	private class SetPositionCallback implements Runnable
	{
		private int _newposition;
		private boolean _isSmooth;

		void setPosition(int newposition, boolean isSmooth)
		{
			_newposition = newposition;
			_isSmooth = isSmooth;
		}

		@Override
		public void run()
		{
			Log.d(TAG, ".setPosition: isSmooth = " + _isSmooth + ", position = " + _newposition
			        + ", previous position = " + _position);

			if (_adapter == null || _newposition < 0 || _newposition >= _adapter.getCount())
				return;

			ZapperList.super.setVisibility(_isShown ? View.VISIBLE : View.INVISIBLE);
			if (_onItemSelecting != null)
				_onItemSelecting.onItemSelecting(_position, _newposition);

			View oldView = getViewAt(_position);
			View newView = getViewAt(_newposition);
			if (_hideSelected)
			{
				if (oldView != null)
				{
					oldView.setVisibility(View.VISIBLE);
				}

				if (newView != null)
				{
					newView.setVisibility(View.INVISIBLE);
				}
			}

			_handler.removeCallbacks(_delaySelectedItem);
			_delaySelectedItem.setPositions(_position, _newposition);
			_handler.postDelayed(_delaySelectedItem, _selectedDelay);

			_position = _newposition;

			int offset = _newposition * _itemsHeight;

			if (_itemsOffset > 0)
				offset -= (int)(_itemsHeight * _itemsOffset);

			Log.d(TAG, ".setPosition.smoothScrollTo: _position = " + _newposition + ", offset = " + offset);
			if (_isSmooth)
			{
				//smoothScrollTo(0, offset);
				_myScroller.setFriction(0.025f);
				_myScroller.startScroll((int)getX(), _prevPosition * _itemsHeight, 0, offset - (_prevPosition * _itemsHeight), 500);
				invalidate();
			}
			else
			{
				scrollTo(0, offset);
			}
		}
	}

	public ZapperList(Context context)
	{
		super(context);
		init(context, context.obtainStyledAttributes(R.styleable.ZapperList));
	}

	public ZapperList(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, context.obtainStyledAttributes(attrs, R.styleable.ZapperList));
	}

	public ZapperList(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, context.obtainStyledAttributes(attrs, R.styleable.ZapperList, defStyle, 0));
	}

	/**
	 * Initialize ZapperList object
	 *
	 * @param context
	 *            is the current Context
	 * @param attr
	 *            is a container with ZapperList attributes
	 *            R.styleable.ZapperList_*
	 */
	private void init(Context context, TypedArray attr)
	{
		_context = context;
		_selectedVisiblePosition = attr.getFloat(R.styleable.ZapperList_selectedVisiblePosition, 0.0f);
		_visibleItems = attr.getInteger(R.styleable.ZapperList_visibleItems, 0);
		_itemsHeight = (int) attr.getDimension(R.styleable.ZapperList_itemsHeight, 0);
		_selectedDelay = attr.getInteger(R.styleable.ZapperList_selectedDelay, 0);
		_hideSelected = attr.getBoolean(R.styleable.ZapperList_hideSelected, false);
		_topInflated = attr.getInteger(R.styleable.ZapperList_topInflated, 5);
		_bottomInflated = attr.getInteger(R.styleable.ZapperList_bottomInflated, 5);
		_itemsOffset = attr.getFloat(R.styleable.ZapperList_itemsOffset, 0f);

		attr.recycle();

		Log.d(TAG, ".init: _selectedVisiblePosition = " + _selectedVisiblePosition + ", _visibleItems = "
		        + _visibleItems + ", _itemsHeight = " + _itemsHeight + ", _selectedDelay = " + _selectedDelay);

		super.setVisibility(View.INVISIBLE);

		_inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Get a reference to the scroller object through reflection since it's private
		try
		{
			Class parent = this.getClass();
			int attempts = 0;
			do
			{
				parent = parent.getSuperclass();
				attempts++;
			} while (!parent.getName().equals("android.widget.ScrollView") && attempts < 20);
			Log.i(TAG, "Class: " + parent.getName());

			Field field = parent.getDeclaredField("mScroller");
			field.setAccessible(true);
			_myScroller = (OverScroller) field.get(this);
			Log.i(TAG, "Scroller found.");
		}
		catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}

		_rcu = (FeatureRCU) Environment.getInstance().getFeatureComponent(FeatureName.Component.RCU);
	}

	/**
	 * Overrides View.setVisibility
	 */
	@Override
	public void setVisibility(int visibility)
	{
		super.setVisibility(visibility);
		_isShown = visibility == View.VISIBLE;
	}

	/**
	 * Sets a generic data items provider. Note that using this method is memory
	 * inefficient since all views are loaded in memory at the same time.
	 *
	 * @param adapter
	 * @param position
	 */
    public void setAdapter(Adapter adapter, final int position)
	{
		Log.d(TAG, ".setAdapter: count = " + adapter.getCount() + ", position = " + position);

		_adapter = adapter;
		_prevPosition = -1;

		LinearLayout linearLayout = getLinearLayout();
		linearLayout.removeAllViews();

		if (_adapter.getCount() > 0)
		{
			for (int i = 0; i < _adapter.getCount(); i++)
			{
				View view = _adapter.getView(i, null, linearLayout);
				linearLayout.addView(view);
			}
			adjustFirstLastChildrenLayoutParams();
			setPosition(position, false);
		}
	}

	/**
	 * Sets data items provider for the ZapperList
	 *
	 * @param adapter
	 *            is the data items provider
	 * @param position
	 *            is the initial position to be selected
	 */
	public void setAdapter(ZapperListAdapter adapter, final int position)
	{
		Log.d(TAG, ".setAdapter (ZapperListAdapter): count = " + adapter.getCount() + ", position = " + position);

		_adapter = adapter;
		_prevPosition = -1;

		if (_adapter.getCount() <= _topInflated + _bottomInflated + 1)
		{
			_isZapperOptimized = false;
			setAdapter(_adapter, position);
			return;
		}

		initFromZapperAdapter(position, adapter.getStubLayoutResource());
	}

	/**
	 * Sets data items provider for the ZapperList
	 *
	 * @param adapter
	 *            is the data items provider
	 * @param position
	 *            is the initial position to be selected
	 */
	public <T> void setAdapter(ZapperListArrayAdapter<T> adapter, final int position)
	{
		Log.d(TAG, ".setAdapter (ZapperListArrayAdapter): count = " + adapter.getCount() + ", position = " + position);

		_adapter = adapter;
		_prevPosition = -1;

		initFromZapperAdapter(position, adapter.getStubLayoutResource());
	}

	private void initFromZapperAdapter(int position, int stubLayout)
	{
		// Reset the previous position. Important of some sort of pagination is introduced.
		_position = -1;

		_isZapperOptimized = true;

		LinearLayout linearLayout = getLinearLayout();
		linearLayout.removeAllViews();

		int numItemsInAdapter = _adapter.getCount();
		if (numItemsInAdapter < 1)
			return;

		int minPositionCached = position - _topInflated - 1;
		if (minPositionCached < 0)
			minPositionCached += numItemsInAdapter;

		int maxPositionCached = position + _bottomInflated + 1;
		if (maxPositionCached >= numItemsInAdapter)
			maxPositionCached -= numItemsInAdapter;

		// No need for stub layouts if number of elements is small enough
		if (numItemsInAdapter < 1 + _topInflated + _bottomInflated)
		{
			minPositionCached = -1;
			maxPositionCached = -1;
		}

		for (int i = 0; i < numItemsInAdapter; i++)
		{
			boolean isStubLayout;
			if (minPositionCached < maxPositionCached)
				isStubLayout = i < minPositionCached || i >= maxPositionCached ? true : false;
			else
				isStubLayout = i < minPositionCached && i >= maxPositionCached ? true : false;

			if (isStubLayout && stubLayout > 0)
			{
				_inflater.inflate(stubLayout, linearLayout, true);
			}
			else
			{
				View view = _adapter.getView(i, null, linearLayout);
				linearLayout.addView(view);
			}
		}

		adjustFirstLastChildrenLayoutParams();

		setPosition(position, false);
	}

	private void adjustFirstLastChildrenLayoutParams()
	{
		adjustChildrenLayoutParams(0, _adapter.getCount() - 1);
	}

	private void adjustChildrenLayoutParams(int... positions)
	{
		int lastItemPos = _adapter.getCount() - 1;

		for (int i = 0; i < positions.length; i++)
		{
			if (positions[i] == 0)
			{
				View childFirst = getViewAt(0);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) childFirst.getLayoutParams();
				params.topMargin = (int) (_selectedVisiblePosition * _itemsHeight);
				childFirst.setLayoutParams(params);
			}
			else if (positions[i] == lastItemPos)
			{
				View childLast = getViewAt(lastItemPos);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) childLast.getLayoutParams();
				params.bottomMargin = (int) ((_visibleItems - _selectedVisiblePosition) * _itemsHeight);
				childLast.setLayoutParams(params);
			}
			else
			{
				View child = getViewAt(positions[i]);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
				params.topMargin = 0;
				params.bottomMargin = 0;
				child.setLayoutParams(params);
			}
		}
	}

	/**
	 * Returns the adapter set by setAdapter
	 *
	 * @return Adapter
	 */
	public Adapter getAdapter()
	{
		return _adapter;
	}

    private void handleStubOnPositionChange(int position)
	{
    	Log.d(TAG, ".handleStubOnPositionChange: position = " + position + ", _prevPosition = " + _prevPosition);

		// No position change yet, so skip any processing
		if (_prevPosition < 0)
			return;

		int numItemsInAdapter = _adapter.getCount();

		boolean movingForward;
		if (position == 0 && _prevPosition == numItemsInAdapter - 1)
			movingForward = true;
		else if (position == numItemsInAdapter - 1 && _prevPosition == 0)
			movingForward = false;
		else if (position < _prevPosition)
			movingForward = false;
		else
			movingForward = true;

		LinearLayout linearLayout = getLinearLayout();

		// Find a view that can be reused

		int contentViewPosition = movingForward ? position - _topInflated - 1 : position + _bottomInflated;

		if (contentViewPosition >= numItemsInAdapter)
			contentViewPosition -= numItemsInAdapter;
		else if (contentViewPosition < 0)
			contentViewPosition += numItemsInAdapter;

		View contentView = getViewAt(contentViewPosition);

		// Find an empty view that needs to be swapped with the view with actual contents

		int stubViewPosition = movingForward ? position + _bottomInflated - 1 : position - _topInflated;

		if (stubViewPosition >= numItemsInAdapter)
			stubViewPosition -= numItemsInAdapter;
		else if (stubViewPosition < 0)
			stubViewPosition += numItemsInAdapter;

		Log.d(TAG, "pos = " + position + ", swap " + contentViewPosition + " (c) and " + stubViewPosition + " (s)");

		View stubView = getViewAt(stubViewPosition);

		// Now we are ready to swap the views
		// Re-populate the contentView for its new position at stubViewPosition
		contentView = _adapter.getView(stubViewPosition, contentView, linearLayout);

		// Remove the contentView and the stubView from their current positions.
		// Then swap the positions of the contentView and the stubView, i.e.
		// re-add them from smallest position to largest
		if (contentViewPosition > stubViewPosition)
		{
			linearLayout.removeViewAt(contentViewPosition);
			linearLayout.removeViewAt(stubViewPosition);

			linearLayout.addView(contentView, stubViewPosition);
			linearLayout.addView(stubView, contentViewPosition);
		}
		else
		{
			linearLayout.removeViewAt(stubViewPosition);
			linearLayout.removeViewAt(contentViewPosition);

			linearLayout.addView(stubView, contentViewPosition);
			linearLayout.addView(contentView, stubViewPosition);
		}

		// Re-calculate some layout parameters
		if (contentViewPosition == 0 || contentViewPosition == numItemsInAdapter - 1 || stubViewPosition == 0
		        || stubViewPosition == numItemsInAdapter - 1)
		{
			adjustChildrenLayoutParams(0, numItemsInAdapter - 1, contentViewPosition, stubViewPosition);
		}
	}

	/**
	 * Sets the current selected position
	 *
	 * @param position
	 *            the new position to be selected
	 * @param isSmooth
	 *            set to true whether we want to scroll to the position smoothly
	 */
	public void setPosition(final int position, final boolean isSmooth)
	{
		Log.d(TAG, ".setPosition: position = " + position + ", isSmooth = " + isSmooth);

		_prevPosition = _position;

		// Only do this if the adapter supports it
		if (_isZapperOptimized && (_adapter instanceof ZapperListAdapter || _adapter instanceof ZapperListArrayAdapter))
		{
			handleStubOnPositionChange(position);
		}

		_setPositionCallback.setPosition(position, isSmooth);
		// select to the new position now...
		// _setPositionCallback.run();
		// ...or at the next UI update
		if (isSmooth)
			_setPositionCallback.run();
		else
			_handler.post(_setPositionCallback);
		// ...or after some time
		// _handler.postDelayed(_setPositionCallback, 100);
	}

	/**
	 * Sets the current selected position by smooth scrolling
	 *
	 * @param position
	 *            the new position to be selected
	 */
	private void setPosition(int position)
	{
		setPosition(position, true);
	}

	/**
	 * @return the current selected position
	 */
	public int getPosition()
	{
		return _position;
	}

	/**
	 * @return the number of items in ZapperList's adapter
	 */
	public int getCount()
	{
		if (_adapter != null)
			return _adapter.getCount();
		return 0;
	}

	/**
	 * Returns the underlying LinerLayout object holding the
	 *
	 * @return
	 */
	private LinearLayout getLinearLayout()
	{
		View child = getChildAt(0);
		if (!(child instanceof LinearLayout))
			throw new IllegalStateException("ZapperList requires LinerLayout child element");
		return (LinearLayout) child;
	}

	/**
	 * Gets child view at a given position
	 *
	 * @param position
	 *            is the child element position
	 * @return View at the given position or null if the position is out of
	 *         bounds
	 */
	public View getViewAt(int position)
	{
		if (position >= 0 && position < getCount())
		{
			return getLinearLayout().getChildAt(position);
		}
		return null;
	}

	public int getViewCount()
	{
		return getLinearLayout().getChildCount();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.i(TAG, ".onKeyDown: keyCode = " + keyCode);

		Key key = _rcu.getKey(keyCode);

		event.startTracking();
		_zapping = event.getRepeatCount() > 0 ? true : false;

		switch (key)
		{
			case LEFT:
				clearFocus();
				return false; // !!! keep this transparent

			case UP:
			{
				int pos = getPosition();
				if (pos == 0)
					setPosition(getCount() - 1);
				else
					setPosition(pos - 1);
				return true;
			}

			case DOWN:
			{
				int pos = getPosition();
				if (pos == getCount() - 1)
					setPosition(0);
				else
					setPosition(pos + 1);
				return true;
			}

			default:
				break;
		}

		AVKeyEvent e = new AVKeyEvent(event, key);
		boolean isHandled = _unhandledKeysListener != null ?
				_unhandledKeysListener.onKeyDown(e) || super.onKeyDown(keyCode, event)
					: super.onKeyDown(keyCode, event);

		return isHandled;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		Log.i(TAG, ".onKeyUp: keyCode = " + keyCode);
		_zapping = false;
		return true;
	}

	public boolean isZapping()
	{
		return _zapping;
	}

	/**
	 * Set on item selected callback
	 *
	 * @param onItemSelected
	 *            callback invoked when the new position changes
	 */
	public void setOnItemSelected(OnItemSelected onItemSelected)
	{
		_onItemSelected = onItemSelected;
	}

	/**
	 * Set on item selecting callback
	 *
	 * @param onItemSelecting
	 *            callback invoked when the new position is about to change
	 */
	public void setOnItemSelecting(OnItemSelecting onItemSelecting)
	{
		_onItemSelecting = onItemSelecting;
	}

	@Override
	public void fling(int arg0)
	{
		// Scroll view is no longer gonna handle scroll velocity.
		// super.fling(arg0);
	}

	public int getTopInflated()
	{
		return _topInflated;
	}

	public int getBottomInflated()
	{
		return _bottomInflated;
	}

	/**
	 * Note that the value returned here does not take into account circular lists.
	 */
	public int getTopInflatedPosition()
	{
		return Math.max(0, _position - _topInflated);
	}

	/**
	 * Note that the value returned here does not take into account circular lists.
	 */
	public int getBottomInflatedPosition()
	{
		return Math.min(_adapter.getCount(), _position + _bottomInflated);
	}

	public void setUnhandledKeyListener(UnhandledKeysListener unhandledKeysListener)
	{
		_unhandledKeysListener = unhandledKeysListener;
	}

	public static abstract class ZapperListAdapter extends BaseAdapter
	{
		private final int _stubLayoutResource;

		public <T> ZapperListAdapter(Context context, List<T> itemList, int stubLayoutResource)
		{
			_stubLayoutResource = stubLayoutResource;
		}

		public int getStubLayoutResource()
		{
			return _stubLayoutResource;
		}
	}

	public static abstract class ZapperListArrayAdapter<T> extends ArrayAdapter<T>
	{
		private final int _stubLayoutResource;

		public ZapperListArrayAdapter(Context context, List<T> itemList, int stubLayoutResource)
		{
			super(context, -1, itemList);
			_stubLayoutResource = stubLayoutResource;
		}

		public int getStubLayoutResource()
		{
			return _stubLayoutResource;
		}
	}
}
