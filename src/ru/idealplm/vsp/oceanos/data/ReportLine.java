package ru.idealplm.vsp.oceanos.data;

import java.util.ArrayList;

import ru.idealplm.vsp.oceanos.util.LineUtil;

public class ReportLine
{
	public enum ReportLineType {
		NONE, ASSEMBLY, KIT, DOCUMENT
	};
	
	public String uid;
	public ReportLineType type;
	public String id;
	public String name;
	public ArrayList<String> nameLines;
	public ArrayList<ReportLineOccurence> occurences;
	public int lineHeight = 1;
	private int totalQuantity = 1;
	
	public ReportLine(ReportLineType type, String name)
	{
		this.type = type;
		this.name = name;
		this.occurences = new ArrayList<ReportLineOccurence>();
	}
	
	public int getTotalQuantity()
	{
		for(ReportLineOccurence occurence : occurences)
		{
			totalQuantity += occurence.getTotalQuantity();
		}
		
		return totalQuantity;
	}
	
	public int calcLineHeight(double maxWidth)
	{
		nameLines = new ArrayList<String>(1);
		nameLines = LineUtil.getFittedLines(name, maxWidth);
		lineHeight = nameLines.size();
		return lineHeight;
	}
}
