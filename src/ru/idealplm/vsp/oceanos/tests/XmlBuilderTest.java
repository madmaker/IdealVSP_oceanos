package ru.idealplm.vsp.oceanos.tests;

import java.io.File;
import java.io.InputStream;

import ru.idealplm.vsp.oceanos.core.Report;
import ru.idealplm.vsp.oceanos.core.Report.ReportType;
import ru.idealplm.vsp.oceanos.data.ReportLine;
import ru.idealplm.vsp.oceanos.data.ReportLine.ReportLineType;
import ru.idealplm.vsp.oceanos.excel.ExcelReportBuilder;
import ru.idealplm.vsp.oceanos.xml.PDFReportBuilder;
import ru.idealplm.vsp.oceanos.xml.PDFReportBuilderConfiguration;
import ru.idealplm.vsp.oceanos.xml.XmlBuilder;
import ru.idealplm.vsp.oceanos.xml.XmlBuilderConfiguration;
import ru.idealplm.vsp.oceanos.data.ReportLineList;
import ru.idealplm.vsp.oceanos.data.ReportLineOccurence;

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
		report.targetId = "РРР.000.001";
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
		ReportLine sborka1 = new ReportLine(ReportLineType.ASSEMBLY, "бсю№ър1");
		ReportLine sborka2 = new ReportLine(ReportLineType.ASSEMBLY, "бсю№ър2");
		ReportLine sborka3 = new ReportLine(ReportLineType.ASSEMBLY, "бсю№ър3");
		ReportLine sborka4 = new ReportLine(ReportLineType.ASSEMBLY, "бсю№ър4");
		ReportLine kit1 = new ReportLine(ReportLineType.KIT, "Ъюьяыхъђ1");
		
		ReportLineOccurence emptyOccurence = new ReportLineOccurence(null, null);
		sborka1o1 = new ReportLineOccurence(sborka1, emptyOccurence);
		ReportLineOccurence sborka2o1 = new ReportLineOccurence(sborka2, sborka1o1);
		ReportLineOccurence sborka3o1 = new ReportLineOccurence(sborka3, sborka1o1);
		ReportLineOccurence sborka4o1 = new ReportLineOccurence(sborka4, sborka1o1);
		ReportLineOccurence sborka2o2 = new ReportLineOccurence(sborka2, sborka4o1);
		ReportLineOccurence kit1o1 = new ReportLineOccurence(kit1, sborka1o1);
		
		sborka1.id = "РРР.000.001";
		sborka1.occurences.add(sborka1o1);
		sborka1.uid = "1";
		sborka2.id = "РРР.000.002";
		sborka2.occurences.add(sborka2o1);
		sborka2.occurences.add(sborka2o2);
		sborka2.uid = "2";
		sborka3.id = "РРР.000.003";
		sborka3.occurences.add(sborka3o1);
		sborka3.uid = "3";
		sborka4.id = "РРР.000.004";
		sborka4.occurences.add(sborka4o1);
		sborka4.uid = "4";
		kit1.id = "KKK.000.001";
		kit1.occurences.add(kit1o1);
		kit1.uid = "5";
		
		sborka2o1.quantity = 2;
		sborka2o1.quantityMult = sborka1o1.getTotalQuantity();
		sborka4o1.quantity = 2;
		sborka4o1.quantityMult = sborka1o1.getTotalQuantity();
		sborka2o2.quantity = 2;
		sborka2o2.quantityMult = sborka4o1.getTotalQuantity();
		sborka2o1.remark="1";
		
		list.addLine(sborka1);
		list.addLine(sborka2);
		list.addLine(sborka3);
		list.addLine(sborka4);
		list.addLine(kit1);
		
		prepareLongData();
		prepareDocumentData();
	}
	
	public void prepareLongData()
	{
		ReportLine sborka8 = new ReportLine(ReportLineType.ASSEMBLY, "бсю№ър8");
		ReportLine sborka9 = new ReportLine(ReportLineType.ASSEMBLY, "ииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииии");
		
		ReportLineOccurence sborka8o1 = new ReportLineOccurence(sborka8, sborka1o1);
		ReportLineOccurence sborka9o1 = new ReportLineOccurence(sborka9, sborka1o1);
		ReportLineOccurence sborka9o2 = new ReportLineOccurence(sborka9, sborka8o1);
		
		//sborka9o1.remark = "ииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииии";
		sborka9o2.remark = "ииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииииии";
		
		sborka8.id = "РРР.000.008";
		sborka8.occurences.add(sborka8o1);
		sborka8.uid = "8";
		sborka9.id = "РРР.000.009";
		sborka9.occurences.add(sborka9o1);
		sborka9.occurences.add(sborka9o2);
		sborka9.uid = "9";
		
		list.addLine(sborka8);
		list.addLine(sborka9);
	}
	
	public void prepareDocumentData()
	{
		ReportLine document10 = new ReportLine(ReportLineType.DOCUMENT, "Тхфюьюёђќ ёяхішєшърішщ\nФюъѓьхэђ10 Тб");
		ReportLine document11 = new ReportLine(ReportLineType.DOCUMENT, "Тхфюьюёђќ ёяхішєшърішщ\nФюъѓьхэђ11 Тб");
		
		ReportLineOccurence document1o1 = new ReportLineOccurence(document10, sborka1o1);
		ReportLineOccurence document2o2 = new ReportLineOccurence(document11, sborka1o1);
		
		document10.id = "РРР.000.010";
		document10.occurences.add(document1o1);
		document10.uid = "10";
		document11.id = "РРР.000.011";
		document11.occurences.add(document2o2);
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
