package ru.idealplm.vsp.oceanos.core;

import java.awt.Desktop;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsOutput;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateItemsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemIdsAndInitialRevisionIds;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ReviseProperties;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseInfo;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseOutput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.ReviseResponse2;

import ru.idealplm.vsp.oceanos.handlers.VSPHandler;

public class ReportUploader
{
	private VSP vsp;
	private File renamedReportFile = null;
	private DataManagementService dmService;
	private String reportFullName = "Ведомость спецификаций";
	private String reportShortName = "ВС";
	private String format = "A3";
	
	public ReportUploader(VSP vsp)
	{
		this.vsp = vsp;
	}
	
	public void addToTeamcenter()
	{
		try
		{
			TCComponentDataset currentReportDataset = null;
			this.dmService = DataManagementService.getService(VSPHandler.session);
			
			if(vsp.report.report!=null)
			{
				renameReportFile();
			}
			else
			{
				throw new Exception("No report file was built.");
			}
			
			if(VSP.vspIR != null)
			{
				currentReportDataset = findExistingDataset();
				deleteDatasetNamedRefs(currentReportDataset);
			}
			else if (VSP.vspIR == null)
			{
				TCComponentItem kdDoc = findKDDocItem();
				if (kdDoc == null)
				{
					TCComponentItemRevision newItemRev = (TCComponentItemRevision)createItem("Oc9_KD", VSP.topBOMLineIR.getProperty("item_id") + " " + reportShortName,
							VSP.topBOMLineIR.getProperty("object_name"),
							"Создано утилитой по генерации документа \"" + reportFullName + "\"")[1];
					VSP.vspIR = newItemRev;
				}
				else
				{
					if (isKdLastRevHasAssemblyRev(kdDoc))
					{
						VSP.vspIR = createNextRevisionBasedOn(getLastRevOfItem(kdDoc));
						
						if (VSP.vspIR != null)
						{
							deleteRelationsToCompanyPart(VSP.vspIR);
							currentReportDataset = findExistingDataset();
							deleteDatasetNamedRefs(currentReportDataset);
						}
					}
					else
					{
						VSP.vspIR = kdDoc.getLatestItemRevision();
						currentReportDataset = findExistingDataset();
						deleteDatasetNamedRefs(currentReportDataset);
					}
				}
				
				if(VSP.vspIR!=null)
				{
					VSP.vspIR.setProperty("oc9_Format", format);
					VSP.vspIR.lock();
					VSP.vspIR.save();
					VSP.vspIR.unlock();
					VSP.topBOMLine.getItemRevision().add("Oc9_DocRel", new TCComponent[]{VSP.vspIR.getItem()});
				}
			}
	
			if(currentReportDataset==null)
			{
				TCComponentDataset ds_new = createDatasetAndAddFile(vsp.report.report.getAbsolutePath());
				if (ds_new != null)
				{
					VSP.vspIR.add("IMAN_specification", ds_new);
					saveGeneralNoteFormInfo();
					
					vsp.report.report=renamedReportFile!=null?renamedReportFile:vsp.report.report;
				}
			}
			else
			{
				String dataset_tool = "PDF_Reference";
				currentReportDataset.setFiles(new String[] { renamedReportFile!=null?renamedReportFile.getAbsolutePath():vsp.report.report.getAbsolutePath() }, new String[] { dataset_tool });
				saveGeneralNoteFormInfo();
				
				vsp.report.report=renamedReportFile!=null?renamedReportFile:vsp.report.report;
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void saveGeneralNoteFormInfo()
	{
		try
		{
			TCComponent tempComp;
			if((tempComp = VSP.vspIR.getRelatedComponent("Oc9_SignRel"))!=null)
			{
				tempComp.setProperty("oc9_Designer", vsp.report.stampData.design);
				tempComp.setProperty("oc9_Check", vsp.report.stampData.check);
				tempComp.setProperty("oc9_TCheck", vsp.report.stampData.techCheck);
				tempComp.setProperty("oc9_NCheck", vsp.report.stampData.normCheck);
				tempComp.setProperty("oc9_Approver", vsp.report.stampData.approve);
				
				tempComp.setProperty("oc9_DesignDate", vsp.report.stampData.designDate);
				tempComp.setProperty("oc9_CheckDate", vsp.report.stampData.checkDate);
				tempComp.setProperty("oc9_TCheckDate", vsp.report.stampData.techCheckDate);
				tempComp.setProperty("oc9_NCheckDate", vsp.report.stampData.normCheckDate);
				tempComp.setProperty("oc9_ApproveDate", vsp.report.stampData.approveDate);
			}
			
			VSP.vspIR.lock();
			VSP.vspIR.setProperty("oc9_Litera1", vsp.report.stampData.litera1);
			VSP.vspIR.setProperty("oc9_Litera2", vsp.report.stampData.litera2);
			VSP.vspIR.setProperty("oc9_Litera3", vsp.report.stampData.litera3);
			VSP.vspIR.save();
			VSP.vspIR.unlock();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private TCComponentItemRevision createNextRevisionBasedOn(TCComponentItemRevision itemRev)
	{
		TCComponentItemRevision out = null;
		
		ReviseProperties revProp = new ReviseProperties();
		ReviseInfo revInfo = new ReviseInfo();
		revInfo.baseItemRevision = itemRev;
		ReviseResponse2 response = dmService.revise2(new ReviseInfo[] {revInfo});
		
		Iterator it = response.reviseOutputMap.entrySet().iterator();
		if (it.hasNext())
		{
			Map.Entry entry = (Entry) it.next();
			out = ((ReviseOutput)entry.getValue()).newItemRev;
		}
		
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private CreateItemsOutput[] createItems(final ItemIdsAndInitialRevisionIds[] itemIds, final String itemType, final String itemName, final String itemDesc)
			throws TCException
	{
		final ItemProperties[] itemProps = new ItemProperties[itemIds.length];
		for (int i = 0; i < itemIds.length; i++)
		{
			final ItemProperties itemProperty = new ItemProperties();
			itemProperty.clientId = VSP.CLIENT_ID;
			itemProperty.itemId = itemIds[i].newItemId;
			itemProperty.revId = itemIds[i].newRevId;
			itemProperty.name = itemName;
			itemProperty.type = itemType;
			itemProperty.description = itemDesc;
			itemProperty.uom = "";
			itemProps[i] = itemProperty;
		}

		final CreateItemsResponse response = dmService.createItems(
				itemProps, null, null);
		return response.output;
	}
	
	public TCComponent[] createItem(final String type, final String id,
			final String name, final String desc) throws TCException
	{
		final ItemIdsAndInitialRevisionIds[] itemIds = generateItemIds(1, type);
		final CreateItemsOutput[] newItems = createItems(itemIds, type, name, desc);
		
		newItems[0].item.setProperty("item_id", id);

		return new TCComponent[] { newItems[0].item, newItems[0].itemRev };
	}
	
	private boolean isKdLastRevHasAssemblyRev(TCComponentItem kdDoc) throws TCException
	{
		boolean out = false;
		TCComponentItemRevision lastRev = getLastRevOfItem(kdDoc);
		if (lastRev != null)
		{
			AIFComponentContext[] relatedComp = lastRev.getRelated("TC_DrawingOf");
			
			for (AIFComponentContext currConetext : relatedComp)
			{
				if (currConetext.getComponent().getType().equals("Oc9_CompanyPartRevision")) {
					TCComponentItemRevision currItemRev = (TCComponentItemRevision) currConetext.getComponent(); 
					if (currItemRev.getProperty("item_id").equals(lastRev.getProperty("item_id")))
						out = true;
				}
			}
		}
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private ItemIdsAndInitialRevisionIds[] generateItemIds(final int numberOfIds, final String type) throws TCException
	{
		final GenerateItemIdsAndInitialRevisionIdsProperties property = new GenerateItemIdsAndInitialRevisionIdsProperties();
		property.count = numberOfIds;
		property.itemType = type;
		property.item = null; // Not used
		final GenerateItemIdsAndInitialRevisionIdsResponse response = dmService
				.generateItemIdsAndInitialRevisionIds(new GenerateItemIdsAndInitialRevisionIdsProperties[] { property });
		final BigInteger bIkey = new BigInteger("0");
		final Map<BigInteger, ItemIdsAndInitialRevisionIds[]> allNewIds = response.outputItemIdsAndInitialRevisionIds;
		final ItemIdsAndInitialRevisionIds[] myNewIds = allNewIds.get(bIkey);
		return myNewIds;
	}
	
	private TCComponentDataset createDatasetAndAddFile(String file_path) throws TCException
	{
		TCComponentDataset ret = null;
		String dataset_tool = null;
		String dataset_type = null;
		dataset_tool = "PDF_Reference";
		dataset_type = "PDF";
		TCComponentDatasetType dst = (TCComponentDatasetType) VSP.topBOMLineIR.getSession().getTypeComponent("Dataset");
		ret = dst.create(gen_dataset_name(), reportFullName, dataset_type);
		ret.setFiles(new String[] { renamedReportFile!=null?renamedReportFile.getAbsolutePath():vsp.report.report.getAbsolutePath() }, new String[] { dataset_tool });
		ret.lock();
		ret.save();
		ret.unlock();

		return ret;
	}

	private String gen_dataset_name() throws TCException
	{
		String ret = null;
		if (VSP.topBOMLineIR != null)
			ret = "Ведомость спецификаций - "
					+ VSP.topBOMLineIR.getTCProperty("object_name").getStringValue();
		return ret;
	}
	
	private TCComponentDataset findExistingDataset()
	{
		try{
			for (AIFComponentContext compContext : VSP.vspIR.getChildren())
			{
				if ((compContext.getComponent() instanceof TCComponentDataset) 
						&& compContext.getComponent().getProperty("object_desc").equals(reportFullName))
				{
					return (TCComponentDataset)compContext.getComponent();
				}
	
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private void deleteDatasetNamedRefs(TCComponentDataset dataset)
	{
		try
		{
			dataset.removeFiles("ImanFile");
		}
		catch (TCException e)
		{
			e.printStackTrace();
		}
	}
	
	private TCComponentDataset deletePrevSpecDatasetOfKd() throws Exception
	{
		TCComponentDataset dataset = null;
		for (AIFComponentContext compContext : VSP.vspIR.getChildren())
		{
			if ((compContext.getComponent() instanceof TCComponentDataset) 
					&& compContext.getComponent().getProperty("object_desc").equals(reportFullName))
			{
				dataset = (TCComponentDataset)compContext.getComponent();
				dataset.removeFiles("ImanFile");
			}

		}
		return dataset;
	}
	
	private TCComponentItemRevision getLastRevOfItem(TCComponentItem item) throws TCException
	{
		TCComponentItemRevision out = null;
		Map<Integer, TCComponentItemRevision> mapItemRevByRev = new HashMap<Integer, TCComponentItemRevision>();
		ArrayList<Integer> revisions = new ArrayList<Integer>();
		AIFComponentContext[] contextArray = item.getChildren();
		for (int i=0; i<contextArray.length; i++)
		{
			if (contextArray[i].getComponent().getType().equals("Oc9_KDRevision"))
			{
				TCComponentItemRevision currItemRev = (TCComponentItemRevision)contextArray[i].getComponent();
				if(currItemRev.getProperty("item_id").equals(item.getProperty("item_id")))
				{
					Integer rev = Integer.valueOf(currItemRev.getProperty("current_revision_id"));
					mapItemRevByRev.put(rev, currItemRev);
					revisions.add(rev);
				}
			}
		}
		Collections.sort(revisions);
		if (revisions.size() > 0) 
			out = mapItemRevByRev.get(revisions.get(revisions.size()-1)); 

		return out;
	}
	
	private void deleteRelationsToCompanyPart(TCComponentItemRevision rev) throws Exception
	{
		ArrayList<TCComponentItemRevision> list4Removing = new ArrayList<TCComponentItemRevision>();
		AIFComponentContext[] itemRev4Delete = rev.getItem().getRelated("Oc9_DocRel");
		for (AIFComponentContext currContext : itemRev4Delete)
		{
			if (((TCComponentItemRevision)currContext.getComponent()).getProperty("item_id")
					.equals(rev.getItem().getProperty("item_id") + " " + reportShortName))
			{
				list4Removing.add((TCComponentItemRevision)currContext.getComponent());
			}
		}
		rev.remove("Oc9_DocRel", list4Removing);
	}
	
	private void renameReportFile()
	{
		try
		{
			renamedReportFile = new File(vsp.report.data.getAbsolutePath().substring(0, vsp.report.data.getAbsolutePath().lastIndexOf("_"))+".pdf");
			Files.deleteIfExists(renamedReportFile.toPath());
			vsp.report.report.renameTo(renamedReportFile);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private TCComponentItem findKDDocItem() throws TCException
	{
		TCComponentItem result = null;
		TCComponentItemType itemType = (TCComponentItemType) VSPHandler.session.getTypeComponent("Oc9_KD");
		String criteria = VSP.topBOMLineIR.getProperty("item_id") + " " + reportShortName;
		TCComponentItem[] items = itemType.findItems(criteria);
		if (items != null && items.length > 0)
		{
			for(TCComponentItem item : items)
			{
				if(item.getType().equals("Oc9_KD"))
				{
					result = item;
					break;
				}
			}
		}
		
		return result;
	}
}
