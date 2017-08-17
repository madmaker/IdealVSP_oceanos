package ru.idealplm.vsp.oceanos.core;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;

import ru.idealplm.vsp.oceanos.util.DateUtil;

public class StampData
{
	public String	name = "";
	public String	id = "";
	public String	pervPrim = "";
	public String	invNo = "";
	public String	litera1 = "";
	public String	litera2 = "";
	public String	litera3 = "";
	public String	design = "";
	public String	designDate = "";
	public String	check = "";
	public String	checkDate = "";
	public String	approve = "";
	public String	approveDate = "";
	public String	techCheck = "";
	public String	techCheckDate = "";
	public String	normCheck = "";
	public String	normCheckDate = "";
	
	public void print()
	{
		System.out.println("--==--");
		System.out.println(name);
		System.out.println(id);
		System.out.println(design);
		System.out.println(designDate);
		System.out.println(check);
		System.out.println(checkDate);
		System.out.println("--==--");
	}
}
