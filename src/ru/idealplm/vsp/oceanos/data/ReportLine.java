package ru.idealplm.vsp.oceanos.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import ru.idealplm.vsp.oceanos.core.ReportLineOccurencesComparator;

public class ReportLine
{
	private static ReportLineOccurencesComparator reportLineOccurencessComparator = new ReportLineOccurencesComparator();
	
	public enum ReportLineType {
		NONE, ASSEMBLY, KIT, DOCUMENT
	};
	
	public ReportLineType type;
	public String uid;
	public String id;
	public String name;
	private int totalQuantity = 0;
	
	private HashMap<String, ReportLineOccurence> occurences;
	
	public ReportLine(ReportLineType type, String name)
	{
		this.type = type;
		this.name = name;
		this.occurences = new HashMap<String, ReportLineOccurence>(1);
	}
	
	public final ArrayList<ReportLineOccurence> occurences()
	{
		ArrayList<ReportLineOccurence> lines = new ArrayList<ReportLineOccurence>(occurences.values());
		Collections.sort(lines, reportLineOccurencessComparator);
		return lines;
	}
	
	public void addOccurence(ReportLineOccurence occurence)
	{
		occurence.reportLine = this;
		occurences.put(occurence.getParentItemUID(), occurence);
	}
	
	public void updateOccurence(ReportLineOccurence occurence)
	{
		if(occurences.containsKey(occurence.getParentItemUID()))
		{
			ReportLineOccurence existingOccurence = occurences.get(occurence.getParentItemUID());
			System.out.println("Existing:"+existingOccurence.totalQuantity+" + new:"+occurence.totalQuantity);
			existingOccurence.totalQuantity += occurence.totalQuantity;
		} 
		else {
			addOccurence(occurence);
		}
	}
	
	public int getTotalQuantity()
	{
		calcTotalQuantity(); 
		return totalQuantity;
	}
	
	private void calcTotalQuantity()
	{
		for(ReportLineOccurence occurence : occurences.values())
		{
			totalQuantity += occurence.totalQuantity;
		}
	}
}
