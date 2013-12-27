package com.aviq.tv.android.aviqtv.state;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.Log;

public class ContextButtonGroup extends LinearLayout
{
	private static final String TAG = ContextButtonGroup.class.getSimpleName();

	private LayoutInflater _layoutInflator;
	private int _focusedChildIndex = 0;
	private OnClickListener _buttonOnClickListener;

	public ContextButtonGroup(Context context)
	{
		super(context);
		init(context);
	}

	public ContextButtonGroup(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
		init(context);
	}

	public ContextButtonGroup(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}

	public void init(Context context)
	{
		_layoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public ContextButton createButton()
	{
		return createButton(0, 0);
	}

	public ContextButton createButton(int iconResId, int textResId)
	{
		ContextButton button = (ContextButton) _layoutInflator.inflate(R.layout.context_button, this, false);
		button.setContent(iconResId, textResId);

		if (_buttonOnClickListener != null)
			button.setOnClickListener(_buttonOnClickListener);

		addView(button);
		getChildAt(0).requestFocus();

		return button;
	}

	public void setButtonOnClickListener(OnClickListener listener)
	{
		_buttonOnClickListener = listener;

		for (int i = 0; i < getChildCount(); i++)
			((Button) getChildAt(i)).setOnClickListener(_buttonOnClickListener);
	}

	/**
	 * This method is only necessary to wrap the navigation around the elements.
	 * If it is removed, the widget will work as expected without wrapping the
	 * navigation around the elements.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Log.i(TAG, ".onKeyDown: keyCode = " + keyCode);

		if (keyCode == KeyEvent.KEYCODE_DPAD_UP)
		{
			_focusedChildIndex--;
			if (_focusedChildIndex < 0)
			{
				_focusedChildIndex = getChildCount() - 1;
				getChildAt(_focusedChildIndex).requestFocus();
				return true;
			}
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
		{
			_focusedChildIndex++;
			if (_focusedChildIndex >= getChildCount())
			{
				_focusedChildIndex = 0;
				getChildAt(_focusedChildIndex).requestFocus();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
