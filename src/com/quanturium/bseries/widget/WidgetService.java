package com.quanturium.bseries.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService
{
	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		return (new WidgetListViewFactory(this.getApplicationContext(), intent));
	}
}
