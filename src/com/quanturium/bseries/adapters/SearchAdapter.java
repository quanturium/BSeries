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

import com.quanturium.bseries.ActivitySearch;
import com.quanturium.bseries.R;
import com.quanturium.bseries.json.JsonShow;
import com.quanturium.bseries.viewholder.ShowViewHolder;

public class SearchAdapter extends BaseAdapter
{

	private List<JsonShow>	shows	= new ArrayList<JsonShow>();
	private LayoutInflater	inflater;
	ActivitySearch			activitySearch;

	public SearchAdapter(Context context, List<JsonShow> shows, ActivitySearch activitySearch)
	{
		this.inflater = LayoutInflater.from(context);
		this.shows = shows;
		this.activitySearch = activitySearch;
	}

	@Override
	public int getCount()
	{
		return shows.size();
	}

	@Override
	public Object getItem(int position)
	{
		return shows.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ShowViewHolder holder;

		if (convertView == null)
		{
			holder = new ShowViewHolder();
			convertView = inflater.inflate(R.layout.show_item2, null);
			holder.title = (TextView) convertView.findViewById(R.id.itemShowTitle);
			holder.icon = (ImageView) convertView.findViewById(R.id.itemShowIcon);
			holder.icon2 = (ImageView) convertView.findViewById(R.id.itemShowIcon2);
			holder.archived = (TextView) convertView.findViewById(R.id.itemShowArchived);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ShowViewHolder) convertView.getTag();
		}

		holder.icon2.setTag(position);
		holder.title.setText(shows.get(position).title);

		int pl = holder.icon2.getPaddingLeft();
		int pt = holder.icon2.getPaddingTop();
		int pr = holder.icon2.getPaddingRight();
		int pb = holder.icon2.getPaddingBottom();

		if (shows.get(position).isAlreadyInTheUserAccount == true)
		{
			switch (shows.get(position).archive)
			{
				case 0:
					holder.archived.setText(R.string.show_notarchived);
					holder.icon.setImageResource(R.drawable.bullet_tv);

					holder.icon2.setVisibility(View.INVISIBLE);
					holder.icon2.setOnClickListener(null);
					holder.icon2.setBackgroundDrawable(null);
				break;

				case 1:
					holder.archived.setText(R.string.show_archived);
					holder.icon.setImageResource(R.drawable.bullet_archived);

					holder.icon2.setVisibility(View.INVISIBLE);
					holder.icon2.setOnClickListener(null);
					holder.icon2.setBackgroundDrawable(null);
				break;
			}
		}
		else
		{
			holder.archived.setText(R.string.show_notadded);
			holder.icon.setImageResource(R.drawable.bullet_blue);

			holder.icon2.setVisibility(View.VISIBLE);
			holder.icon2.setOnClickListener(activitySearch);
			holder.icon2.setBackgroundDrawable(activitySearch.getResources().getDrawable(R.drawable.episode_icon));
		}

		holder.icon2.setPadding(pl, pt, pr, pb);

		return convertView;
	}

}
