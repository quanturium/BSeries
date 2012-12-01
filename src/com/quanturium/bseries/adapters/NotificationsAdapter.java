package com.quanturium.bseries.adapters;

import java.util.List;

import com.quanturium.bseries.ActivityPlanning;
import com.quanturium.bseries.json.JsonEpisode;
import com.quanturium.bseries.json.JsonNotification;
import com.quanturium.bseries.tools.Tools;
import com.quanturium.bseries.viewholder.NotificationViewHolder;
import com.quanturium.bseries.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NotificationsAdapter extends BaseAdapter
{
	private List<JsonNotification>	notifications;
	private LayoutInflater			inflater;
	private Context					context;

	public NotificationsAdapter(Context context, List<JsonNotification> notifications)
	{
		this.context = context;
		this.notifications = notifications;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount()
	{
		return this.notifications.size();
	}

	@Override
	public Object getItem(int position)
	{
		return this.notifications.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{		
		NotificationViewHolder viewHolder = null;
		
		if(convertView == null)
		{
			viewHolder = new NotificationViewHolder();
			convertView = this.inflater.inflate(R.layout.notification_item, parent, false);	
			
			viewHolder.html = (TextView) convertView.findViewById(R.id.textViewHtml);
			viewHolder.date = (TextView) convertView.findViewById(R.id.textViewDate);
			
			convertView.setTag(viewHolder);
		}
		else
		{
			viewHolder = (NotificationViewHolder) convertView.getTag();
		}
		
		viewHolder.html.setText(this.notifications.get(position).html);
		viewHolder.date.setText(Tools.timestamp2dateAndTime(this.notifications.get(position).date));
		
		return convertView;
	}

}
