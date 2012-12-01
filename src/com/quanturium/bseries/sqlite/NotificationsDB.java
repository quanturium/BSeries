package com.quanturium.bseries.sqlite;

import java.util.ArrayList;
import java.util.List;

import com.quanturium.bseries.json.JsonNotification;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;

public class NotificationsDB
{	
	private Context					context;
	private SQLiteDatabase			DB;
	private BSeriesDatabaseOpener	bSeriesDatabaseOpener;

	public NotificationsDB(Context context)
	{
		this.context = context;
		this.bSeriesDatabaseOpener = new BSeriesDatabaseOpener(this.context, BSeriesDatabaseOpener.DB_NAME, null, BSeriesDatabaseOpener.DB_VERSION);
	}

	public void open()
	{
		this.DB = this.bSeriesDatabaseOpener.getWritableDatabase();
	}

	public void close()
	{
		this.DB.close();
	}

	public SQLiteDatabase getDB()
	{
		return this.DB;
	}
	
	public long add(JsonNotification jsonNotification)
	{
		ContentValues values = new ContentValues();				 
		
		values.put(BSeriesDatabaseOpener.NOTIFICATIONS_COL_CONTENT, Html.fromHtml(jsonNotification.html).toString());
		values.put(BSeriesDatabaseOpener.NOTIFICATIONS_COL_DATE, jsonNotification.date);
		
		return this.DB.insert(BSeriesDatabaseOpener.NOTIFICATIONS_TABLE, null, values);		
	}
	
	public void deleteAll()
	{
		this.DB.delete(BSeriesDatabaseOpener.NOTIFICATIONS_TABLE, null, null);
	}
	
	public List<JsonNotification> getAll()
	{
		List<JsonNotification> notifications = new ArrayList<JsonNotification>();
		
		Cursor result = this.DB.rawQuery("select * from " + BSeriesDatabaseOpener.NOTIFICATIONS_TABLE , null);
		result.moveToFirst();
		
		while(!result.isAfterLast())
		{
			JsonNotification notification = new JsonNotification();
			notification.html = result.getString(1);
			notification.date = result.getInt(2);
			
			notifications.add(notification);
			
			result.moveToNext();
		}
		
		return notifications;
	}
	
	public int count()
	{
		Cursor dataCount = this.DB.rawQuery("select count(*) from " + BSeriesDatabaseOpener.NOTIFICATIONS_TABLE , null);
        dataCount.moveToFirst();
        int jcount = dataCount.getInt(0);
        dataCount.close();
        return jcount;
	}
}
