package com.aviq.tv.android.home.feature.state.epg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.feature.epg.Channel;
import com.aviq.tv.android.home.feature.epg.IEpgDataProvider;
import com.aviq.tv.android.home.feature.epg.Program;

public class EpgListViewAdapter extends BaseAdapter
{
	@SuppressWarnings("unused")
    private static final String TAG = EpgListViewAdapter.class.getSimpleName();

	private final ImageLoader mImageLoader;
	private final Context mContext;
	private final IEpgDataProvider mDataProvider;
	private final Map<Channel, List<Program>> mChannelsToPrograms;
	private final List<Channel> mChannelList;
	private final LayoutInflater mLayoutInflater;
	private final Map<String, EpgRowAdapter> mProgramListAdapters;
	private EpgRowView.OnItemSelectingListener mOnProgramItemSelectingListener;
	private EpgRowView.OnItemSelectedListener mOnProgramItemSelectedListener;
	private EpgRowView.OnItemUnsetSelectionListener mOnProgramItemUnsetSelectionListener;
	private long mGridStartTimeMillis;
//	private final boolean _isUsingFavorites;

	public EpgListViewAdapter(Context context, IEpgDataProvider service, Map<Channel, List<Program>> channelsToPrograms)
	{
		mContext = context;
		mDataProvider = service;
		mChannelsToPrograms = channelsToPrograms;
		mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mImageLoader = Environment.getInstance().getImageLoader();

//		_isUsingFavorites = mDataProvider.isUseFavorites();

		// Keep a separate list of channels for quick access
		mChannelList = new ArrayList<Channel>(mChannelsToPrograms.keySet());

		// Prepare the list with adapters, so that we have them ready in memory
		mProgramListAdapters = new HashMap<String, EpgRowAdapter>();
		for (Map.Entry<Channel, List<Program>> entry : mChannelsToPrograms.entrySet())
		{
			EpgRowAdapter adapter = new EpgRowAdapter(context, entry.getValue());
			mProgramListAdapters.put(entry.getKey().getChannelId(), adapter);
		}
	}

	@Override
	public int getCount()
	{
		return mChannelList.size();
	}

	@Override
	public Object getItem(int position)
	{
		return mChannelList.get(position);
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

		// Get Channel object for the requested position
		Channel channel = (Channel) getItem(position);
		final String channelId = channel.getChannelId();

		final EpgRowAdapter epgRowAdapter = mProgramListAdapters.get(channelId);

		final ViewHolder holder;
		if (convertView == null)
		{
			convertView = mLayoutInflater.inflate(R.layout.epg_grid_item_channel, null);

			holder = new ViewHolder();

			holder.channelNo = (TextView) convertView.findViewById(R.id.channelNo);
			holder.channelLogo = (ImageView) convertView.findViewById(R.id.logo);

			holder.programList = (EpgRowView) convertView.findViewById(R.id.program_list);

			//holder.programList.setSelectedItemBackgroundResourceIdOnLayout(R.drawable.epg_grid_cell_selected);

			holder.programList.setSelectedItemBackgroundResourceId(R.drawable.epg_grid_cell_selected);

			holder.programList.setOnItemSelectingListener(mOnProgramItemSelectingListener);
			holder.programList.setOnItemSelectedListener(mOnProgramItemSelectedListener);
			holder.programList.setOnItemUnsetSelectionListener(mOnProgramItemUnsetSelectionListener);

			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}

		int channelIndex = 1 + mDataProvider.getChannelIndex(channel); //_isUsingFavorites ? channel.getFavIndex() : channel.getIndex();
		holder.channelNo.setText("" + channelIndex);
		
		Bitmap logo = mDataProvider.getChannelLogoBitmap(channelIndex);
		holder.channelLogo.setImageBitmap(logo);
		holder.programList.setEpgRowStartTimeMillis(mGridStartTimeMillis);
		holder.programList.setAdapter(epgRowAdapter);

		return convertView;
	}

	public int getItemPosition(Channel channel)
	{
		int position = 0;
		String channelId = channel.getChannelId();
		for (Map.Entry<Channel, List<Program>> entry : mChannelsToPrograms.entrySet())
		{
			if (channelId.equals(entry.getKey().getChannelId()))
				return position;
			position++;
		}
		return -1;
	}

	public void setOnProgramItemSelectingListener(EpgRowView.OnItemSelectingListener listener)
	{
		mOnProgramItemSelectingListener = listener;
	}

	public void setOnProgramItemSelectedListener(EpgRowView.OnItemSelectedListener listener)
	{
		mOnProgramItemSelectedListener = listener;
	}

	public void setOnProgramItemUnsetSelectionListener(EpgRowView.OnItemUnsetSelectionListener listener)
	{
		mOnProgramItemUnsetSelectionListener = listener;
	}

	public void setEpgRowStartTime(long gridStartTimeMillis)
	{
		mGridStartTimeMillis = gridStartTimeMillis;
	}

	public static class ViewHolder
	{
		public TextView channelNo;
		public ImageView channelLogo;
		public EpgRowView programList;
	}
}
