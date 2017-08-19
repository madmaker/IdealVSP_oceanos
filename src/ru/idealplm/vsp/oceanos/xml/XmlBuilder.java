package ru.idealplm.vsp.oceanos.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.idealplm.vsp.oceanos.core.Report;
import ru.idealplm.vsp.oceanos.core.Report.FormField;
import ru.idealplm.vsp.oceanos.core.VSPSettings;
import ru.idealplm.vsp.oceanos.data.ReportLine;
import ru.idealplm.vsp.oceanos.data.ReportLine.ReportLineType;
import ru.idealplm.vsp.oceanos.data.ReportLineOccurence;
import ru.idealplm.vsp.oceanos.util.DateUtil;

public class XmlBuilder
{
	private XmlBuilderConfiguration configuration;
	
	private DocumentBuilderFactory documentBuilderFactory;
	private DocumentBuilder builder;
	private Document document;
	private Element node_root;
	private Element node;
	private Element node_block = null;
	private Element node_occ;
	
	private int currentLineNum = 1;
	private int currentPageNum = 1;
	
	private Report report;

	public XmlBuilder(XmlBuilderConfiguration configuration, Report report)
	{
		this.configuration = configuration;
		this.report = report;
	}

	public void setConfiguration(XmlBuilderConfiguration configuration)
	{
		this.configuration = configuration;
	}
	
	public File buildXml()
	{
		try{			
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			builder = documentBuilderFactory.newDocumentBuilder();
			document = builder.newDocument();
			node_root = document.createElement("root");
			document.appendChild(node_root);
			
			node = document.createElement("Settings");
			node.setAttribute("ShowAdditionalForm", VSPSettings.doShowAdditionalForm==true?"true":"false");
			node_root.appendChild(node);
			
			addStampData();
			processData();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(document);
			File xmlFile = File.createTempFile("vsp_", ".xml");
			StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);
			return xmlFile;
		} catch (TransformerConfigurationException ex) {
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void addStampData()
	{
		node = document.createElement("Izdelie_osnovnai_nadpis");
		node.setAttribute("NAIMEN", report.stampData.name + " ��������� ������������");
		node.setAttribute("OBOZNACH", report.stampData.id);
		node.setAttribute("PERVPRIM", report.stampData.pervPrim);
		node.setAttribute("LITERA1", report.stampData.litera1);
		node.setAttribute("LITERA2", report.stampData.litera2);
		node.setAttribute("LITERA3", report.stampData.litera3);
		node.setAttribute("INVNO", report.stampData.invNo);
		
		node.setAttribute("RAZR", report.stampData.design);
		node.setAttribute("PROV", report.stampData.check);
		node.setAttribute("ADDCHECKER", report.stampData.techCheck);
		node.setAttribute("NORM", report.stampData.normCheck);
		node.setAttribute("UTV", report.stampData.approve);
		report.stampData.print();
		node.setAttribute("CRTDATE", report.stampData.designDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.designDate));
		node.setAttribute("CHKDATE", report.stampData.checkDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.checkDate));
		node.setAttribute("TCHKDATE", report.stampData.techCheckDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.techCheckDate));
		node.setAttribute("CTRLDATE", report.stampData.normCheck.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.normCheck));
		node.setAttribute("APRDATE", report.stampData.approveDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.approveDate));
		
		node_root.appendChild(node);
	}
	
	public void processData()
	{
		if (node_block == null)
		{
			node_block = document.createElement("Block");
			node_root.appendChild(node_block);
		}
		if(getFreeLinesNum() < 1)
		{
			newPage();
		}
		
		for(ReportLine line : report.linesList.getSortedList())
		{
			int lineHeight = calcLineHeight(line);
			if(getFreeLinesNum() < 1 + lineHeight) newPage();
			
			if(line.type==ReportLineType.ASSEMBLY || line.type==ReportLineType.KIT)
			{
				addAssyOrKitLine(line);
			} else 
			{
				addDocumentLine(line);
			}
		}
		
		node_root.appendChild(node_block);
	}
	
	public void addAssyOrKitLine(ReportLine line)
	{
		ReportLineOccurence currentOccurence;
		int lineHeight = line.lineHeight;
		int occurencesHeight = 1;
		for(ReportLineOccurence occurence:line.occurences) occurencesHeight+=calcOccurenceHeight(occurence);
		int totalHeight = lineHeight>occurencesHeight?lineHeight:occurencesHeight;
		
		for(int i = 0; i < line.lineHeight; i++)
		{
			node_occ = document.createElement("Occurrence");
			if(i==0)
			{
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				
				node = document.createElement("Col_" + 2);
				node.setAttribute("align", "left");
				node.setTextContent(line.id);
				node_occ.appendChild(node);
				
				currentOccurence = line.occurences.get(0);
				int currentOccurenceHeight = calcOccurenceHeight(currentOccurence);
				
				if(!line.id.equals(report.targetId) && currentOccurence.getParentId().equals(report.targetId)){
					node = document.createElement("Col_" + 4);
					node.setAttribute("align", "left");
					node.setTextContent(currentOccurence.getParentId());
					node_occ.appendChild(node);
					
					node = document.createElement("Col_" + 5);
					node.setAttribute("align", "center");
					node.setTextContent(String.valueOf(currentOccurence.quantity));
					node_occ.appendChild(node);
				}
				
				node = document.createElement("Col_" + 6);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentOccurence.calcTotalQuantity().getTotalQuantity()));
				node_occ.appendChild(node);
				
				node = document.createElement("Col_" + 7);
				node.setAttribute("align", "left");
				node.setTextContent(currentOccurence.remark);
				node_occ.appendChild(node);
				
			}
			node = document.createElement("Col_" + 3);
			node.setAttribute("align", "left");
			node.setTextContent(line.nameLines.get(i));
			node_occ.appendChild(node);

			node_block.appendChild(node_occ);
		}
		currentLineNum++;

		if(line.occurences.size()>1){
			for(int i = 1; i < line.occurences.size(); i++)
			{
				node_occ = document.createElement("Occurrence");

				currentOccurence = line.occurences.get(i);
				
				node = document.createElement("Col_" + 4);
				node.setAttribute("align", "left");
				node.setTextContent(currentOccurence.getParentId());
				node_occ.appendChild(node);
				
				node = document.createElement("Col_" + 5);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentOccurence.quantity));
				node_occ.appendChild(node);
				
				node = document.createElement("Col_" + 6);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentOccurence.calcTotalQuantity().getTotalQuantity()));
				node_occ.appendChild(node);
				
				node = document.createElement("Col_" + 7);
				node.setAttribute("align", "left");
				node.setTextContent(currentOccurence.remark);
				node_occ.appendChild(node);
				
				node_block.appendChild(node_occ);
			}
			
			node_occ = document.createElement("Occurrence");
			
			node = document.createElement("Col_" + 1);
			node.setAttribute("align", "center");
			node.setTextContent(String.valueOf(currentLineNum));
			node_occ.appendChild(node);
			
			node = document.createElement("Col_" + 6);
			node.setAttribute("align", "center");
			line.calcTotalQuantity();
			node.setTextContent(String.valueOf(line.getTotalQuantity()));
			node_occ.appendChild(node);
			
			node_block.appendChild(node_occ);
			currentLineNum++;
		}
	}
	
	public void addDocumentLine(ReportLine line)
	{
		
	}
	
	public int calcLineHeight(ReportLine line)
	{
		return line.calcLineHeight(configuration.columnLengths.get(FormField.NAME));
	}
	
	public int calcOccurenceHeight(ReportLineOccurence occurence)
	{
		return occurence.calcLineHeight(configuration.columnLengths.get(FormField.REMARK));
	}
	
	public void newPage()
	{
		addEmptyLines(getFreeLinesNum());
		node_block = document.createElement("Block");
		node_root.appendChild(node_block);
		currentPageNum += 1;
		addEmptyLines(1);
	}
	
	public void addEmptyLines(int num)
	{
		for(int i = 0; i < num; i++){
			if(getFreeLinesNum() <= 0){
				newPage();
			}
			//currentLineNum++;
			node_occ = document.createElement("Occurrence");
			node_block.appendChild(node_occ);
		}
	}
	
	int getFreeLinesNum()
	{
		if(currentPageNum==1) return (configuration.MaxLinesOnFirstPage - currentLineNum + 1);
		return (configuration.MaxLinesOnOtherPage - currentLineNum + 1);
	}
}
