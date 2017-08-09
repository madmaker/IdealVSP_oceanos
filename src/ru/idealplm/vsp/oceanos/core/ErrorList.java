package ru.idealplm.vsp.oceanos.core;

import java.util.ArrayList;

public class ErrorList
{
	private ArrayList<Error> errors;

	public ErrorList()
	{
		this.errors = new ArrayList<Error>();
	}
	
	public void storeError(Error error)
	{
		errors.add(error);
	}
}
