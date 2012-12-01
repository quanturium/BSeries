package com.quanturium.bseries.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quanturium.bseries.ActivityPlanning;
import com.quanturium.bseries.R;
import com.quanturium.bseries.json.JsonEpisode;
import com.quanturium.bseries.tools.Tools;
import com.quanturium.bseries.viewholder.PlanningViewHolder;

public class PlanningAdapter extends BaseAdapter
{

	List<JsonEpisode>		planning;
	private LayoutInflater	inflater;
	ActivityPlanning		activityPlanning;

	public PlanningAdapter(Context context, List<JsonEpisode> planning2, ActivityPlanning activityPlanning)
	{
		this.activityPlanning = activityPlanning;
		this.inflater = LayoutInflater.from(context);
		this.planning = planning2;
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return planning.size();
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
	public boolean areAllItemsEnabled()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled(int position)
	{
		// TODO Auto-generated method stub
		return !planning.get(position).isSeparator;
	}

	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		return (planning.get(position).isSeparator ? 0 : 1);
	}

	public void markAsSeen(int position)
	{
		int i;
		String url = this.planning.get(position).url;

		for (i = position; i >= 0; i--)
		{
			if (!this.planning.get(i).isSeparator && this.planning.get(i).url.equals(url))
				this.planning.remove(i);
		}

		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (planning.get(position).isSeparator)
		{
			View v = convertView;
			PlanningViewHolder holder = null;

			if (v == null)
			{
				holder = new PlanningViewHolder();
				v = inflater.inflate(R.layout.episode_separator, null);
				holder.title = (TextView) v.findViewById(R.id.itemEpisodeSeparatorTitle);
				v.setTag(holder);
			}
			else
			{
				holder = (PlanningViewHolder) v.getTag();
			}

			if (planning.get(position).date > Tools.getTimestampOfTodayAtMidnight())
				holder.title.setText("Dans " + Tools.timestamp2timeleft(Integer.valueOf(planning.get(position).date)));
			else if (planning.get(position).date == Tools.getTimestampOfTodayAtMidnight())
			{

				holder.title.setText("Aujourd'hui");
			}
			else
				holder.title.setText(Tools.timestamp2date(Integer.valueOf(planning.get(position).date)));

			return v;
		}
		else
		{
			View v = convertView;
			PlanningViewHolder holder = null;

			if (v == null)
			{
				holder = new PlanningViewHolder();
				v = inflater.inflate(R.layout.episode_item, null);
				holder.show = (TextView) v.findViewById(R.id.itemEpisodeTitle);
				holder.title = (TextView) v.findViewById(R.id.itemEpisodeDate);
				holder.season = (TextView) v.findViewById(R.id.itemEpisodeRight1Text);
				holder.episode = (TextView) v.findViewById(R.id.itemEpisodeRight2Text);
				holder.icon = (ImageView) v.findViewById(R.id.itemEpisodeIcon);
				v.setTag(holder);
			}
			else
			{
				holder = (PlanningViewHolder) v.getTag();
			}

			holder.icon.setTag(position);
			holder.show.setText(planning.get(position).show);
			holder.title.setText(planning.get(position).title);
			holder.season.setText(String.format("%02d", planning.get(position).season));
			holder.episode.setText(String.format("%02d", planning.get(position).episode));

			int pl = holder.icon.getPaddingLeft();
			int pt = holder.icon.getPaddingTop();
			int pr = holder.icon.getPaddingRight();
			int pb = holder.icon.getPaddingBottom();

			if (planning.get(position).date <= Tools.getTimestampOfTodayAtMidnight())
			{
				holder.icon.setImageResource(R.drawable.to_see);
				holder.icon.setOnClickListener(activityPlanning);
				holder.icon.setBackgroundDrawable(activityPlanning.getResources().getDrawable(R.drawable.episode_icon));
			}
			else
			{
				holder.icon.setImageResource(R.drawable.not_aired_yet);
				holder.icon.setOnClickListener(null);
				holder.icon.setBackgroundDrawable(null);
			}

			holder.icon.setPadding(pl, pt, pr, pb);

			return v;

		}

	}

}
