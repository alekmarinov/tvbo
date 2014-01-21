package com.aviq.tv.android.aviqtv.state;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class ContextButton extends Button
{
	public ContextButton(Context context)
    {
	    super(context);
    }

	public ContextButton(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
		init(context);
	}

	public ContextButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}

	public void init(Context context)
	{
	}

	public void setContent(int iconResId, int textResId)
	{
		if (textResId > 0)
			setText(textResId);

		if (iconResId > 0)
			setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);

		setId(textResId);
	}
}
