package ru.idealplm.vsp.oceanos.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import ru.idealplm.vsp.oceanos.core.ReportLinesComparator;

public class ReportLineList
{
	private HashMap<String, ReportLine> reportLines;
	private ReportLinesComparator comparator;
	
	public ReportLineList()
	{
		reportLines = new HashMap<String, ReportLine>();
		comparator = new ReportLinesComparator();
	}
	
	public boolean containsLineWithUid(String uid)
	{
		return reportLines.containsKey(uid);
	}
	
	public void addLine(ReportLine line)
	{
		reportLines.put(line.uid, line);
	}
	
	public ReportLine getLine(String uid)
	{
		return reportLines.get(uid);
	}
	
	public ArrayList<ReportLine> getSortedList()
	{
		ArrayList<ReportLine> lines = new ArrayList<ReportLine>(reportLines.values());
		Collections.sort(lines, comparator);
		return lines;
	}
}
