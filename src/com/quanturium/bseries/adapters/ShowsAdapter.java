package com.quanturium.bseries.adapters;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quanturium.bseries.R;
import com.quanturium.bseries.apis.APIBetaSeries;
import com.quanturium.bseries.json.JsonShow;
import com.quanturium.bseries.tools.Cache;
import com.quanturium.bseries.tools.Tools;
import com.quanturium.bseries.viewholder.ShowViewHolder;

public class ShowsAdapter extends BaseAdapter
{

	public List<JsonShow>									shows;
	private LayoutInflater									inflater;
	private Context											context;
	private List<Bitmap>									bitmaps		= new ArrayList<Bitmap>();
	private List<AsyncTask<Void, Void, Bitmap>>	asyncTask	= new ArrayList<AsyncTask<Void, Void, Bitmap>>();

	public ShowsAdapter(Context context, List<JsonShow> shows)
	{
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.shows = shows;

		for (int i = 0; i < shows.size(); i++)
		{
			bitmaps.add(null);
			asyncTask.add(null);
		}			
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return shows.size();
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return shows.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub

		final ShowViewHolder holder;

		if (convertView == null)
		{
			holder = new ShowViewHolder();
			convertView = inflater.inflate(R.layout.show_item, null);
			holder.title = (TextView) convertView.findViewById(R.id.itemShowTitle);
			holder.icon = (ImageView) convertView.findViewById(R.id.itemShowIcon);
			holder.archived = (TextView) convertView.findViewById(R.id.itemShowArchived);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBarShow);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ShowViewHolder) convertView.getTag();
		}

		holder.title.setText(shows.get(position).title);
		holder.position = position;

		if (shows.get(position).archive == 1)
			holder.archived.setText(R.string.show_archived);
		else
			holder.archived.setText(R.string.show_notarchived);

		final String url = shows.get(position).url;

		Bitmap bitmap;
		if ((bitmap = bitmaps.get(position)) != null)
		{
			holder.icon.setImageBitmap(bitmap);
			holder.icon.setVisibility(View.VISIBLE);
			holder.progressBar.setVisibility(View.GONE);
		}			
		else if(asyncTask.get(position) == null)
		{
			holder.icon.setVisibility(View.GONE);
			holder.progressBar.setVisibility(View.VISIBLE);
			
			Log.e("asyntask", shows.get(position).url);

			asyncTask.set(position, new AsyncTask<Void, Void, Bitmap>() {
				private APIBetaSeries	BetaSeries	= APIBetaSeries.getInstance();

				@Override
				protected Bitmap doInBackground(Void... params)
				{
					Bitmap bitmap2 = null;

					if ((bitmap2 = Cache.getCachedBitmap(context, "thumbnail." + url + ".jpg", Cache.CACHE_TIME_BITMAP)) == null)
					{

						if ((bitmap2 = Cache.getCachedBitmap(context, "bitmap." + url + ".jpg", Cache.CACHE_TIME_BITMAP)) == null)
						{
							URL urlImage;

							try
							{
								urlImage = new URL(BetaSeries.uriPictureShows(url));
								HttpURLConnection connection = (HttpURLConnection) urlImage.openConnection();
								connection.setInstanceFollowRedirects(true);
								InputStream inputStream = connection.getInputStream();
								bitmap2 = BitmapFactory.decodeStream(inputStream);

								Cache.setCachedBitmap(context, "bitmap." + url + ".jpg", bitmap2);
								
								int image_size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
								bitmap2 = Tools.cropBitmap(bitmap2, image_size, image_size);

								Cache.setCachedBitmap(context, "thumbnail." + url + ".jpg", bitmap2);
							}
							catch (MalformedURLException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
								bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.banner_show_nopreview);
							}
							catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
								bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.banner_show_nopreview);
							}
							catch (NullPointerException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
								bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.banner_show_nopreview);
							}

							return bitmap2;
						}
						else
						{
							int image_size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
							bitmap2 = Tools.cropBitmap(bitmap2, image_size, image_size);
							Cache.setCachedBitmap(context, "thumbnail." + url + ".jpg", bitmap2);
							return bitmap2;
						}
					}
					else
					{
						return bitmap2;
					}
				}

				@Override
				protected void onPostExecute(Bitmap result)
				{										
					bitmaps.set(position, result);
					
					Log.e("PostExec","position:"+position+";url:"+url);
					
					if (holder.position == position)
					{						
						Log.e("SetIcon","position:"+position+";url:"+url);
						
						holder.icon.setVisibility(View.VISIBLE);
						holder.progressBar.setVisibility(View.GONE);
						holder.icon.setImageBitmap(result);
					}
				}
			}.execute());
		}
		else
		{
			holder.icon.setVisibility(View.GONE);
			holder.progressBar.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

	public void removeItem(int i)
	{
		shows.remove(i);
		bitmaps.remove(i);
		notifyDataSetChanged();
	}

	public void archiveItem(int i)
	{
		if (shows.get(i).archive == 0)
			shows.get(i).archive = 1;

		notifyDataSetChanged();
	}

	public void unarchiveItem(int i)
	{
		if (shows.get(i).archive == 1)
			shows.get(i).archive = 0;

		notifyDataSetChanged();
	}
}
