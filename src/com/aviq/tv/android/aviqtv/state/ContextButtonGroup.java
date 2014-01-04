package com.aviq.tv.android.aviqtv.state;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
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
				// Vertical layout will get buttons as wide as the parent
				// container
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
		button.setTag(new Integer(getChildCount() - 1));
		getChildAt(0).requestFocus();

		return button;
	}

	public void setButtonOnClickListener(OnClickListener listener)
	{
		_buttonOnClickListener = listener;

		for (int i = 0; i < getChildCount(); i++)
			((Button) getChildAt(i)).setOnClickListener(_buttonOnClickListener);
	}

	public int childIndexOf(View child)
	{
		for (int i = 0; i < getChildCount(); i++)
		{
			if (getChildAt(i).equals(child))
				return i;
		}
		return -1;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent)
	{
		int index = childIndexOf(getFocusedChild());
		if (index == -1 && getChildCount() > 0)
			getChildAt(0).requestFocus();

		else
		{
			switch (keyCode)
			{
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_UP:
					if (index > 0)
						getChildAt(index - 1).requestFocus();
					else
						getChildAt(getChildCount() - 1).requestFocus();
					return true;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
				case KeyEvent.KEYCODE_DPAD_DOWN:
					if (index < getChildCount() - 1)
						getChildAt(index + 1).requestFocus();
					else
						getChildAt(0).requestFocus();
					return true;
			}
		}
		return super.onKeyDown(keyCode, keyEvent);
	}
}
