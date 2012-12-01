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

import com.quanturium.bseries.ActivityEpisodes;
import com.quanturium.bseries.R;
import com.quanturium.bseries.json.JsonEpisode;
import com.quanturium.bseries.tools.Tools;
import com.quanturium.bseries.viewholder.EpisodeViewHolder;

public class EpisodesAdapter extends BaseAdapter
{

	private List<JsonEpisode>	episodes	= new ArrayList<JsonEpisode>();
	private LayoutInflater		inflater;
	ActivityEpisodes			activityEpisodes;

	public EpisodesAdapter(Context context, List<JsonEpisode> episodes, ActivityEpisodes activityEpisodes)
	{
		this.activityEpisodes = activityEpisodes;
		this.inflater = LayoutInflater.from(context);
		this.episodes = episodes;
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return episodes.size();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return episodes.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
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
		return !episodes.get(position).isSeparator;
	}

	public void markAsSeen(int position)
	{
		int i;

		for (i = position; i <= this.episodes.size() - 1; i++)
		{
			this.episodes.get(i).has_seen = 1;
		}

		notifyDataSetChanged();
	}

	public void markAsUnSeen(int position)
	{
		int i;

		for (i = 0; i <= position; i++)
		{
			this.episodes.get(i).has_seen = 0;
		}

		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		return (episodes.get(position).isSeparator ? 0 : 1);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub

		if (episodes.get(position).isSeparator)
		{
			View v = convertView;
			EpisodeViewHolder holder = null;

			if (v == null)
			{
				holder = new EpisodeViewHolder();
				v = inflater.inflate(R.layout.episode_separator, null);
				holder.title = (TextView) v.findViewById(R.id.itemEpisodeSeparatorTitle);
				v.setTag(holder);
			}
			else
			{
				holder = (EpisodeViewHolder) v.getTag();
			}

			holder.title.setText(episodes.get(position).title);

			return v;
		}
		else
		{
			View v2 = convertView;
			EpisodeViewHolder holder2 = null;

			if (v2 == null)
			{
				holder2 = new EpisodeViewHolder();
				v2 = inflater.inflate(R.layout.episode_item, null);
				holder2.title = (TextView) v2.findViewById(R.id.itemEpisodeTitle);
				holder2.date = (TextView) v2.findViewById(R.id.itemEpisodeDate);
				holder2.season = (TextView) v2.findViewById(R.id.itemEpisodeRight1Text);
				holder2.episode = (TextView) v2.findViewById(R.id.itemEpisodeRight2Text);
				holder2.icon = (ImageView) v2.findViewById(R.id.itemEpisodeIcon);
				v2.setTag(holder2);
			}
			else
			{
				holder2 = (EpisodeViewHolder) v2.getTag();
			}

			holder2.icon.setTag(position);
			holder2.title.setText(episodes.get(position).title);
			holder2.season.setText(String.format("%02d", episodes.get(position).season));
			holder2.episode.setText(String.format("%02d", episodes.get(position).episode));

			int pl = holder2.icon.getPaddingLeft();
			int pt = holder2.icon.getPaddingTop();
			int pr = holder2.icon.getPaddingRight();
			int pb = holder2.icon.getPaddingBottom();

			if (episodes.get(position).has_seen == 1)
			{
				holder2.icon.setImageResource(R.drawable.seen);
				holder2.icon.setOnClickListener(activityEpisodes);
				holder2.icon.setBackgroundDrawable(activityEpisodes.getResources().getDrawable(R.drawable.episode_icon));
			}
			else if (episodes.get(position).date <= Tools.getTimestampOfTodayAtMidnight())
			{
				holder2.icon.setImageResource(R.drawable.to_see);
				holder2.icon.setOnClickListener(activityEpisodes);
				holder2.icon.setBackgroundDrawable(activityEpisodes.getResources().getDrawable(R.drawable.episode_icon));
			}
			else
			{
				holder2.icon.setImageResource(R.drawable.not_aired_yet);
				holder2.icon.setOnClickListener(null);
				holder2.icon.setBackgroundDrawable(null);
			}

			holder2.icon.setPadding(pl, pt, pr, pb);

			if (episodes.get(position).date > Tools.getTimestampOfTodayAtMidnight())
				holder2.date.setText("Dans " + Tools.timestamp2timeleft(Integer.valueOf(episodes.get(position).date)));
			else if (episodes.get(position).date == Tools.getTimestampOfTodayAtMidnight())
				holder2.date.setText("Aujourd'hui");
			else
				holder2.date.setText(Tools.timestamp2date(Integer.valueOf(episodes.get(position).date)));

			return v2;
		}
	}
}
