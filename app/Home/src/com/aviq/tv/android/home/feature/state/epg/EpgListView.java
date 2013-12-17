package com.aviq.tv.android.home.feature.state.epg;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.aviq.tv.android.home.R;

public class EpgListView extends ListView
{
	private static final String TAG = EpgListView.class.getSimpleName();

	private Context _context;
	private OnItemSelectingListener _onItemSelectingListener;
	private OnItemSelectedListener _onItemSelectedListener;
	private int _prevPosition = -1;

	public EpgListView(Context context)
	{
		super(context);
		init(context, context.obtainStyledAttributes(R.styleable.EpgListView));
	}

	public EpgListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, context.obtainStyledAttributes(attrs, R.styleable.EpgListView));
	}

	public EpgListView(Context context, AttributeSet attrs, int defStyle)
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
//		_visibleItems = attr.getInteger(R.styleable.ZapperList3_visibleItems, 0);
//		_itemsHeight = (int) attr.getDimension(R.styleable.ZapperList3_itemsHeight, 0);
//		_hideSelected = attr.getBoolean(R.styleable.ZapperList3_hideSelected, false);
//		_itemsOffset = attr.getFloat(R.styleable.ZapperList3_itemsOffset, 0f);

		attr.recycle();
		setItemsCanFocus(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		_prevPosition = getSelectedItemPosition();

		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
		{
			int newPosition = _prevPosition == getCount() - 1 ? 0 : _prevPosition + 1;
			setSelection(newPosition);
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
		{
			int newPosition = _prevPosition == 0 ? getCount() - 1 : _prevPosition - 1;
			setSelection(newPosition);
		}
		return true;
	}

	public View getViewAt(int positionInAdapter)
	{
		/*
		 * A not so obvious fact is that getChildAt() returns the View object of
		 * the child whose index position is counted from the firstVisibleChild.
		 * So this is not the position of the item in the adapter!
		 */
		int firstVisiblePosition = getFirstVisiblePosition();
		int screenPosition = firstVisiblePosition == 0 ? positionInAdapter : positionInAdapter % firstVisiblePosition;
		return getChildAt(screenPosition);
	}

	@Override
    public void setSelection(final int position)
	{
		if (_onItemSelectingListener != null)
		{
			_onItemSelectingListener.onItemSelecting(EpgListView.this, getSelectedView(), _prevPosition, position);
		}

		super.setSelection(position);

		// The _onItemSelectedListener is called by the parent class
	}

	public void setOnItemSelectingListener(OnItemSelectingListener listener)
	{
		_onItemSelectingListener = listener;
	}

	@Override
	public void setOnItemSelectedListener(OnItemSelectedListener listener)
	{
		_onItemSelectedListener = listener;
		super.setOnItemSelectedListener(_onItemSelectedListener);
	}

	public interface OnItemSelectingListener
	{
		/**
		 * Called before the selection of a new item completes
		 * @param parent Parent view
		 * @param view View selected before the selection occurs (refers to oldPosition)
		 * @param oldPosition Selected item's position before the new selection finishes
		 * @param newPosition The target item's position after the selection completes
		 */
		public void onItemSelecting(AdapterView<?> parent, View view, int oldPosition, int newPosition);
	}
}
