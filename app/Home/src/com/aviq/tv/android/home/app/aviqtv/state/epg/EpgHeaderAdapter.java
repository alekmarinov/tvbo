package com.aviq.tv.android.home.app.aviqtv.state.epg;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aviq.tv.android.home.R;

public class EpgHeaderAdapter extends BaseAdapter
{
	private final List<Calendar> mObjects;
	private final LayoutInflater mInflater;

	public EpgHeaderAdapter(Context context, List<Calendar> objects)
	{
		mObjects = objects;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
    public int getCount()
    {
	    return mObjects.size();
    }

	@Override
    public Object getItem(int position)
    {
	    return mObjects.get(position);
    }

	@Override
    public long getItemId(int position)
    {
	    return position;
    }

	@Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
		ViewHolder holder = null;
		if (convertView == null)
		{
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.epg_grid_header_item, parent);
			holder.timeString = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		Calendar cal = (Calendar) getItem(position);

		int hour = cal.get(Calendar.HOUR_OF_DAY);
		String hourStr = hour > 9 ? "" + hour : "0" + hour;

		int minute = cal.get(Calendar.MINUTE);
		String minuteStr = minute > 9 ? "" + minute : "0" + minute;

		String timeString = hourStr + ":" + minuteStr;
		holder.timeString.setText(timeString);

	    return convertView;
    }

	private static class ViewHolder
	{
		TextView timeString;
	}
}
