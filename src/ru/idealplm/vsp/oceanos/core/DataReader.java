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

import ru.idealplm.vsp.oceanos.data.ReportLine;
import ru.idealplm.vsp.oceanos.data.ReportLine.ReportLineType;
import ru.idealplm.vsp.oceanos.data.ReportLineList;
import ru.idealplm.vsp.oceanos.data.ReportLineOccurence;
import ru.idealplm.vsp.oceanos.handlers.VSPHandler;
import ru.idealplm.vsp.oceanos.util.DateUtil;

public class DataReader
{
	private VSP vsp;
	private StampData stampData;
	private ReportLineList lineList;
	private StructureManagementService smsService = StructureManagementService.getService(VSPHandler.session);
	private ProgressMonitorDialog pd;
	private String blPropertyNames[] = {"bl_item_object_type", "bl_Part_oc9_TypeOfPart", "bl_quantity", "bl_item_item_id", "Oc9_Note"};
	private String blPropertyValues[];
	private TCComponent document;
	private ReportLineOccurence emptyOccurence;
	
	public DataReader(VSP vsp)
	{
		this.vsp = vsp;
		this.stampData = vsp.report.stampData;
		pd = vsp.progressMonitor;
		lineList = vsp.report.linesList;
		emptyOccurence = new ReportLineOccurence(null, null);
	}
	
	public void readExistingData()
	{
		findExistingVSP();
		readGeneralNoteForm();
		readExistingVSPData();
		readSpecifiedItemData();
	}
	
	public void calculateQuantities(ReportLineOccurence occurence)
	{
		
	}
	
	public void readData()
	{
		try
		{
			pd.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					monitor.beginTask("������ ������", 100);
					ReportLineOccurence currentOccurence = readBomLineData(VSP.topBOMLine, emptyOccurence);
					readBomData(VSP.topBOMLine, currentOccurence, emptyOccurence, monitor);
					monitor.done();
				}
			});
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (CancellationException ex)
		{
			VSPSettings.isCancelled = true;
			System.out.println(ex.getMessage());
		}
	}
	
	private void readBomData(TCComponentBOMLine bomLine, ReportLineOccurence currentOccurence, ReportLineOccurence parentOccurence, IProgressMonitor monitor)
	{
		ReportLineOccurence tempOccurence;
		if(currentOccurence==null) return;
		ExpandPSOneLevelInfo levelInfo = new ExpandPSOneLevelInfo();
		ExpandPSOneLevelPref levelPref = new ExpandPSOneLevelPref();

		levelInfo.parentBomLines = new TCComponentBOMLine[] { bomLine };
		levelInfo.excludeFilter = "None";
		levelPref.expItemRev = true;

		ExpandPSOneLevelResponse levelResp = smsService.expandPSOneLevel(levelInfo, levelPref);

		if (levelResp.output.length > 0)
		{
			for (ExpandPSOneLevelOutput levelOut : levelResp.output)
			{
				for (ExpandPSData psData : levelOut.children)
				{
					tempOccurence = readBomLineData(psData.bomLine, currentOccurence);
					if(tempOccurence!=null)
						currentOccurence.addChildOccurence(tempOccurence);
					checkIfMonitorIsCancelled(monitor);
				}
			}
			for(ReportLineOccurence child : currentOccurence.children)
			{
				readBomData(child.bomLine, child, currentOccurence, monitor);
				checkIfMonitorIsCancelled(monitor);
			}
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
				printBOMLineInfo(bomLine);
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
			if(document!=null){
				System.out.println("processing doc="+document.getUid());
				if(lineList.containsLineWithUid(document.getUid())){
					updateExistingLine(bomLine, parentOccurence);
				} else {
					resultOccurence = addNewLine(bomLine, parentOccurence);
				}
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
			if(line.type==ReportLineType.DOCUMENT) line.name = "��������� ������������\n"+line.name;
			System.out.println("new line for "+line.name);
			resultOccurence = new ReportLineOccurence(line, parentOccurence);
			resultOccurence.quantity = quantity;
			resultOccurence.quantityMult = parentOccurence.calcTotalQuantity().getTotalQuantity();
			resultOccurence.bomLine = bomLine;
			resultOccurence.remark = blPropertyValues[4];
			line.occurences.add(resultOccurence);
			lineList.addLine(line);
		} catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		return resultOccurence;
	}
	
	public void updateExistingLine(TCComponentBOMLine bomLine, ReportLineOccurence parentOccurence)
	{
		ReportLineOccurence resultOccurence = null;
		int quantity = blPropertyValues[2].trim().isEmpty()?1:Integer.parseInt(blPropertyValues[2]);
		ReportLine line = lineList.getLine(document.getUid());
		resultOccurence = new ReportLineOccurence(line, parentOccurence);
		resultOccurence.quantity = quantity;
		resultOccurence.quantityMult = parentOccurence.calcTotalQuantity().getTotalQuantity();
		resultOccurence.bomLine = bomLine;
		resultOccurence.remark = blPropertyValues[4];
		line.occurences.add(resultOccurence);
	}
	
	public boolean hasValidType(String itemType, String partType)
	{
		if(!itemType.equals("������� �����������") || (!partType.equals("��������� �������") && !partType.equals("��������")))
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
			if(documentId.equals(revId + " ��") && !revId.equals(vsp.report.targetId))
			{
				type = ReportLineType.DOCUMENT;
			} else if (blPropertyValues[1].equals("��������� �������"))
			{
				type = ReportLineType.ASSEMBLY;
			} else if (blPropertyValues[1].equals("��������"))
			{
				type = ReportLineType.KIT;
			}
		} catch (Exception ex) 
		{
			ex.printStackTrace();
		}
		return type;
	}
	
	public void printBOMLineInfo(TCComponentBOMLine line)
	{
		try {
			String values[] = line.getProperties(blPropertyNames);
			StringBuilder stringBuilder = new StringBuilder();
			for(int i = 0; i < values.length; i++) {
				stringBuilder.append(values[i] + "   ");
			}
		} catch (TCException ex) {
			ex.printStackTrace();
		}
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
				if(id.equals(rev.getProperty("item_id") + " ��") && !rev.getProperty("item_id").equals(vsp.report.targetId)) {
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
				if(id.equals(IRid + " ��")){
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
			stampData.id = VSP.topBOMLineI.getProperty("item_id") + " ��";
			stampData.name = VSP.topBOMLineIR.getProperty("object_name");
			//Specification.settings.addStringProperty("AddedText", bomLine.getItemRevision().getProperty("oc9_AddNote").trim().equals("")?null:bomLine.getItemRevision().getProperty("oc9_AddNote").trim());
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void checkIfMonitorIsCancelled(IProgressMonitor monitor)
	{
		if (monitor.isCanceled())
		{
			throw new CancellationException("������ ������ ���� ��������");
		}
	}
}
