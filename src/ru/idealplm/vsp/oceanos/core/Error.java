package ru.idealplm.vsp.oceanos.core;

public class Error
{
	String text;

	public Error(String text)
	{
		this.text = text;
	}
	
	@Override
	public boolean equals(Object v)
	{
		boolean retVal = false;

	    if (v instanceof Error){
	    	Error ptr = (Error) v;
	        retVal = ptr.text.equals(this.text);
	    }
	    
	    return retVal;
	}
	
	@Override
    public int hashCode()
	{
        int hash = 7;
        hash = 17 * hash + (this.text != null ? this.text.hashCode() : 0);
        return hash;
    }
}
