package ru.idealplm.vsp.oceanos.core;

import java.io.File;

public class Report
{
	public File data;
	public File report;
	public ReportConfiguration configuration;
	
	public Report()
	{
		
	}
	
	public void isDataValid()
	{
		if(data == null)
			throw new RuntimeException("Report data is null");

	}
}