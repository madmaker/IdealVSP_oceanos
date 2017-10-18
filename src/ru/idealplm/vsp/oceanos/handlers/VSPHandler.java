package ru.idealplm.vsp.oceanos.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.ui.handlers.HandlerUtil;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.plugin.Activator;

import ru.idealplm.vsp.oceanos.core.VSP;
import ru.idealplm.vsp.oceanos.core.VSPSettings;
import ru.idealplm.vsp.oceanos.gui.ErrorListDialog;
import ru.idealplm.vsp.oceanos.gui.VSPDialog;

public class VSPHandler extends AbstractHandler
{	
	public static TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
	VSP vsp;
	ProgressMonitorDialog pd;

	public VSPHandler()
	{
	}
	
	@SuppressWarnings("restriction")
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		pd = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event).getShell());
		
		vsp = new VSP();
		vsp.init();
		vsp.readExistingData();

		VSPDialog mainDialog = new VSPDialog(HandlerUtil.getActiveShell(event).getShell(), SWT.CLOSE, vsp);
		mainDialog.open();
		
		if (!VSPSettings.isOKPressed) { return null; }
		
		try
		{
			pd.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
				{
					monitor.beginTask("Чтение данных", 100);
					monitor.worked(20);
					vsp.readData();
					monitor.beginTask("Построение отчета", 100);
					monitor.worked(60);
					vsp.buildReportFile();
					monitor.beginTask("Добавление в Teamcenter", 100);
					monitor.worked(80);
					vsp.uploadReportFile();
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
		
		vsp.openReportFile();
		
		if(!VSP.errorList.isEmpty())
		{
			ErrorListDialog errorListDialog = new ErrorListDialog(HandlerUtil.getActiveShell(event).getShell(), VSP.errorList);
		}
		
		return null;
	}
}
