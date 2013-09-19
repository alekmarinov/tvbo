package com.aviq.tv.android.home.state.overlay;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aviq.tv.android.home.Constants;
import com.aviq.tv.android.home.R;

public class OverlayFragment extends Fragment
{
	private static final String TAG = OverlayFragment.class.getSimpleName();
	
	private ViewGroup _overlayContainer;
	private TextView _overlayMessage;
	
	public static OverlayFragment newInstance(int msgResId, int bkgdResId)
	{
		if (msgResId < 1 || bkgdResId < 1)
			throw new IllegalArgumentException(".newInstance: Invalid arguments for " + TAG);
		
		OverlayFragment f = new OverlayFragment();
		
		Bundle args = new Bundle();
		args.putInt(Constants.PARAM_MESSAGE_RES_ID, msgResId);
		args.putInt(Constants.PARAM_BACKGROUND_RES_ID, bkgdResId);
		f.setArguments(args);
		
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_overlay, container, false);
		_overlayContainer = (ViewGroup) viewGroup.findViewById(R.id.container);
		_overlayMessage = (TextView) viewGroup.findViewById(R.id.message);
		
		Bundle params = getArguments();
		if (params == null)
			throw new IllegalArgumentException(".onCreateView: Invalid arguments for " + TAG);
		
		int msgResId = params.getInt(Constants.PARAM_MESSAGE_RES_ID);
		int bkgdResId = params.getInt(Constants.PARAM_BACKGROUND_RES_ID);
		
		_overlayContainer.setBackgroundResource(bkgdResId);
		_overlayMessage.setText(msgResId);
		
		return viewGroup;
	}
	
}
