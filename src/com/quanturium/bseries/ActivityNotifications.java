package com.quanturium.bseries;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.quanturium.bseries.adapters.NotificationsAdapter;
import com.quanturium.bseries.adapters.PlanningAdapter;
import com.quanturium.bseries.json.JsonEpisode;
import com.quanturium.bseries.json.JsonMember;
import com.quanturium.bseries.json.JsonNotification;
import com.quanturium.bseries.json.JsonShow;
import com.quanturium.bseries.sqlite.NotificationsDB;
import com.quanturium.bseries.tools.Prefs;

public class ActivityNotifications extends Activity
{
	private List<JsonNotification>	notifications	= new ArrayList<JsonNotification>();
	private NotificationsAdapter	notificationsAdapter;

	private ListView				listView;
	private RelativeLayout			contentScreen;
	private RelativeLayout			emptyviewScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notifications);
		setTitle("BSeries : Notifications");
		
		if (android.os.Build.VERSION.SDK_INT >= 14)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		this.contentScreen = (RelativeLayout) findViewById(R.id.contentScreen);
		this.emptyviewScreen = (RelativeLayout) findViewById(R.id.emptyviewScreen);
		this.listView = (ListView) findViewById(R.id.listView);

		NotificationsDB notificationsDB = new NotificationsDB(getApplicationContext());
		notificationsDB.open();
		this.notifications = notificationsDB.getAll();
		Prefs.getPreferences(getApplicationContext()).edit().putInt(Prefs.NOTIFICATIONS_COUNT, 0).commit();
		notificationsDB.deleteAll();
		notificationsDB.close();

		this.notificationsAdapter = new NotificationsAdapter(this, this.notifications);
		this.listView.setAdapter(this.notificationsAdapter);

		this.contentScreen.setVisibility(View.VISIBLE);
		this.listView.setEmptyView(emptyviewScreen);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

			case android.R.id.home:

				finish();

			break;
		}

		return true;
	}
}
