package com.quanturium.bseries.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class BSeriesDatabaseOpener extends SQLiteOpenHelper
{
	public static final int		DB_VERSION					= 1;
	public static final String	DB_NAME						= "notifications.db";

	public static final String	NOTIFICATIONS_TABLE			= "notifications";
	public static final String	NOTIFICATIONS_COL_ID		= "id";
	public static final String	NOTIFICATIONS_COL_CONTENT	= "content";
	public static final String	NOTIFICATIONS_COL_DATE		= "timestamp";

	private static final String	CREATE_TABLES				= "CREATE TABLE " + NOTIFICATIONS_TABLE + " (" + NOTIFICATIONS_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NOTIFICATIONS_COL_CONTENT + " TEXT NOT NULL, " + NOTIFICATIONS_COL_DATE + " INTEGER NOT NULL);";

	public BSeriesDatabaseOpener(Context context, String name, CursorFactory factory, int version)
	{
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(CREATE_TABLES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE " + NOTIFICATIONS_TABLE + ";");
		onCreate(db);
	}

}
