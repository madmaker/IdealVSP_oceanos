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
	public DataManagementService dmService;
	
	public ReportUploader(VSP vsp)
	{
		this.vsp = vsp;
	}
	
	public void addToTeamcenter()
	{
		try{
			TCComponentDataset currentVSPDataset = null;
			this.dmService = DataManagementService.getService(VSPHandler.session);
			
			if(vsp.report.report!=null){
				try{
					renamedReportFile = new File(vsp.report.data.getAbsolutePath().substring(0, vsp.report.data.getAbsolutePath().lastIndexOf("_"))+".pdf");
					Files.deleteIfExists(renamedReportFile.toPath());
					vsp.report.report.renameTo(renamedReportFile);
					System.out.println(vsp.report.report.getAbsolutePath());
					System.out.println(renamedReportFile.getAbsolutePath());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if(VSP.vspIR != null) {
				System.out.println("+++++++++++  SPREV!=NULL");
				currentVSPDataset = deletePrevSpecDatasetOfKd();
			} else if (VSP.vspIR == null) {
				System.out.println("+++++++++++  SPREV==NULL");
				TCComponentItem kdDoc = findKDDocItem();
				if (kdDoc == null) {
					System.out.println("CREATING KD ITEM WITH FIRST ITEMREVISION + SignForm!");
					TCComponentItemRevision newItemRev = (TCComponentItemRevision)createItem("Oc9_KD", VSP.topBOMLineIR.getProperty("item_id") + " ВС",
							VSP.topBOMLineIR.getProperty("object_name"),
							"Создано утилитой по генерации документа \"Ведомость спецификаций\"")[1];
					VSP.vspIR = newItemRev;
				} else {
					System.out.println("+++++++++++  KD!=NULL");
					System.out.println(kdDoc.getProperty("item_id"));
					if (isKdLastRevHasAssemblyRev(kdDoc)) {
						System.out.println("REVISE AND REMOVE SP + SignForm...");
						VSP.vspIR = createNextRevisionBasedOn(getLastRevOfItem(kdDoc));
						
						if (VSP.vspIR != null) {
							deleteRelationsToCompanyPart(VSP.vspIR);
							currentVSPDataset = deletePrevSpecDatasetOfKd();
						}
					} else {
						System.out.println("REPLACING LAST REVISION!");
						VSP.vspIR = kdDoc.getLatestItemRevision();
						currentVSPDataset = deletePrevSpecDatasetOfKd();
					}
				}
				
				if(VSP.vspIR!=null){
					VSP.vspIR.setProperty("oc9_Format", "A3");
					VSP.vspIR.lock();
					VSP.vspIR.save();
					VSP.vspIR.unlock();
					VSP.topBOMLine.getItemRevision().add("Oc9_DocRel", new TCComponent[]{VSP.vspIR.getItem()});
				}
				//spRev.getItem().("Oc9_DocRel", topBOMLine.getItemRevision());
				//spRev.setProperty("pm8_Format", finalFormat(page));
			}
	
			if(currentVSPDataset==null){
				TCComponentDataset ds_new = createDatasetAndAddFile(vsp.report.report.getAbsolutePath());
				if (ds_new != null) {
					System.out.println("Adding to item_id: " + VSP.vspIR.getProperty("item_id"));
					VSP.vspIR.add("IMAN_specification", ds_new);
					saveGeneralNoteFormInfo();
					
					ds_new.getFiles("")[0].setReadOnly();
					Desktop.getDesktop().open(ds_new.getFiles("")[0]);
				}
			} else {
				System.out.println("SPEC DATASET IS NOT NULL");
				String dataset_tool = "PDF_Reference";
				currentVSPDataset.setFiles(new String[] { renamedReportFile!=null?renamedReportFile.getAbsolutePath():vsp.report.report.getAbsolutePath() }, new String[] { dataset_tool });
				saveGeneralNoteFormInfo();
				
				currentVSPDataset.getFiles("")[0].setReadOnly();
				Desktop.getDesktop().open(currentVSPDataset.getFiles("")[0]);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void saveGeneralNoteFormInfo()
	{
		try{
			TCComponent tempComp;
			if((tempComp = VSP.vspIR.getRelatedComponent("Oc9_SignRel"))!=null)
			{
				System.out.println("+++++FOUND SIGN FORM!!!!");
				tempComp.setProperty("oc9_Designer", vsp.stampData.design);
				tempComp.setProperty("oc9_Check", vsp.stampData.check);
				tempComp.setProperty("oc9_TCheck", vsp.stampData.techCheck);
				tempComp.setProperty("oc9_NCheck", vsp.stampData.normCheck);
				tempComp.setProperty("oc9_Approver", vsp.stampData.approve);
				
				tempComp.setProperty("oc9_DesignDate", vsp.stampData.designDate);
				tempComp.setProperty("oc9_CheckDate", vsp.stampData.checkDate);
				tempComp.setProperty("oc9_TCheckDate", vsp.stampData.techCheckDate);
				tempComp.setProperty("oc9_NCheckDate", vsp.stampData.normCheckDate);
				tempComp.setProperty("oc9_ApproveDate", vsp.stampData.approveDate);
			}
			/*if(VSP.vspIR.getRelatedComponent("IMAN_master_form_rev")!=null){
				specIR.getRelatedComponent("IMAN_master_form_rev").setProperty("object_desc", Specification.settings.getStringProperty("blockSettings"));
			}*/
			
			VSP.vspIR.lock();
			//topBOMLine.getItemRevision().setProperty("oc9_AddNote", Specification.settings.getStringProperty("AddedText"));
			VSP.vspIR.setProperty("oc9_Litera1", vsp.stampData.litera1);
			VSP.vspIR.setProperty("oc9_Litera2", vsp.stampData.litera2);
			VSP.vspIR.setProperty("oc9_Litera3", vsp.stampData.litera3);
			VSP.vspIR.save();
			VSP.vspIR.unlock();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private TCComponentItemRevision createNextRevisionBasedOn(TCComponentItemRevision itemRev) {
		TCComponentItemRevision out = null;
		
		ReviseProperties revProp = new ReviseProperties();
		ReviseInfo revInfo = new ReviseInfo();
		revInfo.baseItemRevision = itemRev;
		ReviseResponse2 response = dmService.revise2(new ReviseInfo[] {revInfo});
		
		System.out.println("MAP SIZE = " + response.reviseOutputMap.size());
		Iterator it = response.reviseOutputMap.entrySet().iterator();
		if (it.hasNext()) {
			System.out.println("trying to return itemRev...");
			Map.Entry entry = (Entry) it.next();
			System.out.println("Class NAME VALUE: " + entry.getValue().getClass().getName() + " = " + entry.getKey()
					+ "\nClass NAME KEY: " + entry.getKey().getClass().getName()
					);
			out = ((ReviseOutput)entry.getValue()).newItemRev;
		}
		
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private CreateItemsOutput[] createItems(final ItemIdsAndInitialRevisionIds[] itemIds, final String itemType, final String itemName, final String itemDesc)
			throws TCException {
//		final GetItemCreationRelatedInfoResponse relatedResponse = BuildSpec2G.dmService.getItemCreationRelatedInfo(itemType, null);
		final ItemProperties[] itemProps = new ItemProperties[itemIds.length];
		for (int i = 0; i < itemIds.length; i++) {
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
			final String name, final String desc) throws TCException {
		
		final ItemIdsAndInitialRevisionIds[] itemIds = generateItemIds(1, type);
		final CreateItemsOutput[] newItems = createItems(itemIds, type, name, desc);
		
		newItems[0].item.setProperty("item_id", id);

		return new TCComponent[] { newItems[0].item, newItems[0].itemRev };
	}
	
	private boolean isKdLastRevHasAssemblyRev(TCComponentItem kdDoc) throws TCException {
		boolean out = false;
		TCComponentItemRevision lastRev = getLastRevOfItem(kdDoc);
		if (lastRev != null) {
			AIFComponentContext[] relatedComp = lastRev.getRelated("TC_DrawingOf");
			System.out.println("got " + relatedComp.length + " Specs from LAST REVISIONS");
			
			for (AIFComponentContext currConetext : relatedComp) {
				System.out.println("TYPE: " + currConetext.getComponent().getType());
				if (currConetext.getComponent().getType().equals("Pm8_CompanyPartRevision")) {
					TCComponentItemRevision currItemRev = (TCComponentItemRevision) currConetext.getComponent(); 
					if (currItemRev.getProperty("pm8_Designation").equals(lastRev.getProperty("item_id")))
						out = true;
				}
			}
		}
		System.out.println("IS KD LAST REV HAS ASSEMBLY? >> " + out);
		return out;
	}
	
	@SuppressWarnings("unchecked")
	private ItemIdsAndInitialRevisionIds[] generateItemIds(final int numberOfIds, final String type) throws TCException {
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
	
	private TCComponentDataset createDatasetAndAddFile(String file_path)
			throws TCException {
		TCComponentDataset ret = null;
		String dataset_tool = null;
		String dataset_type = null;
		dataset_tool = "PDF_Reference";
		dataset_type = "PDF";
		TCComponentDatasetType dst = (TCComponentDatasetType) VSP.topBOMLineIR.getSession().getTypeComponent("Dataset");
		ret = dst.create(gen_dataset_name(), "Ведомость спецификаций", dataset_type);
		ret.setFiles(new String[] { renamedReportFile!=null?renamedReportFile.getAbsolutePath():vsp.report.report.getAbsolutePath() }, new String[] { dataset_tool });
		ret.lock();
		ret.save();
		ret.unlock();

		return ret;
	}

	private String gen_dataset_name() throws TCException {
		String ret = null;
		if (VSP.topBOMLineIR != null)
			ret = "Ведомость спецификаций - "
					+ VSP.topBOMLineIR.getTCProperty("object_name").getStringValue();
		return ret;
	}
	
	private TCComponentDataset deletePrevSpecDatasetOfKd() throws Exception {
		TCComponentDataset dataset = null;
		for (AIFComponentContext compContext : VSP.vspIR.getChildren()){
			System.out.println(">>> TYPE: " + compContext.getComponent().getProperty("object_type"));
			if ((compContext.getComponent() instanceof TCComponentDataset) 
					&& compContext.getComponent().getProperty("object_desc").equals("Ведомость спецификаций")) {
				dataset = (TCComponentDataset)compContext.getComponent();
				System.out.println("Deleting Spec Dataset Named Ref in KD");
				dataset.removeFiles("ImanFile");
				System.out.println("after destroying");
			}

		}
		return dataset;
	}
	
	private TCComponentItemRevision getLastRevOfItem(TCComponentItem item) throws TCException {
		TCComponentItemRevision out = null;
		Map<Integer, TCComponentItemRevision> mapItemRevByRev = new HashMap<Integer, TCComponentItemRevision>();
		ArrayList<Integer> revisions = new ArrayList<Integer>();
		AIFComponentContext[] contextArray = item.getChildren();
		System.out.println("Children of ITEM: " + contextArray.length);
		for (int i=0; i<contextArray.length; i++) {
			System.out.println("~~~~ TYPE: " + contextArray[i].getComponent().getType());
			if (contextArray[i].getComponent().getType().equals("Oc9_KDRevision")) {
				TCComponentItemRevision currItemRev = (TCComponentItemRevision)contextArray[i].getComponent();
				if(currItemRev.getProperty("item_id").equals(item.getProperty("item_id"))) {
					System.out.println("ADDING TO MAP!");
					Integer rev = Integer.valueOf(currItemRev.getProperty("current_revision_id"));
					mapItemRevByRev.put(rev, currItemRev);
					revisions.add(rev);
				}
			}
		}
		Collections.sort(revisions);
		if (revisions.size() > 0) 
			out = mapItemRevByRev.get(revisions.get(revisions.size()-1)); 
		
		System.out.println("returning: " + out.getProperty("item_id"));
		return out;
	}
	
	private static void deleteRelationsToCompanyPart(TCComponentItemRevision rev) throws Exception {
		ArrayList<TCComponentItemRevision> list4Removing = new ArrayList<TCComponentItemRevision>();
		AIFComponentContext[] itemRev4Delete = rev.getItem().getRelated("Oc9_DocRel");
		for (AIFComponentContext currContext : itemRev4Delete) {
			if (((TCComponentItemRevision)currContext.getComponent()).getProperty("item_id")
					.equals(rev.getItem().getProperty("item_id") + " ВС")) {
				System.out.println("~~~ Added to delete");
				list4Removing.add((TCComponentItemRevision)currContext.getComponent());
			}
		}
		rev.remove("Oc9_DocRel", list4Removing);
	}
	
	private TCComponentItem findKDDocItem() throws TCException {
		TCComponentItem result = null;
		TCComponentItemType itemType = (TCComponentItemType) VSPHandler.session.getTypeComponent("Oc9_KD");
		String criteria = VSP.topBOMLineIR.getProperty("item_id") + " ВС";
		TCComponentItem[] items = itemType.findItems(criteria);
		if (items != null && items.length > 0) {
			for(TCComponentItem item : items){
				System.out.println("Found item " + item.getProperty("item_id") + " of type " + item.getType());
				if(item.getType().equals("Oc9_KD")){
					result = item;
					break;
				}
			}
		}
		
		return result;
	}
}
