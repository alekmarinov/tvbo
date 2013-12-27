package com.aviq.tv.android.aviqtv.state.epg;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.aviq.tv.android.aviqtv.R;
import com.aviq.tv.android.sdk.core.Environment;
import com.aviq.tv.android.sdk.core.feature.FeatureName;
import com.aviq.tv.android.sdk.core.feature.FeatureNotFoundException;
import com.aviq.tv.android.sdk.feature.epg.FeatureEPG;
import com.aviq.tv.android.sdk.feature.epg.FeatureEPG.IOnProgramDetails;
import com.aviq.tv.android.sdk.feature.epg.Program;
import com.aviq.tv.android.sdk.feature.epg.ProgramAttribute;

public class EpgProgramInfo
{
	private static final String TAG = EpgProgramInfo.class.getSimpleName();
	protected static final float LINE_SPACING_ADD = 4f;
	protected static final float LINE_SPACING_MULT = 1f;

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

	private Context _context;
	private ViewFlipper _detailsFlipper;
	private int _textViewHeight;
	private int _textViewWidth;

	public EpgProgramInfo(Context context, ViewGroup container)
	{
		_context = context;
		_thumbnail = (ImageView) container.findViewById(R.id.thumbnail);
		_date = (TextView) container.findViewById(R.id.date);
		_time = (TextView) container.findViewById(R.id.time);
		_primaryTitle = (TextView) container.findViewById(R.id.title_primary);
		_secondaryTitle = (TextView) container.findViewById(R.id.title_secondary);
		_summary = (TextView) container.findViewById(R.id.summary);
		_timeRange = (TextView) container.findViewById(R.id.time_range);
		_pager = (TextView) container.findViewById(R.id.pager);
		_detailsFlipper = (ViewFlipper) container.findViewById(R.id.details_flipper);

		// obtaining _detailsFlipper dimensions
		_textViewHeight = context.getResources().getDimensionPixelSize(R.dimen.details_area_height);
		_textViewWidth = context.getResources().getDimensionPixelSize(R.dimen.details_area_width);

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
				if (_summary != null)
					_summary.setText(program.getDetailAttribute(ProgramAttribute.SUMMARY));

				if (_detailsFlipper != null)
					fillProgramDescription(program.getDetailAttribute(ProgramAttribute.DESCRIPTION));
			}

			@Override
			public void onError(int resultCode)
			{
				Log.e(TAG, ".updateBrief: error loading program details for channel = " + channelId + ", program = "
				        + program.getId() + ", " + program.getTitle());

				_secondaryTitle.setText(null);

				if (_summary != null)
					_summary.setText(null);
				if (_detailsFlipper != null)
					fillProgramDescription(null);
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

		if (_summary != null)
			_summary.setText(null);

		if (_detailsFlipper != null)
			fillProgramDescription(null);

		// _pager.setText(String.format(_pagerTemplate, 1, 1));

		_featureEPG.getProgramDetails(channelId, program, new IOnProgramDetails()
		{
			@Override
			public void onProgramDetails(Program program)
			{
				_secondaryTitle.setText(program.getDetailAttribute(ProgramAttribute.SUBTITLE));

				if (_summary != null)
					_summary.setText(program.getDetailAttribute(ProgramAttribute.SUMMARY));

				if (_detailsFlipper != null)
					fillProgramDescription(program.getDetailAttribute(ProgramAttribute.DESCRIPTION));
			}

			@Override
			public void onError(int resultCode)
			{
				Log.e(TAG, ".updateDetails: error loading program details for channel = " + channelId + ", program = "
				        + program.getId() + ", " + program.getTitle());

				_secondaryTitle.setText(null);

				if (_summary != null)
					_summary.setText(null);

				if (_detailsFlipper != null)
					fillProgramDescription(null);
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

	protected void fillProgramDescription(String description)
	{
		_detailsFlipper.removeAllViews();
		while (description != null && description.length() != 0)
		{
			int textViewHeight = _textViewHeight;

			// creating new textviews for every page
			TextView textView = (TextView) LayoutInflater.from(_context).inflate(R.layout.program_summary_view, null);
			textView.setWidth(_textViewWidth);
			// textView.setLineSpacing(LINE_SPACING_ADD, LINE_SPACING_MULT);
			textView.setHeight(textViewHeight);

			StaticLayout layout = new StaticLayout(description, textView.getPaint(), _textViewWidth,
			        Alignment.ALIGN_NORMAL, LINE_SPACING_MULT, LINE_SPACING_ADD, true);

			int numChars = 0;
			int lineCount = layout.getLineCount();
			if (lineCount > 0)
			{
				// Since the line at the specific vertical position would be cut
				// off, we must trim up to the previous line
				int lastVisibleLine = layout.getLineForVertical(textViewHeight) - 1;
				if (lastVisibleLine > 0 && layout.getHeight() > textViewHeight)
				{
					numChars = layout.getLineEnd(lastVisibleLine);
				}
				else
				{
					numChars = description.length();
				}
			}

			// retrieve the String to be displayed in the current textView
			String pageText = description.substring(0, numChars);
			description = description.substring(numChars);

			textView.setText(pageText);
			_detailsFlipper.addView(textView);
		}

		// set first page visible
		_detailsFlipper.setDisplayedChild(0);

		showPageNumber();
	}

	public void showNextPage()
	{
		_detailsFlipper.showNext();
		showPageNumber();
	}

	public void showPrevPage()
	{
		_detailsFlipper.showPrevious();
		showPageNumber();
	}

	private void showPageNumber()
	{
		// show/hide nextPage icon and text
		if (_detailsFlipper.getChildCount() <= 1)
		{
			_pager.setText(null);
			_pager.setVisibility(View.INVISIBLE);
			return;
		}

		_pager.setText(String.format(_pagerTemplate, _detailsFlipper.getDisplayedChild() + 1,
		        _detailsFlipper.getChildCount()));
		_pager.setVisibility(View.VISIBLE);
	}
}
