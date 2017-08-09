package ru.idealplm.vsp.oceanos.xml;

import java.util.HashMap;

import ru.idealplm.vsp.oceanos.core.VSP.FormField;

public class XmlBuilderConfiguration
{
	public int	MaxLinesOnFirstPage;
	public int	MaxLinesOnOtherPage;
	public int	MaxWidthGlobalRemark;
	public HashMap<FormField, Double> columnLengths;

	public XmlBuilderConfiguration()
	{
		MaxLinesOnFirstPage = 1;
		MaxLinesOnOtherPage = 1;
		MaxWidthGlobalRemark = 1;
		initColumnLengths();
	}

	public XmlBuilderConfiguration(int MaxLinesOnFirstPage, int MaxLinesOnOtherPage)
	{
		this();
		this.MaxLinesOnFirstPage = MaxLinesOnFirstPage;
		this.MaxLinesOnOtherPage = MaxLinesOnOtherPage;
	}
	
	public void initColumnLengths()
	{
		columnLengths = new HashMap<FormField, Double>();
		columnLengths.put(FormField.ID, 3d);
		columnLengths.put(FormField.NAME, 204.0d);
		columnLengths.put(FormField.PARENTID, 3d);
		columnLengths.put(FormField.QUANTITY, 3d);
		columnLengths.put(FormField.TOTALQUANTITY, 3d);
		columnLengths.put(FormField.REMARK, 88d);
	}
	
	public void setColumnLengths(HashMap<FormField, Double> columnLengths)
	{
		this.columnLengths = columnLengths;
	}
}
