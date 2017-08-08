package ru.idealplm.vsp.oceanos.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class VSPHandler extends AbstractHandler
{	
	public VSPHandler(){}
	
	@SuppressWarnings("restriction")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.out.println("Сисаут\n");
		return null;
	}
}
