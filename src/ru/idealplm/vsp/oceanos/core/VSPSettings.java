package ru.idealplm.vsp.oceanos.core;

public class VSPSettings
{
	public static boolean isOKPressed;
	public static boolean isCancelled;
	public static boolean doShowAdditionalForm;
	
	public static String[] nonbreakableWords;
	
	static
	{
		reset();
	}
	
	public static void reset()
	{
		isOKPressed = false;
		isCancelled = false;
		doShowAdditionalForm = false;
		if(nonbreakableWords!=null && nonbreakableWords.length > 0)
			nonbreakableWords = new String[]{};
	}
}
