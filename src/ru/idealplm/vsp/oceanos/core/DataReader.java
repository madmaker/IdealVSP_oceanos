package ru.idealplm.vsp.oceanos.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.cad.StructureManagementService;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSData;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelInfo;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelOutput;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelPref;
import com.teamcenter.services.rac.cad._2007_01.StructureManagement.ExpandPSOneLevelResponse;

import ru.idealplm.vsp.oceanos.data.ReportLineOccurence;
import ru.idealplm.vsp.oceanos.data.ReportLine;
import ru.idealplm.vsp.oceanos.data.ReportLine.ReportLineType;
import ru.idealplm.vsp.oceanos.data.ReportLineList;
import ru.idealplm.vsp.oceanos.handlers.VSPHandler;
import ru.idealplm.vsp.oceanos.util.DateUtil;

public class DataReader
{
	private VSP vsp;
	private StampData stampData;
	private ReportLineList lineList;
	private StructureManagementService smsService = StructureManagementService.getService(VSPHandler.session);
	private String blPropertyNames[] = {"bl_item_object_type", "bl_Part_oc9_TypeOfPart", "bl_quantity", "bl_item_item_id", "Oc9_Note"};
	private String blPropertyValues[];
	private TCComponent document;
	private ReportLine emptyLine;
	private ReportLineOccurence emptyOccurence;
	private ReportLineOccurence topOccurence;
	
	public DataReader(VSP vsp)
	{
		this.vsp = vsp;
		this.stampData = vsp.report.stampData;
		lineList = vsp.report.linesList;
		emptyLine = new ReportLine(ReportLineType.NONE, "");
		emptyOccurence = new ReportLineOccurence(emptyLine, null);
		emptyLine.addOccurence(emptyOccurence);
		topOccurence = readBomLineData(VSP.topBOMLine, emptyOccurence);
	}
	
	public void readExistingData()
	{
		findExistingVSP();
		readGeneralNoteForm();
		readExistingVSPData();
		readSpecifiedItemData();
	}
	
	public void readData()
	{
		readBomData(VSP.topBOMLine, topOccurence, emptyOccurence);
	}

	private void readBomData(TCComponentBOMLine parentBomLine, ReportLineOccurence currentOccurence, ReportLineOccurence parentOccurence)
	{
		ReportLineOccurence tempOccurence;
		if(currentOccurence==null) return;

		for (TCComponentBOMLine bomLine : getChildBOMLines(parentBomLine))
		{
			tempOccurence = readBomLineData(bomLine, currentOccurence);
			if(tempOccurence!=null)
				currentOccurence.addChild(tempOccurence);
		}
		for(ReportLineOccurence child : currentOccurence.getChildren())
		{
			readBomData(child.bomLine, child, currentOccurence);
		}
	}
	
	public ReportLineOccurence readBomLineData(TCComponentBOMLine bomLine, ReportLineOccurence parentOccurence)
	{
		ReportLineOccurence resultOccurence = null;
		try
		{
			blPropertyValues = bomLine.getProperties(blPropertyNames);
			boolean hasValidType = hasValidType(blPropertyValues[0], blPropertyValues[1]);
			if(hasValidType)
			{
				resultOccurence = processLine(bomLine, parentOccurence);
			}
		} catch (TCException ex)
		{
			ex.printStackTrace();
		}
		return resultOccurence;
	}
	
	public ReportLineOccurence processLine(TCComponentBOMLine bomLine, ReportLineOccurence parentOccurence)
	{
		ReportLineOccurence resultOccurence = null;
		try{
			document = getRelatedDocument(bomLine.getItemRevision());
			if(document!=null)
			{
				System.out.println("processing doc="+document.getUid());
				if(lineList.containsLineWithUid(document.getUid())){
					resultOccurence = updateExistingLine(bomLine, parentOccurence);
				} else {
					resultOccurence = addNewLine(bomLine, parentOccurence);
				}
			}
			else 
			{
				VSP.errorList.storeError(new Error(bomLine.getProperty("bl_item_item_id")));
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
		return resultOccurence;
	}
	
	public ReportLineOccurence addNewLine(TCComponentBOMLine bomLine, ReportLineOccurence parentOccurence)
	{
		ReportLineOccurence resultOccurence = null;
		try{
			int quantity = blPropertyValues[2].trim().isEmpty()?1:Integer.parseInt(blPropertyValues[2]);
			ReportLine line = new ReportLine(getTypeOfLine(), document.getProperty("object_name"));
			line.uid = document.getUid();
			line.id = document.getProperty("item_id");
			if(line.type==ReportLineType.DOCUMENT) line.name = "Ведомость спецификаций\n"+line.name;
			System.out.println("new line for "+line.name);
			resultOccurence = new ReportLineOccurence(line, parentOccurence);
			resultOccurence.setQuantity(quantity);
			resultOccurence.bomLine = bomLine;
			resultOccurence.remark = blPropertyValues[4];
			line.addOccurence(resultOccurence);
			lineList.addLine(line);
		} catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		return resultOccurence;
	}
	
	public ReportLineOccurence updateExistingLine(TCComponentBOMLine bomLine, ReportLineOccurence parentOccurence)
	{
		ReportLineOccurence resultOccurence = null;
		int quantity = blPropertyValues[2].trim().isEmpty()?1:Integer.parseInt(blPropertyValues[2]);
		ReportLine line = lineList.getLine(document.getUid());
		resultOccurence = new ReportLineOccurence(line, parentOccurence);
		resultOccurence.setQuantity(quantity);
		resultOccurence.bomLine = bomLine;
		resultOccurence.remark = blPropertyValues[4];
		line.updateOccurence(resultOccurence);
		return resultOccurence;
	}
	
	
	private TCComponentBOMLine[] getChildBOMLines(TCComponentBOMLine parent)
	{
		TCComponentBOMLine[] childLines = null;
		
		ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
		ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();

		levelInfo.parentBomLines = new TCComponentBOMLine[] { parent };
		levelInfo.excludeFilter = "None";
		levelPref.expItemRev = true;

		ExpandPSOneLevelResponse levelResp = smsService.expandPSOneLevel(levelInfo, levelPref);

		if (levelResp.output.length > 0)
		{
			for (ExpandPSOneLevelOutput levelOut : levelResp.output)
			{
				childLines = new TCComponentBOMLine[levelOut.children.length];
				for (int i=0; i<levelOut.children.length; i++)
				{
					childLines[i] = levelOut.children[i].bomLine;
				}
			}
		}
		
		if(childLines==null) childLines = new TCComponentBOMLine[0];
		
		return childLines;
	}
	
	public boolean hasValidType(String itemType, String partType)
	{
		if(!itemType.equals("Изделие предприятия") || (!partType.equals("Сборочная единица") && !partType.equals("Комплект")))
			return false;
		return true;
	}
	
	public ReportLineType getTypeOfLine()
	{
		ReportLineType type = ReportLineType.NONE;
		try
		{
			String documentId = document.getProperty("item_id");
			String revId = blPropertyValues[3];
			if(documentId.equals(revId + " ВС") && !revId.equals(vsp.report.targetId))
			{
				type = ReportLineType.DOCUMENT;
			} else if (blPropertyValues[1].equals("Сборочная единица"))
			{
				type = ReportLineType.ASSEMBLY;
			} else if (blPropertyValues[1].equals("Комплект"))
			{
				type = ReportLineType.KIT;
			}
		} catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		return type;
	}
	
	public TCComponent getRelatedDocument(TCComponentItemRevision rev)
	{
		try{
			String id;
			TCComponent result  = null;
			TCComponent[] documents = rev.getRelatedComponents("Oc9_DocRel");
			for(TCComponent document : documents)
			{
				id = document.getProperty("item_id");
				if(id.equals(rev.getProperty("item_id") + " ВС") && !rev.getProperty("item_id").equals(vsp.report.targetId)) {
					return document;
				} else if (rev.getProperty("item_id").equals(id)) {
					result = document;
				}
			}
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void findExistingVSP()
	{
		try{
			System.out.println("Looking for vsp!");
			TCComponent[] documents = VSP.topBOMLineIR.getRelatedComponents("Oc9_DocRel");
			String IRid = VSP.topBOMLineI.getProperty("item_id");
			String id;
			for(TCComponent document : documents){
				id = document.getProperty("item_id");
				System.out.println("Comparing " + id + " and " + IRid);
				if(id.equals(IRid + " ВС")){
					System.out.println("Found one");
					VSP.vspIR = ((TCComponentItem)document).getLatestItemRevision();
					VSP.generalNoteForm = VSP.vspIR.getRelatedComponent("Oc9_SignRel");
					return;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void readExistingVSPData()
	{
		try {
			if(VSP.vspIR != null){
				stampData.litera1 = VSP.vspIR.getProperty("oc9_Litera1");
				stampData.litera2 = VSP.vspIR.getProperty("oc9_Litera2");
				stampData.litera3 = VSP.vspIR.getProperty("oc9_Litera3");
				stampData.pervPrim = VSP.vspIR.getItem().getProperty("oc9_PrimaryApp");
				stampData.invNo = VSP.vspIR.getItem().getProperty("oc9_InvNo");
				stampData.reportRevNo = VSP.vspIR.getProperty("item_revision_id");
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
	}
	
	public void readGeneralNoteForm()
	{
		try {
			if(VSP.generalNoteForm != null)
			{
				stampData.design =  VSP.generalNoteForm.getProperty("oc9_Designer");
				stampData.check = VSP.generalNoteForm.getProperty("oc9_Check");
				stampData.techCheck = VSP.generalNoteForm.getProperty("oc9_TCheck");
				stampData.normCheck = VSP.generalNoteForm.getProperty("oc9_NCheck");
				stampData.approve = VSP.generalNoteForm.getProperty("oc9_Approver");
				stampData.designDate = VSP.generalNoteForm.getProperty("oc9_DesignDate").equals("")?"":DateUtil.parseDateFromTC(VSP.generalNoteForm.getProperty("oc9_DesignDate"));
				stampData.checkDate = VSP.generalNoteForm.getProperty("oc9_CheckDate").equals("")?"":DateUtil.parseDateFromTC(VSP.generalNoteForm.getProperty("oc9_CheckDate"));
				stampData.techCheckDate = VSP.generalNoteForm.getProperty("oc9_TCheckDate").equals("")?"":DateUtil.parseDateFromTC(VSP.generalNoteForm.getProperty("oc9_TCheckDate"));
				stampData.normCheckDate = VSP.generalNoteForm.getProperty("oc9_NCheckDate").equals("")?"":DateUtil.parseDateFromTC(VSP.generalNoteForm.getProperty("oc9_NCheckDate"));
				stampData.approveDate = VSP.generalNoteForm.getProperty("oc9_ApproveDate").equals("")?"":DateUtil.parseDateFromTC(VSP.generalNoteForm.getProperty("oc9_ApproveDate"));
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
	}
	
	private void readSpecifiedItemData(){
		try {
			stampData.id = VSP.topBOMLineI.getProperty("item_id") + " ВС";
			stampData.name = VSP.topBOMLineIR.getProperty("object_name");
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void checkIfMonitorIsCancelled(IProgressMonitor monitor)
	{
		if (monitor.isCanceled())
		{
			throw new CancellationException("Чтение данных было отменено");
		}
	}
	
	private void printData()
	{
		for(ReportLine line : lineList.getSortedList())
		{
			System.out.println("line " + line.name);
			for(ReportLineOccurence occurence : line.occurences())
			{
				System.out.println("occurence " + occurence.getParentItemId());
			}
		}
	}
}
