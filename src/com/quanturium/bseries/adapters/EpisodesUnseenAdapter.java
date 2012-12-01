package com.quanturium.bseries.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quanturium.bseries.ActivityEpisodesUnseen;
import com.quanturium.bseries.R;
import com.quanturium.bseries.json.JsonEpisode;
import com.quanturium.bseries.tools.Tools;
import com.quanturium.bseries.viewholder.PlanningViewHolder;

public class EpisodesUnseenAdapter extends BaseAdapter
{

	List<JsonEpisode>		episodes;
	private LayoutInflater	inflater;
	ActivityEpisodesUnseen	activityEpisodesUnseen;

	public EpisodesUnseenAdapter(Context context, List<JsonEpisode> planning2, ActivityEpisodesUnseen activityEpisodesUnseen)
	{
		this.activityEpisodesUnseen = activityEpisodesUnseen;
		this.inflater = LayoutInflater.from(context);
		this.episodes = planning2;
	}

	@Override
	public int getCount()
	{
		return episodes.size();
	}

	@Override
	public Object getItem(int position)
	{
		return null;
	}
	
	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public void markAsSeen(int position, boolean isViewOnlyOne)
	{
		int i;
		String url = this.episodes.get(position).url;

		for (i = position; i >= 0; i--)
		{
			if (!this.episodes.get(i).isSeparator && this.episodes.get(i).url.equals(url))
			{
				if(isViewOnlyOne)
				{
					this.episodes.get(i).isViewOnlyOne = true;
					break;
				}
				else
					this.episodes.remove(i);
			}				
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
		return (episodes.get(position).isViewOnlyOne ? 0 : 1);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if(episodes.get(position).isViewOnlyOne)
		{
			View v = convertView;
			PlanningViewHolder holder = null;
			
			if (v == null)
			{
				holder = new PlanningViewHolder();
				v = inflater.inflate(R.layout.episode_onlyone_item, null);
				holder.title = (TextView) v.findViewById(R.id.itemEpisodeOnlyOne); // inutile vu que l'on ne modifie pas le titre
				v.setTag(holder);
			}
			else
			{
				holder = (PlanningViewHolder) v.getTag();
			}			
			
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
			holder.show.setText(episodes.get(position).show);
			holder.title.setText(episodes.get(position).title);
			holder.season.setText(String.format("%02d", episodes.get(position).season));
			holder.episode.setText(String.format("%02d", episodes.get(position).episode));
	
			int pl = holder.icon.getPaddingLeft();
			int pt = holder.icon.getPaddingTop();
			int pr = holder.icon.getPaddingRight();
			int pb = holder.icon.getPaddingBottom();
	
			if (episodes.get(position).date <= Tools.getTimestampOfTodayAtMidnight())
			{
				holder.icon.setImageResource(R.drawable.to_see);
				holder.icon.setOnClickListener(activityEpisodesUnseen);
				holder.icon.setBackgroundDrawable(activityEpisodesUnseen.getResources().getDrawable(R.drawable.episode_icon));
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
