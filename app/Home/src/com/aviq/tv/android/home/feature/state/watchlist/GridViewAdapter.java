package com.aviq.tv.android.home.feature.state.watchlist;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.feature.epg.Channel;
import com.aviq.tv.android.home.feature.epg.Program;

public class GridViewAdapter<T> extends BaseAdapter
{
	private List<T> _items;
	private Context _context;
	private int _itemLayoutId;;

	public GridViewAdapter(Context context, List<T> items, int layoutId)
	{
		_items = items;
		_context = context;
		_itemLayoutId = layoutId;
	}

	@Override
	public int getCount()
	{
		return _items.size();
	}

	@Override
	public Object getItem(int position)
	{
		return _items.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder;
		if (convertView == null)
		{
			LayoutInflater inflator = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflator.inflate(_itemLayoutId, null);

			holder = new ViewHolder();
			holder.textView = (TextView) convertView.findViewById(R.id.title);
			holder.image = (ImageView) convertView.findViewById(R.id.thumbnail);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		Object obj = _items.get(position);
		if (obj instanceof Program)
			holder.textView.setText(((Program)obj).getTitle());

		else if (obj instanceof Channel)
		{
			holder.textView.setText(position + " " + ((Channel)obj).getTitle());

//			Bitmap bmp = ((Channel)obj).getChannelLogoBitmap(i);
//			if (bmp == null)
//				Log.w(TAG, "Channel " + _epgData.getChannel(i).getChannelId() + " doesn't have image logo!");
//			holder.image.setImageBitmap(bmp);
		}

		if (holder.image != null)
		{
			//holder.image.setImageResource(program.getImage());
		}

		return convertView;
	}

	protected class ViewHolder
	{
		public TextView textView;
		public ImageView image;
	}
}
