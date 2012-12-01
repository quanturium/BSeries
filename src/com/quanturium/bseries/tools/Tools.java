package com.quanturium.bseries.tools;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.Display;

public class Tools
{
	public static String implode(String delim, String[] args)
	{
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < args.length; i++)
		{
			if (i > 0)
				sb.append(delim);

			sb.append(args[i]);
		}

		return sb.toString();
	}

	public static String md5(String key)
	{
		byte[] uniqueKey = key.getBytes();
		byte[] hash = null;
		StringBuffer hashString = new StringBuffer();
		try
		{

			hash = MessageDigest.getInstance("MD5").digest(uniqueKey);

			for (int i = 0; i < hash.length; ++i)
			{
				String hex = Integer.toHexString(hash[i]);
				if (hex.length() == 1)
				{
					hashString.append('0');
					hashString.append(hex.charAt(hex.length() - 1));
				}
				else
				{
					hashString.append(hex.substring(hex.length() - 2));
				}
			}

		}
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return hashString.toString();
	}

	public static String timestamp2date(int timestamp)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis((long) timestamp * 1000);

		return cal.get(Calendar.DAY_OF_MONTH) + " " + Tools.getMonth(cal.get(Calendar.MONTH)) + " " + cal.get(Calendar.YEAR);
	}
	
	public static String timestamp2dateAndTime(int timestamp)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis((long) timestamp * 1000);

		return cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR) + " ˆ " + cal.get(Calendar.HOUR_OF_DAY) + "h" + cal.get(Calendar.MINUTE);
	}

	public static String timestamp2timeleft(int timestamp)
	{
		int timestampB = timestamp;
		int timestampA = Tools.getTimestampOfTodayAtMidnight();

		int timeleftSecond = timestampB - timestampA;

		int timeleftMonth = timeleftSecond / (3600 * 24 * 30);

		if (timeleftMonth > 0)
		{
			int timeleftDays = ((timeleftSecond - timeleftMonth * 3600 * 24 * 30) / (3600 * 24));
			return timeleftMonth + " mois " + timeleftDays + " jours";
		}
		else
		{
			int timeleftDays = timeleftSecond / (3600 * 24);

			if (timeleftDays <= 1)
				return timeleftDays + " jour";
			else
				return timeleftDays + " jours";
		}
	}

	public static int getTimestampOfTodayAtMidnight()
	{
		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeInMillis(System.currentTimeMillis());
		int year = cal1.get(Calendar.YEAR);
		int month = cal1.get(Calendar.MONTH);
		int day = cal1.get(Calendar.DAY_OF_MONTH);

		Calendar cal2 = Calendar.getInstance();
		cal2.set(year, month, day, 0, 0, 0);

		return millisToTimestamp(cal2.getTimeInMillis());
	}

	public static int millisToTimestamp(long millis)
	{
		return (int) Math.floor((int) (millis / 1000));
	}

	public static int getScreenOrientation(Activity activity)
	{
		Display getOrient = activity.getWindowManager().getDefaultDisplay();
		int orientation = Configuration.ORIENTATION_UNDEFINED;
		if (getOrient.getWidth() == getOrient.getHeight())
		{
			orientation = Configuration.ORIENTATION_SQUARE;
		}
		else
		{
			if (getOrient.getWidth() < getOrient.getHeight())
			{
				orientation = Configuration.ORIENTATION_PORTRAIT;
			}
			else
			{
				orientation = Configuration.ORIENTATION_LANDSCAPE;
			}
		}
		return orientation;
	}

	public static String getMonth(int number)
	{
		String[] months = { "Janvier", "FŽvrier", "Mars", "Avril", "Mai", "Juin", "Juillet", "Aožt", "Septembre", "Octobre", "Novembre", "DŽcembre" };
		return months[number];
	}

	public static String durationToString(int duration)
	{
		String answer = "";
		String[] units = { "a", "m", "j", "h", "min" };
		int[] value = { 518400, 43200, 1440, 60, 1 };
		int i;

		for (i = 0; i < value.length; i++)
		{
			if (duration >= value[i])
			{
				int r = (duration / value[i]);
				answer += " " + r + " " + units[i];
				duration -= r * value[i];
			}
		}

		return answer;
	}
	
	public static Bitmap cropBitmap(Bitmap bitmap, int destWidth, int destHeight)
	{
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		float scaleWidth = ((float) destWidth) / width;
		float scaleHeight = ((float) destHeight) / height;

		if (scaleWidth < 1 && scaleHeight < 1) // Si l'image de destination est plus petite que l'original
		{
			bitmap = scaleDownBitmap(bitmap, destWidth, destHeight, true);

			width = bitmap.getWidth();
			height = bitmap.getHeight();
			
			int posX = (int) ((width - destWidth) / 2);
			int posY = (int) ((height - destHeight) / 2);

			return Bitmap.createBitmap(bitmap, posX, posY, destWidth, destHeight);
		}
		else
		{
			return bitmap;
		}
	}

	public static Bitmap scaleDownBitmap(Bitmap bitmap, int destWidth, int destHeight, boolean reverse)
	{
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		float scaleWidth = ((float) destWidth) / width;
		float scaleHeight = ((float) destHeight) / height;
		float ratio = ((float) width / height);

		if (scaleWidth < 1 && scaleHeight < 1)
		{
			if (!reverse)
			{
				if (scaleWidth < scaleHeight)
					destHeight = (int) (destWidth / ratio);
				else
					destWidth = (int) (destHeight * ratio);
			}
			else
			{
				if (scaleWidth < scaleHeight)
					destWidth = (int) (destHeight * ratio);
				else
					destHeight = (int) (destWidth / ratio);
			}
			return Bitmap.createScaledBitmap(bitmap, destWidth, destHeight, false);
		}
		else
		{
			return bitmap;
		}
	}

	public static File getExternalCacheDir(final Context context)
	{
		final File extCacheDir = new File(Environment.getExternalStorageDirectory(), "/Android/data/" + context.getApplicationInfo().packageName + "/cache/");
		extCacheDir.mkdirs();
		return extCacheDir;
	}
}
