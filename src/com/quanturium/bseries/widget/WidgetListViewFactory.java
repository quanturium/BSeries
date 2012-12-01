package com.quanturium.bseries.widget;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quanturium.bseries.ActivityPlanning;
import com.quanturium.bseries.R;
import com.quanturium.bseries.json.JsonEpisode;
import com.quanturium.bseries.tools.Prefs;
import com.quanturium.bseries.tools.Tools;

public class WidgetListViewFactory implements RemoteViewsService.RemoteViewsFactory
{
	List<JsonEpisode>	items	= new ArrayList<JsonEpisode>();
	private Context		context	= null;

	// private int appWidgetId;

	public WidgetListViewFactory(Context context, Intent intent)
	{
		this.context = context;
		// appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		Type type = new TypeToken<HashMap<Integer, JsonEpisode>>() {
		}.getType();

		HashMap<Integer, JsonEpisode> items = new Gson().fromJson(intent.getStringExtra("json"), type);
		Map<Integer, List<JsonEpisode>> itemsSorted = new HashMap<Integer, List<JsonEpisode>>();
		List<Integer> keys = new ArrayList<Integer>();
		JsonEpisode separator = null;

		int i, j, k;
		boolean hideNotAired = Prefs.getPreferences(context).getBoolean(Prefs.PLANNING_HIDE_NOTAIRED, false);
		int frenchTime = (Prefs.getPreferences(context).getBoolean(Prefs.DECALLAGE_1_JOUR, false) ? (3600 * 24) : 0);

		for (i = 0; i < items.size(); i++)
		{
			// DECALLAGE POUR CORRESPONDRE A LA DATE FRANCAISE
			items.get(i).date += frenchTime;

			if (items.get(i).date > Tools.getTimestampOfTodayAtMidnight() && hideNotAired)
				continue;

			if (!itemsSorted.containsKey(items.get(i).date))
			{
				itemsSorted.put(items.get(i).date, new ArrayList<JsonEpisode>());
				keys.add(items.get(i).date);
			}

			itemsSorted.get(items.get(i).date).add(items.get(i));
		}

		k = 0;
		
		outerloop:
		for (i = 0; i < itemsSorted.size(); i++)
		{
			if(k >= 10)
				break outerloop;
			
			separator = new JsonEpisode();
			separator.isSeparator = true;
			separator.date = itemsSorted.get(keys.get(i)).get(0).date;
			this.items.add(separator);
			
			for (j = 0; j < itemsSorted.get(keys.get(i)).size(); j++)
			{									
				this.items.add(itemsSorted.get(keys.get(i)).get(j));
				k++;
			}
		}
	}

	@Override
	public void onCreate()
	{
		// no-op
	}

	@Override
	public void onDestroy()
	{
		// no-op
	}

	@Override
	public int getCount()
	{
		return (items.size());
	}

	@Override
	public RemoteViews getViewAt(int position)
	{
		if (position < 0 || position >= getCount())
		{
			return null;
		}
		
		RemoteViews row;

		if (items.get(position).isSeparator)
		{
			row = new RemoteViews(context.getPackageName(), R.layout.widget_separator);
			
			if (items.get(position).date > Tools.getTimestampOfTodayAtMidnight())
				row.setTextViewText(R.id.widgetItemSeparator,"Dans ".toUpperCase() + Tools.timestamp2timeleft(Integer.valueOf(items.get(position).date)).toUpperCase());
			else if (items.get(position).date == Tools.getTimestampOfTodayAtMidnight())
			{

				row.setTextViewText(R.id.widgetItemSeparator,"Aujourd'hui".toUpperCase());
			}
			else
				row.setTextViewText(R.id.widgetItemSeparator,Tools.timestamp2date(Integer.valueOf(items.get(position).date)).toUpperCase());
		}
		else
		{

			row = new RemoteViews(context.getPackageName(), R.layout.widget_item);

			row.setTextViewText(R.id.widgetItemShow, items.get(position).show);
			row.setTextViewText(R.id.widgetItemTitle, items.get(position).title);
			row.setTextViewText(R.id.widgetItemEpisodeRight1Text, String.format("%02d", items.get(position).season));
			row.setTextViewText(R.id.widgetItemEpisodeRight2Text, String.format("%02d", items.get(position).episode));

			Intent intent = new Intent();
			intent.putExtra("json", new Gson().toJson(items.get(position), JsonEpisode.class));
			intent.putExtra("url", items.get(position).url);
			intent.putExtra("title", items.get(position).show);
			intent.putExtra("archived", false);

			row.setOnClickFillInIntent(R.id.widgetItemLayout, intent);

		}
		
		return (row);
	}

	@Override
	public RemoteViews getLoadingView()
	{
		return null;
	}

	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public long getItemId(int position)
	{
		return (position);
	}

	@Override
	public boolean hasStableIds()
	{
		return (true);
	}

	@Override
	public void onDataSetChanged()
	{
		// no-op
	}
}
