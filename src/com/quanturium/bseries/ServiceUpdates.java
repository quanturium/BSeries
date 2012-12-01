package com.quanturium.bseries;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.R.anim;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.quanturium.bseries.apis.APIBetaSeries;
import com.quanturium.bseries.apis.APIBetaSeriesAnswer;
import com.quanturium.bseries.json.JsonEpisode;
import com.quanturium.bseries.json.JsonNotification;
import com.quanturium.bseries.sqlite.NotificationsDB;
import com.quanturium.bseries.tools.Cache;
import com.quanturium.bseries.tools.Constant;
import com.quanturium.bseries.tools.Prefs;
import com.quanturium.bseries.tools.Tools;
import com.quanturium.bseries.widget.WidgetService;

public class ServiceUpdates extends IntentService
{
	public APIBetaSeries		BetaSeries	= APIBetaSeries.getInstance();
	private APIBetaSeriesAnswer	planningMemberAnswer;
	private APIBetaSeriesAnswer	memberEpisodesAnswer;
	private APIBetaSeriesAnswer	memberNotificationsAnswer;

	public ServiceUpdates()
	{
		super("ServiceUpdates");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		String action = intent.getAction();

		if (action == null)
			return;

		if (action.equals(Constant.ACTION_BETASERIES_DATA))
		{
			if (!Prefs.getPreferences(this).getString(Prefs.TOKEN, "").equals(""))
			{
				if (Prefs.getPreferences(this).getBoolean(Prefs.PLANNING_BADGE, true))
					planningMember();

				if (Prefs.getPreferences(this).getBoolean(Prefs.EPISODES_BADGE, false))
					memberEpisodes();

				if (Prefs.getPreferences(this).getBoolean(Prefs.NOTIFICATIONS_ENABLED, true))
					memberNotifications();
			}
		}
		else if (action.equals(Constant.ACTION_SERVICE_WIDGET_UPDATE))
		{
			Log.d("Intent", "UpdateServiceWidget");

			String data = "";

			if (Cache.isCachedString(getApplicationContext(), "string.planning.json", Cache.CACHE_TIME_WIDGET))
			{
				data = Cache.getCachedString(getApplicationContext(), "string.planning.json", Cache.CACHE_TIME_WIDGET);
			}
			else
			{
				planningMember();

				if (Cache.isCachedString(getApplicationContext(), "string.planning.json", Cache.CACHE_TIME_WIDGET))
				{
					data = Cache.getCachedString(getApplicationContext(), "string.planning.json", Cache.CACHE_TIME_WIDGET);
				}
			}

			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

			int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

			for (int widgetId : allWidgetIds)
			{
				Intent listviewIntent = new Intent(getApplicationContext(), WidgetService.class);
				listviewIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
				listviewIntent.putExtra("json", data);

				Intent headerIntent = new Intent(getApplicationContext(), ActivityHome.class);
				PendingIntent headerPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, headerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				RemoteViews widget = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_content);
				widget.setRemoteAdapter(widgetId, R.id.widgetListView, listviewIntent);
				widget.setViewVisibility(R.id.widgetListView, View.VISIBLE);
				widget.setViewVisibility(R.id.widgetLoading, View.GONE);
				widget.setOnClickPendingIntent(R.id.widgetHeader, headerPendingIntent);

				Intent clickIntent = new Intent(getApplicationContext(), ActivityEpisodeInfos.class);
				PendingIntent clickPI = PendingIntent.getActivity(getApplicationContext(), 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				widget.setPendingIntentTemplate(R.id.widgetListView, clickPI);

				appWidgetManager.updateAppWidget(widgetId, widget);
			}
		}
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	private void memberNotifications()
	{
		new Thread(new Runnable() {

			@Override
			public void run()
			{
				memberNotificationsAnswer = BetaSeries.memberNotifications(Prefs.getPreferences(getApplicationContext()).getString(Prefs.TOKEN, ""));

				if (memberNotificationsAnswer != null && memberNotificationsAnswer.json.root.code == 1)
				{
					if (memberNotificationsAnswer.json.root.notifications != null)
					{
						// // DUMMY DATA
						//
						// JsonNotification test = new JsonNotification();
						// test.html = "sqhfjkshfdjksh hdsjhf jskdhf jshdf jkshdjkf hjsdh jfs";
						// test.date = 1332193646;
						//
						// memberNotificationsAnswer.json.root.notifications.put(memberNotificationsAnswer.json.root.notifications.size(), test);
						// memberNotificationsAnswer.json.root.notifications.put(memberNotificationsAnswer.json.root.notifications.size(), test);
						// memberNotificationsAnswer.json.root.notifications.put(memberNotificationsAnswer.json.root.notifications.size(), test);
						//
						// // FIN DUMMY DATA

						if (memberNotificationsAnswer.json.root.notifications.size() > 0)
						{
							HashMap<Integer, JsonNotification> notifications = memberNotificationsAnswer.json.root.notifications;

							NotificationsDB notificationsDB = new NotificationsDB(getApplicationContext());
							notificationsDB.open();

							for (int i = 0; i < notifications.size(); i++)
							{
								notificationsDB.add(notifications.get(i));
							}

							Prefs.getPreferences(getApplicationContext()).edit().putInt(Prefs.NOTIFICATIONS_COUNT, notificationsDB.count()).commit();

							notificationsDB.close();
						}

						memberNotificationsAnswer = null;
					}
				}
			}
		}).start();
	}

	private void planningMember()
	{
		new Thread(new Runnable() {

			@Override
			public void run()
			{
				planningMemberAnswer = BetaSeries.planningMember("unseen", Prefs.getPreferences(getApplicationContext()).getString(Prefs.TOKEN, ""));

				if (planningMemberAnswer != null && planningMemberAnswer.json.root.code == 1)
				{
					if (planningMemberAnswer.json.root.planning != null)
					{
						Cache.setCachedString(getApplicationContext(), "string.planning.json", new Gson().toJson(planningMemberAnswer.json.root.planning));

						Prefs.getPreferences(getApplicationContext()).edit().putInt(Prefs.NB_PLANNING_UNSEEN, getCountUnseen(planningMemberAnswer.json.root.planning)).commit();
						sendBroadcast(new Intent(Constant.ACTION_UPDATE_BADGES));
						planningMemberAnswer = null;
					}

				}
			}
		}).start();
	}

	private void memberEpisodes()
	{
		new Thread(new Runnable() {

			@Override
			public void run()
			{
				memberEpisodesAnswer = BetaSeries.memberEpisodes("all", (Prefs.getPreferences(ServiceUpdates.this).getBoolean(Prefs.EPISODES_ONLYONE, false) ? "next" : "-1"), Prefs.getPreferences(ServiceUpdates.this).getString(Prefs.TOKEN, ""));

				if (memberEpisodesAnswer != null && memberEpisodesAnswer.json.root.code == 1)
				{

					if (memberEpisodesAnswer.json.root.episodes != null)
					{
						if (memberEpisodesAnswer.json.root.episodes.size() > 200)
						{
							Prefs.getPreferences(getApplicationContext()).edit().putBoolean(Prefs.EPISODES_ONLYONE, true).commit();
						}
						else
						{
							Prefs.getPreferences(getApplicationContext()).edit().putInt(Prefs.NB_EPISODES_UNSEEN, getCountUnseen(memberEpisodesAnswer.json.root.episodes)).commit();
							sendBroadcast(new Intent(Constant.ACTION_UPDATE_BADGES));
							memberEpisodesAnswer = null;
						}
					}
				}
				else if (memberEpisodesAnswer != null && memberEpisodesAnswer.errorNumber == 12)
				{
					Prefs.getPreferences(getApplicationContext()).edit().putBoolean(Prefs.EPISODES_ONLYONE, true).commit();
				}
			}
		}).start();
	}

	public int getCountUnseen(Map<Integer, JsonEpisode> data)
	{
		int v = 0;
		Iterator<Integer> iterator = data.keySet().iterator();

		int frenchTime = (Prefs.getPreferences(ServiceUpdates.this).getBoolean(Prefs.DECALLAGE_1_JOUR, false) ? (3600 * 24) : 0);

		while (iterator.hasNext())
		{
			int key = iterator.next();

			// DECALLAGE POUR CORRESPONDRE A LA DATE FRANCAISE
			data.get(key).date += frenchTime;

			if (data.get(key).date <= Tools.getTimestampOfTodayAtMidnight())
				v++;
		}

		return v;
	}

}
