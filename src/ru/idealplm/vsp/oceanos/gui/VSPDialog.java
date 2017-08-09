package ru.idealplm.vsp.oceanos.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.teamcenter.rac.util.DateButton;

import ru.idealplm.vsp.oceanos.core.VSP;
import ru.idealplm.vsp.oceanos.core.VSPSettings;

public class VSPDialog extends Dialog
{
	protected Object result;
	protected Shell shell;
	private TabFolder tabFolder;
	private TabItem tabMain;
	private TabItem tabSignatures;
	private Composite compositeMain;
	private Composite compositeSignatures;
	
	private Text text_Litera1;
	private Text text_Litera2;
	private Text text_Litera3;
	private VSP vsp;
	
	private Text textDesigner;
	private Text textCheck;
	private Text textTCheck;
	private Text textNCheck;
	private Text textApprover;
	
	private DateButton dateDesigner;
	private DateButton dateCheck;
	private DateButton dateTCheck;
	private DateButton dateNCheck;
	private DateButton dateApprover;
	
	public VSPDialog(Shell parent, int style, VSP vsp) {
		super(parent, style);
		this.vsp = vsp;
	}
	
	public Object open()
	{
		createContents();
		fillContents();
		shell.setLayout(new FillLayout());
		shell.open();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	private void createContents()
	{
		shell = new Shell();
		shell.setSize(470, 605);
		shell.setText("\u0421\u043F\u0435\u0446\u0438\u0444\u0438\u043A\u0430\u0446\u0438\u044F");
		tabFolder = new TabFolder(shell, SWT.NONE);
		tabMain = new TabItem(tabFolder, SWT.BORDER);
		tabSignatures = new TabItem(tabFolder, SWT.BORDER);
		compositeMain = new Composite(tabFolder, SWT.NONE);
		compositeSignatures = new Composite(tabFolder, SWT.NONE);
		compositeMain.setLayout(null);
		compositeSignatures.setLayout(null);
		tabMain.setControl(compositeMain);
		tabSignatures.setControl(compositeSignatures);
		
		Label labelDesigner = new Label(compositeSignatures, SWT.NONE);
		labelDesigner.setText("\u0420\u0430\u0437\u0440\u0430\u0431\u043E\u0442\u0430\u043B");
		labelDesigner.setBounds(37, 47, 90, 23);
		
		textDesigner = new Text(compositeSignatures, SWT.BORDER);
		textDesigner.setBounds(144, 44, 110, 23);
		
		Label labelCheck = new Label(compositeSignatures, SWT.NONE);
		labelCheck.setBounds(37, 88, 90, 23);
		labelCheck.setText("\u041F\u0440\u043E\u0432\u0435\u0440\u0438\u043B");
		
		textCheck = new Text(compositeSignatures, SWT.BORDER);
		textCheck.setBounds(144, 85, 110, 23);
		
		/*Label labelAddCheck = new Label(compositeSignatures, SWT.NONE);
		labelAddCheck.setBounds(173, 20, 90, 23);
		labelAddCheck.setText("\u0424\u0430\u043C\u0438\u043B\u0438\u044F");*/
		
		textTCheck = new Text(compositeSignatures, SWT.BORDER);
		textTCheck.setBounds(144, 128, 110, 23);
		
		Label labelNCheck = new Label(compositeSignatures, SWT.NONE);
		labelNCheck.setBounds(37, 173, 90, 23);
		labelNCheck.setText("\u041D.\u043A\u043E\u043D\u0442\u0440\u043E\u043B\u044C");
		
		Label labelTCheck = new Label(compositeSignatures, SWT.NONE);
		labelTCheck.setBounds(37, 128, 90, 23);
		labelTCheck.setText("Т.контр");
		
		Label labelApprover = new Label(compositeSignatures, SWT.NONE);
		labelApprover.setBounds(37, 215, 90, 23);
		labelApprover.setText("\u0423\u0442\u0432\u0435\u0440\u0434\u0438\u043B");
		
		textNCheck = new Text(compositeSignatures, SWT.BORDER);
		textNCheck.setBounds(144, 170, 110, 23);
		
		textApprover = new Text(compositeSignatures, SWT.BORDER);
		textApprover.setBounds(144, 212, 110, 23);
		
		//TODO okeanos
		Composite compositeDesigner = new Composite(compositeSignatures, SWT.EMBEDDED);
		compositeDesigner.setBounds(260, 44, 150, 23);
		java.awt.Frame frameDesigner = SWT_AWT.new_Frame(compositeDesigner);
		java.awt.Panel panelDesigner = new java.awt.Panel(new java.awt.BorderLayout());
	    frameDesigner.add(panelDesigner);
		dateDesigner = new DateButton();
		dateDesigner.setDoubleBuffered(true);
		panelDesigner.add(dateDesigner);
		
		Composite compositeCheck = new Composite(compositeSignatures, SWT.EMBEDDED);
		compositeCheck.setBounds(260, 85, 150, 23);
		java.awt.Frame frameCheck = SWT_AWT.new_Frame(compositeCheck);
	    java.awt.Panel panelCheck = new java.awt.Panel(new java.awt.BorderLayout());
	    frameCheck.add(panelCheck);
		dateCheck = new DateButton();
		dateCheck.setDoubleBuffered(true);
		panelCheck.add(dateCheck);
		
		Composite compositeTCheck = new Composite(compositeSignatures, SWT.EMBEDDED);
		compositeTCheck.setBounds(260, 128, 150, 23);
		java.awt.Frame frameTCheck = SWT_AWT.new_Frame(compositeTCheck);
	    java.awt.Panel panelTCheck = new java.awt.Panel(new java.awt.BorderLayout());
	    frameTCheck.add(panelTCheck);
		dateTCheck = new DateButton();
		dateTCheck.setDoubleBuffered(true);
		panelTCheck.add(dateTCheck);
		
		Composite compositeNCheck = new Composite(compositeSignatures, SWT.EMBEDDED);
		compositeNCheck.setBounds(260, 170, 150, 23);
		java.awt.Frame frameNCheck = SWT_AWT.new_Frame(compositeNCheck);
		java.awt.Panel panelNCheck = new java.awt.Panel(new java.awt.BorderLayout());
		frameNCheck.add(panelNCheck);
		dateNCheck = new DateButton();
		dateNCheck.setDoubleBuffered(true);
		panelNCheck.add(dateNCheck);
		
		Composite compositeApprover = new Composite(compositeSignatures, SWT.EMBEDDED);
		compositeApprover.setBounds(260, 212, 150, 23);
		java.awt.Frame frameApprover = SWT_AWT.new_Frame(compositeApprover);
	    java.awt.Panel panelApprover = new java.awt.Panel(new java.awt.BorderLayout());
	    frameApprover.add(panelApprover);
		dateApprover = new DateButton();
		dateApprover.setDoubleBuffered(true);
		panelApprover.add(dateApprover);
		
	    tabMain.setText("Настройки");
	    tabSignatures.setText("\u041F\u043E\u0434\u043F\u0438\u0441\u0430\u043D\u0442\u044B");

	    final Button button_ShowAdditionalForm = new Button(compositeMain, SWT.CHECK);
		button_ShowAdditionalForm.setBounds(10, 320, 225, 16);
		button_ShowAdditionalForm.setText("Показать дополнительную форму");
		
		Label label_litera_1 = new Label(compositeMain, SWT.NONE);
		label_litera_1.setText("\u041B\u0438\u0442\u0435\u0440\u0430 1");
		label_litera_1.setBounds(10, 460, 76, 13);
		
		text_Litera1 = new Text(compositeMain, SWT.BORDER);
		text_Litera1.setBounds(10, 477, 76, 19);
		
		Label label_litera_2 = new Label(compositeMain, SWT.NONE);
		label_litera_2.setText("\u041B\u0438\u0442\u0435\u0440\u0430 2");
		label_litera_2.setBounds(96, 460, 76, 13);
		
		text_Litera2 = new Text(compositeMain, SWT.BORDER);
		text_Litera2.setBounds(96, 477, 76, 19);
		
		Label label_litera_3 = new Label(compositeMain, SWT.NONE);
		label_litera_3.setText("\u041B\u0438\u0442\u0435\u0440\u0430 3");
		label_litera_3.setBounds(182, 460, 76, 13);
		
		text_Litera3 = new Text(compositeMain, SWT.BORDER);
		text_Litera3.setBounds(182, 477, 76, 19);
	    
		Button btnOk = new Button(compositeMain, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				VSPSettings.isOKPressed = true;
				VSPSettings.doShowAdditionalForm = button_ShowAdditionalForm.getSelection();
				
				vsp.stampData.litera1 = text_Litera1.getText();
				vsp.stampData.litera2 = text_Litera2.getText();
				vsp.stampData.litera3 = text_Litera3.getText();
				
				vsp.stampData.design = textDesigner.getText();
				vsp.stampData.check = textCheck.getText();
				vsp.stampData.techCheck = textTCheck.getText();
				vsp.stampData.normCheck = textNCheck.getText();
				vsp.stampData.approve = textApprover.getText();
				
				vsp.stampData.designDate = dateDesigner.getText().equals("Дата не установлена.")?null:fixData(dateDesigner.getText());
				vsp.stampData.checkDate = dateDesigner.getText().equals("Дата не установлена.")?null:fixData(dateCheck.getText());
				vsp.stampData.techCheckDate = dateDesigner.getText().equals("Дата не установлена.")?null:fixData(dateTCheck.getText());
				vsp.stampData.normCheckDate = dateDesigner.getText().equals("Дата не установлена.")?null:fixData(dateNCheck.getText());
				vsp.stampData.approveDate = dateDesigner.getText().equals("Дата не установлена.")?null:fixData(dateApprover.getText());
				
				shell.dispose();
				System.out.println("OK!");
			}
		});
		
		btnOk.setBounds(139, 512, 68, 23);
		btnOk.setText("OK");
		
		Button btnCancel = new Button(compositeMain, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		btnCancel.setText("Cancel");
		btnCancel.setBounds(229, 512, 68, 23);
	}
	
	private void fillContents()
	{
		
	}
	
	private String fixData(String input){
		String output = input;
		if(input.substring(0, input.indexOf("-")).length()<2){
			output = "0"+output;
		}
		return output;
	}
}
