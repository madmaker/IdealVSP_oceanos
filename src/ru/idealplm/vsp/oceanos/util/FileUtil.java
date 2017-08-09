package ru.idealplm.vsp.oceanos.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil
{
	public static void copy(InputStream source, File dest) throws IOException
	{
		try
		{
			FileOutputStream os = new FileOutputStream(dest);
			try
			{
				byte[] buffer = new byte[4096];
				int length;
				while ((length = source.read(buffer)) > 0)
				{
					os.write(buffer, 0, length);
				}
			}
			finally
			{
				os.close();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			source.close();
		}
	}
}
