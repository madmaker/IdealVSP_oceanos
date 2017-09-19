package ru.idealplm.vsp.oceanos.tests;

import java.io.File;
import java.io.InputStream;

import org.eclipse.ui.internal.dialogs.EmptyPropertyPage;

import ru.idealplm.vsp.oceanos.core.Report;
import ru.idealplm.vsp.oceanos.core.Report.ReportType;
import ru.idealplm.vsp.oceanos.data.ReportLineOccurence;
import ru.idealplm.vsp.oceanos.excel.ExcelReportBuilder;
import ru.idealplm.vsp.oceanos.xml.PDFReportBuilder;
import ru.idealplm.vsp.oceanos.xml.PDFReportBuilderConfiguration;
import ru.idealplm.vsp.oceanos.xml.XmlBuilder;
import ru.idealplm.vsp.oceanos.xml.XmlBuilderConfiguration;
import ru.idealplm.vsp.oceanos.data.ReportLine;
import ru.idealplm.vsp.oceanos.data.ReportLine.ReportLineType;
import ru.idealplm.vsp.oceanos.data.ReportLineList;

public class XmlBuilderTest
{
	Report report;
	ReportLineList list;
	ReportLineOccurence sborka1o1;
	
	public XmlBuilderTest()
	{
		report = new Report();
		list = new ReportLineList();
		report.linesList = list;
		report.type = ReportType.PDF;
		report.targetId = "AAA.000.001";
		report.stampData.id = report.targetId;
	}
	
	public static void main(String argc[])
	{
		XmlBuilderTest test = new XmlBuilderTest();
		test.prepareData();
		test.writeReport();
	}
	
	public void prepareData()
	{
		System.out.println("preparing data...");
		ReportLine sborka1 = new ReportLine(ReportLineType.ASSEMBLY, "Сборка1");
		ReportLine sborka2 = new ReportLine(ReportLineType.ASSEMBLY, "Сболка2");
		ReportLine sborka3 = new ReportLine(ReportLineType.ASSEMBLY, "Сборка3");
		ReportLine sborka4 = new ReportLine(ReportLineType.ASSEMBLY, "Сборка4");
		ReportLine sborka5 = new ReportLine(ReportLineType.ASSEMBLY, "Сборка5");
		ReportLine kit1 = new ReportLine(ReportLineType.KIT, "Комплект1");
		
		ReportLine emptyLine = new ReportLine(ReportLineType.NONE, "");
		ReportLineOccurence emptyOccurence = new ReportLineOccurence(emptyLine, null);
		sborka1o1 = new ReportLineOccurence(sborka1, emptyOccurence);
		sborka1.addOccurence(sborka1o1);
		
		ReportLineOccurence sborka2o1 = new ReportLineOccurence(sborka2, sborka1o1);
		sborka2.addOccurence(sborka2o1);
		
		ReportLineOccurence sborka3o1 = new ReportLineOccurence(sborka3, sborka1o1);
		sborka3.addOccurence(sborka3o1);
		
		ReportLineOccurence sborka4o1 = new ReportLineOccurence(sborka4, sborka1o1);
		sborka4.addOccurence(sborka4o1);
		
		ReportLineOccurence sborka2o2 = new ReportLineOccurence(sborka2, sborka4o1);
		sborka2.addOccurence(sborka2o2);
		
		ReportLineOccurence kit1o1 = new ReportLineOccurence(kit1, sborka1o1);
		kit1.addOccurence(kit1o1);
		
		sborka1.id = "ААА.000.001";
		sborka1.uid = "1";
		sborka2.id = "ААА.000.002";
		sborka2.uid = "2";
		sborka3.id = "ААА.000.003";
		sborka3.uid = "3";
		sborka4.id = "ААА.000.004";
		sborka4.uid = "4";
		kit1.id = "ККК.000.001";
		kit1.uid = "5";
		
		sborka1o1.quantity = 1;
		sborka2o1.setQuantity(2);
		sborka4o1.setQuantity(2);
		sborka2o2.setQuantity(2);
		sborka2o1.remark="1";
		
		list.addLine(sborka1);
		list.addLine(sborka2);
		list.addLine(sborka3);
		list.addLine(sborka4);
		list.addLine(kit1);
		
		prepareDocumentData();
	}
	
	public void prepareDocumentData()
	{
		ReportLine document10 = new ReportLine(ReportLineType.DOCUMENT, "Документ1\n1 ВС");
		ReportLine document11 = new ReportLine(ReportLineType.DOCUMENT, "Документ2\n2 ВС");
		
		ReportLineOccurence document1o1 = new ReportLineOccurence(document10, sborka1o1);
		document10.addOccurence(document1o1);
		ReportLineOccurence document2o2 = new ReportLineOccurence(document11, sborka1o1);
		document11.addOccurence(document2o2);
		
		document10.id = "ДДД.000.010";
		document10.uid = "10";
		document11.id = "ДДД.000.011";
		document11.uid = "11";
		
		list.addLine(document10);
		list.addLine(document11);
	}
	
	public void writeReport()
	{
		System.out.println("writing data...");
		if(report.type == ReportType.PDF)
		{
			XmlBuilderConfiguration A4xmlBuilderConfiguration = new XmlBuilderConfiguration(26, 32);
			A4xmlBuilderConfiguration.MaxWidthGlobalRemark = 474;

			XmlBuilder xmlBuilder = new XmlBuilder(A4xmlBuilderConfiguration, report);
			File data = xmlBuilder.buildXml();
			report.data = data;
			
			InputStream template = XmlBuilderTest.class.getResourceAsStream("/OceanosVSPPDFTemplate.xsl");
			InputStream config = XmlBuilderTest.class.getResourceAsStream("/OceanosVSPUserconfig.xml");
			PDFReportBuilderConfiguration A3pdfBuilderconfiguration = new PDFReportBuilderConfiguration(template, config);
	
			report.configuration = A3pdfBuilderconfiguration;
	
			PDFReportBuilder reportBuilder = new PDFReportBuilder(report);
			reportBuilder.buildReportStatic();
		} else {
			ExcelReportBuilder reportBuilder = new ExcelReportBuilder(report);
		}
	}
}
