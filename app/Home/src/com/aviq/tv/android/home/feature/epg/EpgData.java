package com.aviq.tv.android.home.feature.epg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import android.graphics.Bitmap;
import android.util.Log;

public class EpgData implements IEpgDataProvider
{
	private static final String TAG = EpgData.class.getSimpleName();
	
	private List<Channel> _channelList;
	private Bitmap[] _channelLogos;
	private Calendar _maxEpgStartTime;
	private Calendar _minEpgStartTime;

	/** key = start time; value = index in program list for a specific channel */
	private Map<String, NavigableMap<String, Integer>> _channelToProgramNavigableMap = new HashMap<String, NavigableMap<String, Integer>>();
	
	/** key = channel id; value = program list for the specific channel */
	private Map<String, List<Program>> _channelToProgramListMap = new LinkedHashMap<String, List<Program>>();
	
	public EpgData(List<Channel> newChannelList)
	{
		_maxEpgStartTime = Calendar.getInstance();
		_minEpgStartTime = Calendar.getInstance();
		
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
		
		// Keep EPG program max start time
		
		String lastStartTime = newProgramNavigableMap.lastKey();
		Calendar lastStartTimeCal = Program.getEpgTime(lastStartTime);

		if (_maxEpgStartTime.before(lastStartTimeCal))
			_maxEpgStartTime = lastStartTimeCal;
		
		// Keep EPG program min start time
		
		String firstStartTime = newProgramNavigableMap.firstKey();
		Calendar firstStartTimeCal = Program.getEpgTime(firstStartTime);
		
		if (_minEpgStartTime.before(firstStartTimeCal))
			_minEpgStartTime = firstStartTimeCal;
		
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
	 * @param index Channel position in the list
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
	
	public int getChannelIndex(Channel channel)
	{
		return _channelList.indexOf(channel);
	}
	
	public List<Program> getProgramList(String channelId, String startTime, String endTime)
	{
		if (startTime.compareTo(endTime) > 0)
		{
			Log.w(TAG, ".getProgramList: startTime > endTime: " + startTime + " > " + endTime + ", ignoring method call");
			return new ArrayList<Program>();
		}
		
		NavigableMap<String, Integer> programMap = _channelToProgramNavigableMap.get(channelId);
		NavigableMap<String, Integer> subMap = programMap != null ? programMap.subMap(startTime, true, endTime, false) : null;
		if (subMap == null)
		{
			Log.w(TAG, ".getProgramList: no EPG data for period: startTime = " + startTime + ", endTime " + endTime);
			return new ArrayList<Program>();
		}

		List<Program> list = new ArrayList<Program>(subMap.size());
		for (Map.Entry<String, Integer> entry : subMap.entrySet())
		{
			int index = entry.getValue();
			list.add(_channelToProgramListMap.get(channelId).get(index));
		}
		return list;
	}

	public int getProgramIndex(String channelId, Calendar when)
	{
		String dateTime = Program.getEpgTime(when);
		NavigableMap<String, Integer> programMap = _channelToProgramNavigableMap.get(channelId);
		if (programMap == null)
			return -1;
		
		Map.Entry<String, Integer> programEntry = programMap.floorEntry(dateTime);
		if (programEntry != null)
		{
			int programIndex = programEntry.getValue();
			return programIndex;
		}
		return -1;
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

	@Override
	public Program getProgram(String channelId, Calendar when)
	{
		return getProgramByIndex(channelId, getProgramIndex(channelId, when));
	}
	
	@Override
    public Program getProgram(String channelId, String dateTime)
    {
		Calendar when = Program.getEpgTime(dateTime);
		return getProgramByIndex(channelId, getProgramIndex(channelId, when));
    }
	
	public Calendar getMaxEpgStartTime()
	{
		return _maxEpgStartTime;
	}

	public Calendar getMinEpgStartTime()
	{
		return _minEpgStartTime;
	}
}
