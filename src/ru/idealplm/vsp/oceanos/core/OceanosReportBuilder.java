package ru.idealplm.vsp.oceanos.core;

import java.io.File;
import java.io.IOException;

import ru.idealplm.vsp.oceanos.util.FileUtil;
import ru.idealplm.xml2pdf2.handlers.PDFBuilder;

public class OceanosReportBuilder
{
	private PDFBuilder pdfBuilder;
	private PDFBuilderConfiguration pdfConfiguration;
	private Report report;
	
	public OceanosReportBuilder(Report report)
	{
		this.report = report;
		this.pdfConfiguration = (PDFBuilderConfiguration)report.configuration;
	}
	
	public void buildReport()
	{
		report.isDataValid();
		try
		{
			FileUtil.copy(OceanosReportBuilder.class.getResourceAsStream("/icons/iconOceanos.jpg"),
					new File(report.data.getParentFile().getAbsolutePath() + "\\iconOceanos.jpg"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try {
			pdfBuilder = new PDFBuilder(pdfConfiguration.getTemplateStream(), pdfConfiguration.getConfigStream());
		} catch (Exception e1) {
			e1.printStackTrace();
			VSP.errorList.storeError(new Error("Can't initialize PDFBuilder."));
			return;
		}
		pdfBuilder.passSourceFile(report.data, this);
		synchronized (this)
		{
			try
			{
				this.wait();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		report.report = pdfBuilder.getReport();
	}

	public void buildReportStatic()
	{
		report.isDataValid();
		try
		{
			FileUtil.copy(OceanosReportBuilder.class.getResourceAsStream("/icons/iconOceanos.jpg"),
					new File(report.data.getParentFile().getAbsolutePath() + "\\iconOceanos.jpg"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		report.report = PDFBuilder.xml2pdf(report.data, ((PDFBuilderConfiguration)report.configuration).getTemplateStream(), ((PDFBuilderConfiguration)report.configuration).getConfigStream());
	}
}
