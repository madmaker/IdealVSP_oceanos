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
	private ReportLineXMLRepresentation reportLineXMLRepresentation;

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
			addExtraData();
			processData();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			DOMSource source = new DOMSource(document);
			File xmlFile = File.createTempFile(report.stampData.id+"_", ".xml");
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
	
	private void addStampData()
	{
		node = document.createElement("Izdelie_osnovnai_nadpis");
		node.setAttribute("NAIMEN", report.stampData.name);
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
		node.setAttribute("CRTDATE", report.stampData.designDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.designDate));
		node.setAttribute("CHKDATE", report.stampData.checkDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.checkDate));
		node.setAttribute("TCHKDATE", report.stampData.techCheckDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.techCheckDate));
		node.setAttribute("CTRLDATE", report.stampData.normCheckDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.normCheck));
		node.setAttribute("APRDATE", report.stampData.approveDate.isEmpty()?"":DateUtil.parseDateFromTC(report.stampData.approveDate));
		
		node_root.appendChild(node);
	}
	
	private void addExtraData()
	{
		node = document.createElement("FileData");
		node.setAttribute("FileName", "Файл ведомости спецификаций: " + report.stampData.id+".pdf/" + report.stampData.reportRevNo);
		node_root.appendChild(node);
	}
	
	private void processData()
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
		ReportLineType previousLineType = ReportLineType.NONE;
		
		for(ReportLine line : report.linesList.getSortedList())
		{
			System.out.println("-line!");
			ReportLineXMLRepresentation reportLineXMLRepresentation = new ReportLineXMLRepresentation(line);
			int lineHeight = reportLineXMLRepresentation.getLineHeight();
			if(getFreeLinesNum() < 1 + lineHeight) newPage();
			ReportLineType currentLineType = line.type;
			
			// Adding empty line before Document lines
			if(previousLineType!=ReportLineType.NONE && previousLineType!=ReportLineType.DOCUMENT && currentLineType==ReportLineType.DOCUMENT)
			{
				addEmptyLines(1);
			}
			previousLineType = currentLineType;
			
			if(currentLineType==ReportLineType.ASSEMBLY || currentLineType==ReportLineType.KIT)
			{
				addAssyOrKitLine2(reportLineXMLRepresentation);
			} else 
			{
				addDocumentLine(reportLineXMLRepresentation);
			}
		}
		
		node_root.appendChild(node_block);
	}
	
	public void addAssyOrKitLine2(ReportLineXMLRepresentation line)
	{
		ReportLineOccurenceXmlRepresentation currentOccurence;
		int lineHeight = line.getLineHeight();
		int totalQuantity = 0;
		int occurencesHeight = 0;
		for(ReportLineOccurenceXmlRepresentation occurence:line.occurences) {
			System.out.println("incrementing with " + occurence.getLineHeight());
			occurencesHeight += occurence.getLineHeight();
			totalQuantity += occurence.occurence.totalQuantity;
		}
		int totalHeight = lineHeight>occurencesHeight?lineHeight:occurencesHeight;
		int currentOccurenceNumber = 0;
		int currentOccurenceRemarkLine = 0;
		
		for(int i = 0; i < totalHeight; i++)
		{
			node_occ = document.createElement("Occurrence");
			System.out.println(line.reportLine.name + " " + currentOccurenceNumber + " lh" + lineHeight + " os" + line.occurences.size() + " th" + totalHeight + " oh" + occurencesHeight);
			// If it is the first line, we print first line info and increment currentLine number
			if(i==0)
			{
				// Line number
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				currentLineNum++;
				// Id of the line
				node = document.createElement("Col_" + 2);
				node.setAttribute("align", "left");
				node.setTextContent(line.reportLine.id);
				node_occ.appendChild(node);
				// Name
				node = document.createElement("Col_" + 3);
				node.setAttribute("align", "left");
				node.setTextContent(line.nameLines.get(i));
				node_occ.appendChild(node);
			} else if (lineHeight>i && lineHeight <= totalHeight) {
				System.out.println("new line for name");
				// If line name takes multiple lines, we print it
				node = document.createElement("Col_" + 3);
				node.setAttribute("align", "left");
				node.setTextContent(line.nameLines.get(i));
				node_occ.appendChild(node);
			}
			// If there are still occurences left
			if(currentOccurenceNumber < line.occurences.size()){
				currentOccurence = line.occurences.get(currentOccurenceNumber);
				//Occurence first line info
				if(currentOccurenceRemarkLine==0)
				{
					//Parent id
					node = document.createElement("Col_" + 4);
					node.setAttribute("align", "left");
					node.setTextContent(currentOccurence.occurence.getParentItemId());
					node_occ.appendChild(node);
					// Quantity
					node = document.createElement("Col_" + 5);
					node.setAttribute("align", "center");
					node.setTextContent(String.valueOf(currentOccurence.occurence.quantity));
					node_occ.appendChild(node);
					// Total quantity
					node = document.createElement("Col_" + 6);
					node.setAttribute("align", "center");
					node.setTextContent(String.valueOf(currentOccurence.occurence.totalQuantity));
					node_occ.appendChild(node);
				}
				// Remark
				node = document.createElement("Col_" + 7);
				node.setAttribute("align", "left");
				node.setTextContent(currentOccurence.remarkLines.size()>0?currentOccurence.remarkLines.get(currentOccurenceRemarkLine):"");
				node_occ.appendChild(node);
				
				currentOccurenceRemarkLine++;
				if(currentOccurenceRemarkLine >= currentOccurence.getLineHeight())
				{
					currentOccurenceRemarkLine = 0;
					currentOccurenceNumber++;
				}
			}
			
			node_block.appendChild(node_occ);
			
			//If it is the last line
			if(i==(totalHeight-1) && line.occurences.size()>1)
			{
				System.out.println("YEAHHH!");
				node_occ = document.createElement("Occurrence");
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				
				node = document.createElement("Col_" + 6);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(totalQuantity));
				node_occ.appendChild(node);
				currentLineNum++;
				node_block.appendChild(node_occ);
			}
		}
	}
	
	public void addDocumentLine(ReportLineXMLRepresentation line)
	{
		int lineHeight = line.getLineHeight();
		//node_occ = document.createElement("Occurrence");
		for(int i = 0; i < lineHeight; i++)
		{
			node_occ = document.createElement("Occurrence");
			// If it is the first line, we print first line info and increment currentLine number
			if(i==0)
			{
				// Line number
				node = document.createElement("Col_" + 1);
				node.setAttribute("align", "center");
				node.setTextContent(String.valueOf(currentLineNum));
				node_occ.appendChild(node);
				currentLineNum++;
				// Name
				node = document.createElement("Col_" + 3);
				node.setAttribute("align", "left");
				node.setTextContent(line.nameLines.get(i));
				node_occ.appendChild(node);
			} else {
				System.out.println("new line for name");
				// If line name takes multiple lines, we print it
				node = document.createElement("Col_" + 3);
				node.setAttribute("align", "left");
				node.setTextContent(line.nameLines.get(i));
				node_occ.appendChild(node);
			}
			node_block.appendChild(node_occ);
		}
	}
	
	public void newPage()
	{
		addEmptyLines(getFreeLinesNum());
		node_block = document.createElement("Block");
		node_root.appendChild(node_block);
		currentPageNum += 1;
		addEmptyLines(1);
	}
	
	public void addBasicLine(BasicXmlLine line)
	{
		// Line number
		node = document.createElement("Col_" + 1);
		node.setAttribute("align", "center");
		node.setTextContent(String.valueOf(currentLineNum));
		node_occ.appendChild(node);
		currentLineNum++;
		// Id of the line
		node = document.createElement("Col_" + 2);
		node.setAttribute("align", "left");
		node.setTextContent(line.getAttribute(FormField.ID));
		node_occ.appendChild(node);
		// Name
		node = document.createElement("Col_" + 3);
		node.setAttribute("align", "left");
		node.setTextContent(line.getAttribute(FormField.NAME));
		node_occ.appendChild(node);
		// Parent Id
		node = document.createElement("Col_" + 4);
		node.setAttribute("align", "center");
		node.setTextContent(line.getAttribute(FormField.PARENTID));
		node_occ.appendChild(node);
		// Quantity
		node = document.createElement("Col_" + 5);
		node.setAttribute("align", "left");
		node.setTextContent(line.getAttribute(FormField.QUANTITY));
		node_occ.appendChild(node);
		// TotalQuantity
		node = document.createElement("Col_" + 6);
		node.setAttribute("align", "left");
		node.setTextContent(line.getAttribute(FormField.TOTALQUANTITY));
		node_occ.appendChild(node);
		// Remark
		node = document.createElement("Col_" + 7);
		node.setAttribute("align", "left");
		node.setTextContent(line.getAttribute(FormField.REMARK));
		node_occ.appendChild(node);
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
