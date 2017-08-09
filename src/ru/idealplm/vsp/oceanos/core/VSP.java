package ru.idealplm.vsp.oceanos.core;

import java.io.File;
import java.io.InputStream;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.plugin.Activator;

import ru.idealplm.vsp.oceanos.xml.XmlBuilder;
import ru.idealplm.vsp.oceanos.xml.XmlBuilderConfiguration;

public class VSP
{
	public static enum FormField {
		ID, NAME, PARENTID, QUANTITY, TOTALQUANTITY, REMARK
	};
	
	public static TCComponentBOMLine topBOMLine;
	public static TCComponentItem topBOMLineI;
	public static TCComponentItemRevision topBOMLineIR;
	public static TCComponentItemRevision vspIR;
	
	public static ErrorList errorList;

	public ProgressMonitorDialog progressMonitor;
	public StampData stampData;
	public Report report;
	
	public VSP()
	{
		stampData = new StampData();
		report = new Report();
		errorList = new ErrorList();
	}
	
	public void init()
	{
		try{
			topBOMLine = Activator.getPSEService().getTopBOMLine();
			topBOMLineI = topBOMLine.getItem();
			topBOMLineIR = topBOMLine.getItemRevision();
		} catch (TCException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Error while initializing");
		}
	}
	
	public void readData()
	{
		DataReader dataReader = new DataReader(this);
		dataReader.readData();
	}

	public void buildXmlFile()
	{
		XmlBuilderConfiguration A4xmlBuilderConfiguration = new XmlBuilderConfiguration(26, 32);
		A4xmlBuilderConfiguration.MaxWidthGlobalRemark = 474;

		XmlBuilder xmlBuilder = new XmlBuilder(A4xmlBuilderConfiguration);
		File data = xmlBuilder.buildXml();

		report.data = data;
	}

	public void buildReportFile()
	{
		InputStream template = VSP.class.getResourceAsStream("/pdf/OceanosVSPPDFTemplate.xsl");
		InputStream config = VSP.class.getResourceAsStream("/pdf/OceanosVSPUserconfig.xml");
		PDFBuilderConfiguration A3pdfBuilderconfiguration = new PDFBuilderConfiguration(template, config);

		report.configuration = A3pdfBuilderconfiguration;

		OceanosReportBuilder reportBuilder = new OceanosReportBuilder(report);
		reportBuilder.buildReportStatic();
	}
	
	public void uploadReportFile()
	{
		ReportUploader uploader = new ReportUploader();
		uploader.addToTeamcenter();
	}
}
