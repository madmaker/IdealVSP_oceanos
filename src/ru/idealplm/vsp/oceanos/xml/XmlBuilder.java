package ru.idealplm.vsp.oceanos.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
import ru.idealplm.vsp.oceanos.core.VSPSettings;

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

	public XmlBuilder(XmlBuilderConfiguration configuration)
	{
		this.configuration = configuration;
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
	
	public void processData()
	{
		if (node_block == null) {
			node_block = document.createElement("Block");
			node_root.appendChild(node_block);
			addEmptyLines(1);
		}
		node_root.appendChild(node_block);
	}
	
	public void newPage()
	{
		addEmptyLines(getFreeLinesNum());
		node_block = document.createElement("Block");
		node_root.appendChild(node_block);
		currentLineNum = 1;
		currentPageNum += 1;
		addEmptyLines(1);
	}
	
	public void addEmptyLines(int num)
	{
		for(int i = 0; i < num; i++){
			if(getFreeLinesNum() <= 0){
				newPage();
			}
			currentLineNum++;
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
