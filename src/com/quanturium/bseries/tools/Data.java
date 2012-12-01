package com.quanturium.bseries.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;

public class Data
{

	public static void setString(Context context, String fileName, String content)
	{
		FileOutputStream fos = null;
		OutputStreamWriter output = null;

		try
		{
			fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			output = new OutputStreamWriter(fos);
			output.write(content);
			output.flush();
			output.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getString(Context context, String fileName)
	{
		FileInputStream fis = null;
		BufferedReader input = null;
		StringBuilder data = new StringBuilder();

		try
		{
			fis = context.openFileInput(fileName);
			input = new BufferedReader(new InputStreamReader(fis));

			String line;

			while ((line = input.readLine()) != null)
			{
				data.append(line);
			}

		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return data.toString();
	}

}
