package com.aviq.tv.android.home.feature.epg;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import android.graphics.Bitmap;

public class EpgData
{
	private List<Channel> _channelList;
	private Bitmap[] _channelLogos;

	/** key = start time; value = index in program list for a specific channel */
	private Map<String, NavigableMap<String, Integer>> _channelToProgramNavigableMap = new HashMap<String, NavigableMap<String, Integer>>();

	/** key = channel id; value = program list for the specific channel */
	private Map<String, List<Program>> _channelToProgramListMap = new LinkedHashMap<String, List<Program>>();

	public static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

	public EpgData(List<Channel> newChannelList)
	{
		_channelList = newChannelList;
		_channelLogos = new Bitmap[_channelList.size()];
	}

	synchronized boolean setChannelLogo(int channelIndex, Bitmap newLogo)
	{
		if (newLogo == null || channelIndex < 0 || channelIndex > _channelList.size())
			return false;

		_channelLogos[channelIndex] = newLogo;

		return true;
	}

	synchronized boolean addProgramNavigableMap(String channelId, NavigableMap<String, Integer> newProgramNavigableMap)
	{
		if (newProgramNavigableMap == null || newProgramNavigableMap.size() == 0)
			return false;

		_channelToProgramNavigableMap.put(channelId, newProgramNavigableMap);

		return true;
	}

	synchronized boolean addProgramList(String channelId, List<Program> newProgramList)
	{
		if (newProgramList == null || newProgramList.size() == 0)
			return false;

		_channelToProgramListMap.put(channelId, newProgramList);

		return true;
	}

	/**
	 * @param index
	 *            Channel position in the list
	 * @return the Channel at location 'index' in the channel list
	 */
	public Channel getChannel(int index)
	{
		return _channelList.get(index);
	}

	/**
	 * @return the number of channels
	 */
	public int getChannelCount()
	{
		return _channelList.size();
	}

	public Bitmap getChannelLogoBitmap(int index)
	{
		return _channelLogos[index];
	}

	public int getProgramIndex(String channelId, Calendar when)
	{
		NavigableMap<String, Integer> map = _channelToProgramNavigableMap.get(channelId);
		if (map == null)
			return -1;

		String dateTime = DATE_TIME_FORMAT.format(when.getTime());
		Map.Entry<String, Integer> programEntry = map.floorEntry(dateTime);
		if (programEntry == null)
			return -1;

		return programEntry.getValue();
	}

	public Program getProgramByIndex(String channelId, int programIndex)
	{
		List<Program> programsList = _channelToProgramListMap.get(channelId);
		if (programsList == null)
			return null;

		if (programIndex < 0 || programIndex >= programsList.size())
			return null;

		return programsList.get(programIndex);
	}

	public Program getProgram(String channelId, Calendar when)
	{
		return getProgramByIndex(channelId, getProgramIndex(channelId, when));
	}
}
