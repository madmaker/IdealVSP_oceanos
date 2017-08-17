package ru.idealplm.vsp.oceanos.core;

import java.io.File;

import ru.idealplm.vsp.oceanos.data.ReportLineList;

public class Report
{
	public enum ReportType {
		PDF, XLS
	};
	
	public static enum FormField {
		ID, NAME, PARENTID, QUANTITY, TOTALQUANTITY, REMARK
	};
	
	public ReportType type;
	public ReportConfiguration configuration;
	public ReportLineList linesList;
	public File data;
	public File report;
	
	public Report()
	{
		linesList = new ReportLineList();
	}
	
	public void isDataValid()
	{
		if(data == null)
			throw new RuntimeException("Report data is null");

	}
}