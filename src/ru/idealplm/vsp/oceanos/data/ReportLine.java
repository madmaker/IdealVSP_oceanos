package ru.idealplm.vsp.oceanos.data;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportLine
{
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
		final ArrayList<ReportLineOccurence> lines = new ArrayList<ReportLineOccurence>(occurences.values());
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
