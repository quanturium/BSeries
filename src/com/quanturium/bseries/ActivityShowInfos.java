package com.quanturium.bseries;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.quanturium.bseries.json.JsonSeason2;
import com.quanturium.bseries.json.JsonShow;
import com.quanturium.bseries.tools.Cache;
import com.quanturium.bseries.tools.GAnalytics;

public class ActivityShowInfos<E> extends Activity implements OnClickListener
{

	private Handler				handler;

	private static final int	ACTION_BANNER_LOADED	= 3;

	private JsonShow			showJson;
	private Bitmap				bitmap;
	private RelativeLayout		showHeader;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_infos);

		Intent i = getIntent();
		Gson gson = new Gson();
		String temp = i.getStringExtra("json");

		if (temp != null)
			showJson = gson.fromJson(temp, JsonShow.class);
		else
			finish();

		setTitle("BSeries : Serie : " + showJson.title);

		if (android.os.Build.VERSION.SDK_INT >= 14)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		showHeader = (RelativeLayout) findViewById(R.id.showHeader);
		showHeader.setOnClickListener(this);

		setHandler();
		loadContent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show_infos, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.episodesSerie:
			case android.R.id.home:

				finish();

			break;
		}

		return true;
	}

	private void setShowImage(final String link, final String url)
	{
		if ((bitmap = Cache.getCachedBitmap(ActivityShowInfos.this, "bitmap." + url + ".jpg", Cache.CACHE_TIME_BITMAP)) != null)
		{
			handler.sendEmptyMessage(ACTION_BANNER_LOADED);
		}
		else
		{
			new Thread(new Runnable() {

				@Override
				public void run()
				{
					// TODO Auto-generated method stub

					URL urlImage;
					try
					{
						urlImage = new URL(link);
						HttpURLConnection connection = (HttpURLConnection) urlImage.openConnection();
						connection.setInstanceFollowRedirects(true);
						InputStream inputStream = connection.getInputStream();
						bitmap = BitmapFactory.decodeStream(inputStream);

						Cache.setCachedBitmap(ActivityShowInfos.this, "bitmap." + url + ".jpg", bitmap);

					}
					catch (MalformedURLException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banner_show_nopreview);
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banner_show_nopreview);
					}
					catch (NullPointerException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banner_show_nopreview);
					}
					finally
					{
						handler.sendEmptyMessage(ACTION_BANNER_LOADED);
					}

				}
			}).start();
		}
	}

	public int nbSeasons()
	{
		return showJson.seasons.size();
	}

	public int nbEpisodes()
	{
		int v = 0;

		Iterator<Integer> it = showJson.seasons.keySet().iterator();

		while (it.hasNext())
		{
			Object key = it.next();
			JsonSeason2 val = showJson.seasons.get(key);

			v += val.episodes;
		}

		return v;
	}

	public void loadContent()
	{
		if (showJson.banner != null)
			setShowImage(showJson.banner, showJson.url);
		else
		{
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banner_show_nopreview);
			ImageView banner = (ImageView) findViewById(R.id.showHeaderBanner);
			banner.setImageBitmap(bitmap);
			banner.setVisibility(View.VISIBLE);
		}

		TextView showTitle = (TextView) findViewById(R.id.itemEpisodeSeparatorTitle);
		TextView showStatus = (TextView) findViewById(R.id.showInfoStatus);
		TextView showNbSaisons = (TextView) findViewById(R.id.showInfoNbSaisons);
		TextView showNbEpisodes = (TextView) findViewById(R.id.showInfoNbEpisodes);
		TextView showDescription = (TextView) findViewById(R.id.showDescription);

		showTitle.setText(showJson.title);
		showNbSaisons.setText(String.valueOf(nbSeasons()));
		showNbEpisodes.setText(String.valueOf(nbEpisodes()));

		if (showJson.status != null && !showJson.status.equals(""))
			showStatus.setText(showJson.status);

		if (showJson.description != null && !showJson.description.equals(""))
			showDescription.setText(showJson.description);

	}

	private void setHandler()
	{
		handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg)
			{

				switch (msg.what)
				{

					case ACTION_BANNER_LOADED:

						ImageView background = (ImageView) findViewById(R.id.showHeaderBackground);
						ImageView banner = (ImageView) findViewById(R.id.showHeaderBanner);

						banner.setImageBitmap(bitmap);

						background.setVisibility(View.VISIBLE);
						banner.setVisibility(View.VISIBLE);

					break;

				}
			};
		};
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		GAnalytics.getInstance().Track(this, "/show/infos?url=" + showJson.url);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.showHeader:

				finish();
				
			break;
		}

	}
}
