package com.quanturium.bseries.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quanturium.bseries.HomeItem;
import com.quanturium.bseries.R;
import com.quanturium.bseries.tools.Prefs;
import com.quanturium.bseries.viewholder.HomeViewHolder;

public class HomeAdapter extends BaseAdapter
{

	public List<HomeItem>	items	= new ArrayList<HomeItem>();
	private LayoutInflater	inflater;
	private Context			context;

	public HomeAdapter(Context context, List<HomeItem> items)
	{
		this.items = items;
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{

		View v = convertView;
		HomeViewHolder holder = null;

		if (v == null)
		{
			holder = new HomeViewHolder();
			v = inflater.inflate(R.layout.home_item, null);
			holder.title = (TextView) v.findViewById(R.id.homeItemTitle);
			holder.icon = (ImageView) v.findViewById(R.id.homeItemIcon);
			holder.badge = (TextView) v.findViewById(R.id.homeItemBadge);
			v.setTag(holder);
		}
		else
		{
			holder = (HomeViewHolder) v.getTag();
		}

		holder.badge.setVisibility(View.INVISIBLE);

		if (items.get(position).badge != 0)
		{
			if ((position == 0 && Prefs.getPreferences(context).getBoolean(Prefs.PLANNING_BADGE, true)) || (position == 1 && Prefs.getPreferences(context).getBoolean(Prefs.EPISODES_BADGE, true)))
			{
				holder.badge.setVisibility(View.VISIBLE);
				holder.badge.setText(String.valueOf(items.get(position).badge));
			}
		}

		holder.title.setText(items.get(position).title);
		holder.icon.setImageResource(items.get(position).icon);

		return v;

	}
}
