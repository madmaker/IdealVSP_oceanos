package ru.idealplm.vsp.oceanos.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.plugin.Activator;

import ru.idealplm.vsp.oceanos.core.VSP;
import ru.idealplm.vsp.oceanos.core.VSPSettings;
import ru.idealplm.vsp.oceanos.gui.VSPDialog;

public class VSPHandler extends AbstractHandler
{	
	public static TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	VSP vsp;

	public VSPHandler()
	{
	}
	
	@SuppressWarnings("restriction")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ProgressMonitorDialog pd = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event).getShell());
		
		vsp = new VSP();
		vsp.progressMonitor = pd;
		vsp.init();

		VSPDialog mainDialog = new VSPDialog(HandlerUtil.getActiveShell(event).getShell(), SWT.CLOSE, vsp);
		

		mainDialog.open();
		
		if (!VSPSettings.isOKPressed) { return null; }
		
		
		vsp.readData();
		vsp.buildXmlFile();
		vsp.buildReportFile();
		vsp.uploadReportFile();
		
		return null;
	}
}
