package com.aviq.tv.android.aviqtv.state.epg;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.feature.epg.Program;

public class EpgRowAdapter extends BaseAdapter
{
	public static final String TAG = EpgRowAdapter.class.getSimpleName();

	private final Context mContext;
	private final LayoutInflater mLayoutInflater;
	private final List<Program> mProgramList;

	public EpgRowAdapter(Context context, List<Program> programList)
	{
		mContext = context;
		mProgramList = programList;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount()
	{
		return mProgramList.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mProgramList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		//Log.v(TAG, ".getView: position = " + position + ", convertView = " + convertView);

		ViewHolder holder = null;
		TimeBasedRelativeLayout view;
		if (convertView == null)
		{
			view =  (TimeBasedRelativeLayout) mLayoutInflater.inflate(R.layout.epg_grid_item_program, null);

			holder = new ViewHolder();
			holder.title = (TextView) view.findViewById(R.id.title);

			view.setTag(holder);
		}
		else
		{
			view = (TimeBasedRelativeLayout) convertView;
			holder = (ViewHolder) view.getTag();
		}

		Program program = (Program) getItem(position);

		holder.title.setText(program.getTitle());

		// TODO: remove when done testing; leave line above
//		holder.title.setText(
//				program.getStartTimeCalendar().get(Calendar.HOUR_OF_DAY)
//				+ ":"
//				+ program.getStartTimeCalendar().get(Calendar.MINUTE)
//				+ " - "
//				+ program.getStopTimeCalendar().get(Calendar.HOUR_OF_DAY)
//				+ ":"
//				+ program.getStopTimeCalendar().get(Calendar.MINUTE)
//				+ "\n"
//				+ program.getTitle());

		long programLengthInSec = (program.getStopTimeCalendar().getTimeInMillis() - program.getStartTimeCalendar().getTimeInMillis()) / 1000;
		view.setTimeInSeconds(programLengthInSec);

		return view;
    }

	public int getItemPosition(Program program)
	{
		if (program == null)
			return -1;

		int position = 0;
		String guid = program.getId();
		for (Program p : mProgramList)
		{
			if (guid.equals(p.getId()))
				return position;
			position++;
		}
		return -1;
	}

	public static class ViewHolder
	{
		public TextView title;
	}
}
