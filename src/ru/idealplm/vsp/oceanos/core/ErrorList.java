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
		System.out.println("?" + error.text + " " + errors.contains(error));
		if(!errors.contains(error))
			errors.add(error);
	}
	
	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		for(Error error : errors)
			stringBuilder.append(error.text + "\n");
		return stringBuilder.toString();
	}
	
	public void printList()
	{
		for(Error error : errors)
			System.out.println(error.text);
	}
	
	public boolean isEmpty()
	{
		return errors.isEmpty() ? true : false;
	}
}
