package com.aviq.tv.android.test.horizlist;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.aviq.tv.android.test.R;

public class HorizListActivity extends Activity
{
	private static final String TAG = HorizListActivity.class.getSimpleName();
//	private HorizListView _horizListView;
	private LinearLayout _horizListView;

	private static int[] dataObjects = new int[]
	{
		R.drawable.ic_menu_epg, R.drawable.ic_menu_my_channels, R.drawable.ic_menu_watchlist, R.drawable.ic_menu_settings,
		R.drawable.ic_menu_epg, R.drawable.ic_menu_my_channels, R.drawable.ic_menu_watchlist, R.drawable.ic_menu_settings,
		R.drawable.ic_menu_epg, R.drawable.ic_menu_my_channels, R.drawable.ic_menu_watchlist, R.drawable.ic_menu_settings,
		R.drawable.ic_menu_epg, R.drawable.ic_menu_my_channels, R.drawable.ic_menu_watchlist, R.drawable.ic_menu_settings
		};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_horiz_list);
		// _horizListView = (HorizListView) findViewById(R.id.horizListView);
		_horizListView = (LinearLayout) findViewById(R.id.horizListView);
		//_horizListView.setAdapter(mAdapter);
		for (int i = 0; i < mAdapter.getCount(); i++)
		{
			_horizListView.addView(mAdapter.getView(i, null, _horizListView));
		}
	}

	private BaseAdapter mAdapter = new BaseAdapter()
	{
		@Override
		public int getCount()
		{
			Log.i(TAG, ".getCount -> " + dataObjects.length);
			return dataObjects.length;
		}

		@Override
		public Object getItem(int position)
		{
			Log.i(TAG, ".getItem: position = " + position);
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			Log.i(TAG, ".getItemId: position = " + position);
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			Log.i(TAG, ".getView: position = " + position);
			ImageButton button = (ImageButton) LayoutInflater.from(parent.getContext()).inflate(R.layout.horizlist_item, null);
			button.setImageResource(dataObjects[position]);

			button.setOnClickListener(new OnClickListener()
			{
				@Override
                public void onClick(View v)
                {
					Log.i(TAG, ".onClick: v = " + v);
                }
			});
			return button;
		}
	};
}
