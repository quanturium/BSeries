package com.quanturium.bseries.tools;

import android.content.Context;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class GAnalytics
{
	private static volatile GAnalytics	instance	= null;
	private final String				UA			= "UA-28301626-1";
	private GoogleAnalyticsTracker		tracker		= null;

	private GAnalytics()
	{
	}

	public final static GAnalytics getInstance()
	{
		if (GAnalytics.instance == null)
		{
			synchronized (GAnalytics.class)
			{
				if (GAnalytics.instance == null)
				{
					GAnalytics.instance = new GAnalytics();
				}
			}
		}

		return GAnalytics.instance;
	}

	public void Track(Context context, String url)
	{
		if (tracker == null)
		{
			tracker = GoogleAnalyticsTracker.getInstance();
			tracker.startNewSession(this.UA, 30, context);
		}

		tracker.trackPageView(url);

		Log.i("tracker", url);
	}
}
