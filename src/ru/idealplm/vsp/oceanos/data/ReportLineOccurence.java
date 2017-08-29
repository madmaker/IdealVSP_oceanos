package ru.idealplm.vsp.oceanos.data;

import java.util.ArrayList;

import com.teamcenter.rac.kernel.TCComponentBOMLine;

import ru.idealplm.vsp.oceanos.util.LineUtil;

public class ReportLineOccurence
{
	public TCComponentBOMLine bomLine;
	public ReportLine reportLine;
	public ReportLineOccurence parentOccurence;
	public ArrayList<ReportLineOccurence> children;
	public int quantity = 1;
	public int quantityMult = 0;
	public int totalQuantity = 1;
	public String remark = "";
	public ArrayList<String> remarkLines;
	private int lineHeight = 1;
	
	public ReportLineOccurence(ReportLine reportLine, ReportLineOccurence parentOccurence)
	{
		this.parentOccurence = parentOccurence;
		this.reportLine = reportLine;
		this.children = new ArrayList<ReportLineOccurence>(1);
	}
	
	public ReportLineOccurence calcTotalQuantity()
	{
		if(quantityMult == 0) quantityMult = 1;
		totalQuantity = quantity * quantityMult;
		return this;
	}
	
	public int getTotalQuantity()
	{
		return totalQuantity;
	}
	
	public String getLineId()
	{
		return reportLine==null?"":reportLine.id;
	}
	
	public String getParentId()
	{
		return parentOccurence==null?"":parentOccurence.getLineId();
	}
	
	public void addChildOccurence(ReportLineOccurence child)
	{
		this.children.add(child);
	}
	
	public int calcLineHeight(double maxWidth)
	{
		remarkLines = new ArrayList<String>(1);
		remarkLines = LineUtil.getFittedLines(remark, maxWidth);
		lineHeight = remarkLines.size();
		if(lineHeight == 0)
		{
			lineHeight = 1;
			remarkLines.add(remark);
		}
		return lineHeight;
	}
	
	/*public void setQuantityMultiplier(int multiplier)
	{
		this.quantityMult = multiplier;
		for(ReportLineOccurence occurence:children)
		{
			occurence.updateQuantityMultiplier(quantityMult);
		}
	}
	
	public void updateQuantityMultiplier(int multiplier)
	{
		this.quantityMult += multiplier;
		for(ReportLineOccurence occurence:children)
		{
			occurence.updateQuantityMultiplier(quantityMult);
		}
	}*/
	
	public int getLineHeight()
	{
		return lineHeight;
	}
}
