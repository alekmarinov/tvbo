package com.aviq.tv.android.aviqtv.state;

import android.content.Context;
import android.content.res.TypedArray;
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
	private int _buttonSpacing;
	private int _buttonPaddingLeft;
	private int _buttonPaddingRight;

	public ContextButtonGroup(Context context)
	{
		super(context);
		init(context, context.obtainStyledAttributes(R.styleable.ContextButtonGroup));
	}

	public ContextButtonGroup(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
		init(context, context.obtainStyledAttributes(attrs, R.styleable.ContextButtonGroup));
	}

	public ContextButtonGroup(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context, context.obtainStyledAttributes(attrs, R.styleable.ContextButtonGroup, defStyle, 0));
	}

	public void init(Context context, TypedArray attr)
	{
		_layoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		_buttonSpacing = (int) attr.getDimension(R.styleable.ContextButtonGroup_buttonSpacing, 0);
		_buttonPaddingLeft = (int) attr.getDimension(R.styleable.ContextButtonGroup_buttonPaddingLeft, 0);
		_buttonPaddingRight = (int) attr.getDimension(R.styleable.ContextButtonGroup_buttonPaddingRight, 0);
		
		attr.recycle();
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

		switch (getOrientation())
		{
			case HORIZONTAL:
			{
				// Horizontal layout will get buttons as wide as their contents
				LayoutParams lp = (LinearLayout.LayoutParams) button.getLayoutParams();
				lp.width = LayoutParams.WRAP_CONTENT;
				lp.rightMargin = _buttonSpacing;
				button.setLayoutParams(lp);
				break;
			}
				
			case VERTICAL:
			{
				// Vertical layout will get buttons as wide as the parent container
				LayoutParams lp = (LinearLayout.LayoutParams) button.getLayoutParams();
				lp.width = LayoutParams.FILL_PARENT;
				lp.bottomMargin = _buttonSpacing;
				button.setLayoutParams(lp);
				break;
			}
				
			default:
				Log.w(TAG,
				        "Unspecified layout orientation. Default layout parameters will be used for the context buttons.");
				break;
		}

		button.setPadding(_buttonPaddingLeft, button.getPaddingTop(), _buttonPaddingRight, button.getPaddingBottom());
		
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
		Log.i(TAG, ".onKeyDown: keyCode = " + keyCode + ", child count = " + getChildCount() + ", _focusedChildIndex = " + _focusedChildIndex);

		if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
		{
			_focusedChildIndex--;
			if (_focusedChildIndex < 0)
			{
				_focusedChildIndex = getChildCount() - 1;
				getChildAt(_focusedChildIndex).requestFocus();
				return true;
			}
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
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
