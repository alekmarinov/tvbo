package com.aviq.tv.android.home.feature.state.epg;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.aviq.tv.android.home.R;
import com.aviq.tv.android.home.core.Environment;
import com.aviq.tv.android.home.core.feature.FeatureName;
import com.aviq.tv.android.home.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.home.feature.epg.FeatureEPG;
import com.aviq.tv.android.home.feature.epg.FeatureEPG.IOnProgramDetails;
import com.aviq.tv.android.home.feature.epg.Program;
import com.aviq.tv.android.home.feature.epg.ProgramAttribute;

public class EpgProgramInfo
{
	private static final String TAG = EpgProgramInfo.class.getSimpleName();
	
	// Common to epg_program_details.xml and state_program.info.xml
	private TextView _primaryTitle;
	private TextView _secondaryTitle;
	
	// From epg_program_details.xml
	private TextView _summary;
	
	// From state_program.info.xml
	private ImageView _thumbnail;
	private TextView _date;
	private TextView _time;
	private TextView _timeRange;
	private TextView _pager;
	
	// Inner properties
	private SimpleDateFormat _dateFormat;
	private SimpleDateFormat _timeFormat;
	private String _timeRangeTemplate;
	private String _pagerTemplate;
	private FeatureEPG _featureEPG;
	
	public EpgProgramInfo(Context context, ViewGroup container)
	{
		_thumbnail = (ImageView) container.findViewById(R.id.thumbnail);
		_date = (TextView) container.findViewById(R.id.date);
		_time = (TextView) container.findViewById(R.id.time);
		_primaryTitle = (TextView) container.findViewById(R.id.title_primary);
		_secondaryTitle = (TextView) container.findViewById(R.id.title_secondary);
		_summary = (TextView) container.findViewById(R.id.summary);
		_timeRange = (TextView) container.findViewById(R.id.time_range);
		_pager = (TextView) container.findViewById(R.id.pager);
		
		String dateFormat = context.getString(R.string.programInfoDateFormat);
		_dateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
		
		String timeFormat = context.getString(R.string.programInfoTimeFormat);
		_timeFormat = new SimpleDateFormat(timeFormat, Locale.getDefault());
		
		_timeRangeTemplate = context.getString(R.string.programInfoTimeRange);
		_pagerTemplate = context.getString(R.string.programInfoPager);
		
		try
		{
			_featureEPG = (FeatureEPG) Environment.getInstance().getFeatureComponent(FeatureName.Component.EPG);
		}
		catch (FeatureNotFoundException e)
		{
			Log.e(TAG, e.getMessage(), e);
			throw new RuntimeException("FeatureEPG is a required component for " + TAG);
		}
	}
	
	public void updateBrief(final String channelId, final Program program)
	{
		// Update start date and time
		
		long startTimeMillis = program.getStartTimeCalendar().getTimeInMillis();
		
		String startDate = _dateFormat.format(startTimeMillis);
		_date.setText(startDate);
		
		String startTime = _timeFormat.format(startTimeMillis);
		_time.setText(startTime);
		
		// Update other fields
		
		_primaryTitle.setText(program.getTitle());
		
		_featureEPG.getProgramDetails(channelId, program, new IOnProgramDetails()
		{
			@Override
			public void onProgramDetails(Program program)
			{
				_secondaryTitle.setText(program.getDetailAttribute(ProgramAttribute.SUBTITLE));
				_summary.setText(program.getDetailAttribute(ProgramAttribute.SUMMARY));
			}
			
			@Override
			public void onError(int resultCode)
			{
				Log.e(TAG, ".updateBrief: error loading program details for channel = " + channelId + ", program = "
				        + program.getId() + ", " + program.getTitle());
				
				_secondaryTitle.setText(null);
				_summary.setText(null);
			}
		});
	}
	
	public void updateDetails(final String channelId, final Program program)
	{
		// Update start date and time
		
		long startTimeMillis = program.getStartTimeCalendar().getTimeInMillis();
		
		String startDate = _dateFormat.format(startTimeMillis);
		_date.setText(startDate);
		
		String startTime = _timeFormat.format(startTimeMillis);
		_time.setText(startTime);
		
		// Update time range
		
		String rangeStartTime = _timeFormat.format(startTimeMillis);
		
		Calendar stopTimeCal = program.getStopTimeCalendar();
		String rangeStopTime = _timeFormat.format(stopTimeCal.getTime());
		
		_timeRange.setText(String.format(_timeRangeTemplate, rangeStartTime, rangeStopTime));
		
		// Update other fields
		
		_primaryTitle.setText(program.getTitle());
		_secondaryTitle.setText(null);
		_summary.setText(null);
		
		_pager.setText(String.format(_pagerTemplate, 1, 1));
		
		_featureEPG.getProgramDetails(channelId, program, new IOnProgramDetails()
		{
			@Override
			public void onProgramDetails(Program program)
			{
				_secondaryTitle.setText(program.getDetailAttribute(ProgramAttribute.SUBTITLE));
				_summary.setText(program.getDetailAttribute(ProgramAttribute.SUMMARY));
			}
			
			@Override
			public void onError(int resultCode)
			{
				Log.e(TAG, ".updateDetails: error loading program details for channel = " + channelId + ", program = "
				        + program.getId() + ", " + program.getTitle());
				
				_secondaryTitle.setText(null);
				_summary.setText(null);
			}
		});
		
		// Update image
		
		String imageUrl = program.getDetailAttribute(ProgramAttribute.IMAGE_URL);
		if (imageUrl != null)
		{
			ImageLoader imageLoader = Environment.getInstance().getImageLoader();
			ImageListener imageListener = imageLoader.getImageListener(_thumbnail, R.drawable.epg_image_thumbnail,
			        R.drawable.epg_image_thumbnail);
			imageLoader.get(imageUrl, imageListener);
		}
	}
}
