package ru.idealplm.vsp.oceanos.data;

import java.util.ArrayList;

import com.teamcenter.rac.kernel.TCComponentBOMLine;

public class ReportLineOccurence
{
	public ReportLine reportLine;
	public TCComponentBOMLine bomLine;
	public int quantity = 1;
	public int totalQuantity = 1;
	public String remark = "";
	
	private ReportLineOccurence parent;
	private ArrayList<ReportLineOccurence> children;
	
	public ReportLineOccurence(ReportLine reportLine, ReportLineOccurence parentOccurence)
	{
		this.reportLine = reportLine;
		this.parent = parentOccurence;
		this.children = new ArrayList<ReportLineOccurence>(1);
	}
	
	public void addChild(ReportLineOccurence child)
	{
		children.add(child);
	}
	
	public int getChildrenCount()
	{
		return children.size();
	}
	
	public ReportLineOccurence getChild(int index)
	{
		return children.get(index);
	}
	
	public ArrayList<ReportLineOccurence> getChildren()
	{
		return children;
	}
	
	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
		this.totalQuantity = parent.totalQuantity * quantity;
	}
	
	public String getParentItemId()
	{
		if(parent!=null && parent.reportLine!=null)
			return parent.reportLine.id;
		return "";
	}
	
	public String getParentItemUID()
	{
		if(parent!=null && parent.reportLine!=null)
			return parent.reportLine.uid;
		return "";
	}
	
	@Override
	public boolean equals(Object v)
	{
		boolean retVal = false;

	    if (v instanceof ReportLineOccurence){
	    	ReportLineOccurence ptr = (ReportLineOccurence) v;
	        retVal = ptr.reportLine.uid.equals(this.reportLine.uid);
	    }
	    
	    return retVal;
	}
	
	@Override
    public int hashCode()
	{
        int hash = 7;
        hash = 17 * hash + (this.reportLine.uid != null ? this.reportLine.uid.hashCode() : 0);
        return hash;
    }
}
