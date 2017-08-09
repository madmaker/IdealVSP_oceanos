package ru.idealplm.vsp.oceanos.core;

public class VSPSettings
{
	public static boolean isOKPressed;
	public static boolean isCancelled;
	public static boolean doShowAdditionalForm;
	
	static
	{
		reset();
	}
	
	public static void reset()
	{
		isOKPressed = false;
		isCancelled = false;
		doShowAdditionalForm = false;
	}
}
