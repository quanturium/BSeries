package com.quanturium.bseries;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.quanturium.bseries.adapters.EpisodesAdapter;
import com.quanturium.bseries.apis.APIBetaSeries;
import com.quanturium.bseries.apis.APIBetaSeriesAnswer;
import com.quanturium.bseries.apis.Social;
import com.quanturium.bseries.json.JsonEpisode;
import com.quanturium.bseries.json.JsonSeason;
import com.quanturium.bseries.json.JsonShow;
import com.quanturium.bseries.tools.Cache;
import com.quanturium.bseries.tools.GAnalytics;
import com.quanturium.bseries.tools.Prefs;
import com.quanturium.bseries.tools.Tools;

public class ActivityEpisodes extends Activity implements OnItemClickListener, OnClickListener
{
	private Handler				handler;

	public APIBetaSeries		BetaSeries				= APIBetaSeries.getInstance();

	private List<JsonEpisode>	episodes				= new ArrayList<JsonEpisode>();
	private EpisodesAdapter		episodesAdapter;

	private ListView			listView;
	private RelativeLayout		loadingScreen;
	private RelativeLayout		contentScreen;
	private RelativeLayout		emptyviewScreen;

	Bitmap						bitmap;

	private JsonShow			showJson;
	private APIBetaSeriesAnswer	ShowDisplayAnswer;
	private APIBetaSeriesAnswer	showEpisodesAnswer;

	private static final int	ERROR_DISPLAY			= -2;
	private static final int	ERROR_EPISODES			= -1;
	private static final int	ACTION_SHOW_LOADED		= 0;
	private static final int	ACTION_BANNER_LOADED	= 3;
	private int					action_left				= 2;

	private static final int	SCREEN_LOADER			= 1;
	private static final int	SCREEN_CONTENT			= 2;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_episodes);

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

		loadingScreen = (RelativeLayout) findViewById(R.id.loadingScreen);
		contentScreen = (RelativeLayout) findViewById(R.id.contentScreen);
		emptyviewScreen = (RelativeLayout) findViewById(R.id.emptyviewScreen);

		listView = (ListView) findViewById(R.id.listView);

		View headerView = View.inflate(this, R.layout.show_header, null);
		headerView.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		listView.addHeaderView(headerView, "", true);
		listView.setOnItemClickListener(this);

		registerForContextMenu(listView);

		setHandler();

		showDisplay();
		showEpisodes();

		Prefs.getPreferences(this).edit().putBoolean(Prefs.NEED_RELOAD_EPISODES, false).commit();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		GAnalytics.getInstance().Track(this, "/show/episodes?url=" + showJson.url);

		if (Prefs.getPreferences(this).getBoolean(Prefs.NEED_RELOAD_EPISODES, false))
		{
			Prefs.getPreferences(this).edit().putBoolean(Prefs.NEED_RELOAD_EPISODES, false).commit();

			action_left = 2;
			showDisplay();
			showEpisodes();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.episodes, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (contentScreen.getVisibility() == View.VISIBLE)
		{
			menu.findItem(R.id.infosSerie).setEnabled(true);
		}
		else
		{
			menu.findItem(R.id.infosSerie).setEnabled(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.infosSerie:

				Intent intent = new Intent(this, ActivityShowInfos.class);
				intent.putExtra("json", new Gson().toJson(ShowDisplayAnswer.json.root.show, JsonShow.class));
				startActivity(intent);

			break;

			case android.R.id.home:

				finish();

			break;
		}

		return true;
	}

	private void showDisplay()
	{
		((TextView) loadingScreen.getChildAt(0)).setText(R.string.loading_default);
		setScreen(SCREEN_LOADER);

		new Thread(new Runnable() {

			@Override
			public void run()
			{

				ShowDisplayAnswer = BetaSeries.showDisplay(showJson.url, Prefs.getPreferences(ActivityEpisodes.this).getString(Prefs.TOKEN, ""));

				if (ShowDisplayAnswer.json.root.code == 1)
				{
					handler.sendEmptyMessage(ACTION_SHOW_LOADED);
				}
				else
				{
					handler.sendEmptyMessage(ERROR_DISPLAY);
				}
			}
		}).start();
	}

	private void showEpisodes()
	{
		((TextView) loadingScreen.getChildAt(0)).setText(R.string.loading_default);
		setScreen(SCREEN_LOADER);

		new Thread(new Runnable() {

			@Override
			public void run()
			{

				showEpisodesAnswer = BetaSeries.showEpisodes(showJson.url, Prefs.getPreferences(ActivityEpisodes.this).getString(Prefs.TOKEN, ""), 1);

				if (showEpisodesAnswer.json.root.code == 1)
				{
					handler.sendEmptyMessage(ACTION_SHOW_LOADED);
				}
				else
				{
					handler.sendEmptyMessage(ERROR_EPISODES);
				}
			}
		}).start();
	}

	private void setShowImage(final String link, final String url)
	{
		if ((bitmap = Cache.getCachedBitmap(ActivityEpisodes.this, "bitmap." + url + ".jpg", Cache.CACHE_TIME_BITMAP)) != null)
		{
			handler.sendEmptyMessage(ACTION_BANNER_LOADED);
		}
		else
		{
			new Thread(new Runnable() {

				@Override
				public void run()
				{

					URL urlImage;
					try
					{
						urlImage = new URL(link);
						HttpURLConnection connection = (HttpURLConnection) urlImage.openConnection();
						connection.setInstanceFollowRedirects(true);
						InputStream inputStream = connection.getInputStream();
						bitmap = BitmapFactory.decodeStream(inputStream);

						Cache.setCachedBitmap(ActivityEpisodes.this, "bitmap." + url + ".jpg", bitmap);

					}
					catch (MalformedURLException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					handler.sendEmptyMessage(ACTION_BANNER_LOADED);

				}
			}).start();
		}
	}

	private void memberWatched(final JsonEpisode jsonEpisode, final int rate)
	{
		Social social = new Social(this);

		if (!Prefs.getPreferences(this).getString(Prefs.SOCIAL_FACEBOOK_NAME, "").equals(""))
			social.addNetwork(Social.FACEBOOK);

		if (!Prefs.getPreferences(this).getString(Prefs.SOCIAL_TWITTER_NAME, "").equals(""))
			social.addNetwork(Social.TWITTER);

		social.publish("Je viens de regarder cet épisode :", jsonEpisode.number + " " + jsonEpisode.title, jsonEpisode.show, "", "http://api.betaseries.com/pictures/episode/" + jsonEpisode.url + ".jpg?season=" + jsonEpisode.season + "&episode=" + jsonEpisode.episode, "https://www.betaseries.com/episode/" + jsonEpisode.url + "/" + jsonEpisode.number);

		new Thread(new Runnable() {

			@Override
			public void run()
			{

				APIBetaSeriesAnswer answer = BetaSeries.memberWatched(showJson.url, "" + jsonEpisode.season, "" + jsonEpisode.episode, rate, Prefs.getPreferences(ActivityEpisodes.this).getString(Prefs.TOKEN, ""));

				if (answer.json.root.code != 1)
				{
					handler.post(new Runnable() {

						@Override
						public void run()
						{
							Toast.makeText(ActivityEpisodes.this, "Echec lors du marquage de l'épisode comme lu / non lu", Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}

	private void setScreen(int screen)
	{
		switch (screen)
		{
			case SCREEN_CONTENT:

				loadingScreen.setVisibility(View.GONE);
				contentScreen.setVisibility(View.VISIBLE);
				listView.setEmptyView(emptyviewScreen);

			break;

			case SCREEN_LOADER:

				contentScreen.setVisibility(View.GONE);
				loadingScreen.setVisibility(View.VISIBLE);

			break;
		}
		
		if (android.os.Build.VERSION.SDK_INT >= 11)
			invalidateOptionsMenu();
	}

	private void setHandler()
	{
		handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg)
			{

				switch (msg.what)
				{
					case ERROR_DISPLAY:

						action_left = 2;

						if (ShowDisplayAnswer.errorNumber == 10)
						{

							if (!isFinishing())
							{
								AlertDialog alertDialog = new AlertDialog.Builder(ActivityEpisodes.this).create();
								alertDialog.setMessage(getString(R.string.error_connexion));
								alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										finish();
									}
								});
								alertDialog.setIcon(R.drawable.icon_app);
								alertDialog.show();
							}
						}
						else
						{
							Toast.makeText(ActivityEpisodes.this, ShowDisplayAnswer.errorNumber + " : " + ShowDisplayAnswer.errorString, Toast.LENGTH_LONG).show();
						}

					break;

					case ERROR_EPISODES:

						action_left = 2;

						if (showEpisodesAnswer.errorNumber == 10)
						{
							if (!isFinishing())
							{
								AlertDialog alertDialog = new AlertDialog.Builder(ActivityEpisodes.this).create();
								alertDialog.setMessage(getString(R.string.error_connexion));
								alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										finish();
									}
								});
								alertDialog.setIcon(R.drawable.icon_app);
								alertDialog.show();
							}
						}
						else
						{
							Toast.makeText(ActivityEpisodes.this, showEpisodesAnswer.errorNumber + " : " + showEpisodesAnswer.errorString, Toast.LENGTH_LONG).show();
						}

					break;

					case ACTION_SHOW_LOADED:

						action_left--;

						if (action_left == 0)
						{
							ActivityEpisodes.this.episodes = new ArrayList<JsonEpisode>();

							Map<Integer, JsonSeason> seasons = showEpisodesAnswer.json.root.seasons;

							JsonEpisode separator;
							Map<Integer, JsonEpisode> episodes;
							int i, j;

							int frenchTime = (Prefs.getPreferences(ActivityEpisodes.this).getBoolean(Prefs.DECALLAGE_1_JOUR, false) ? (3600 * 24) : 0);

							for (i = seasons.size() - 1; i >= 0; i--)
							{
								separator = new JsonEpisode();
								separator.title = "Saison " + seasons.get(i).number;
								separator.isSeparator = true;
								ActivityEpisodes.this.episodes.add(separator);

								episodes = seasons.get(i).episodes;

								for (j = episodes.size() - 1; j >= 0; j--)
								{
									// DECALLAGE POUR CORRESPONDRE A LA DATE
									// FRANCAISE
									episodes.get(j).date += frenchTime;
									episodes.get(j).season = seasons.get(i).number;

									ActivityEpisodes.this.episodes.add(episodes.get(j));
								}
							}

							episodesAdapter = new EpisodesAdapter(ActivityEpisodes.this, ActivityEpisodes.this.episodes, ActivityEpisodes.this);
							listView.setAdapter(episodesAdapter);

							setScreen(SCREEN_CONTENT);

							if (ShowDisplayAnswer.json.root.show.banner != null)
								setShowImage(ShowDisplayAnswer.json.root.show.banner, showJson.url);
							else
							{
								bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banner_show_nopreview);
								ImageView banner = (ImageView) findViewById(R.id.showHeaderBanner);
								banner.setImageBitmap(bitmap);
								banner.setVisibility(View.VISIBLE);
							}
						}

					break;

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
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		if (arg2 == 0) // Il s'agit du header de la listView, donc on redirige
						// vers l'activity ActivityUser
		{
			Intent intent = new Intent(this, ActivityShowInfos.class);
			intent.putExtra("json", new Gson().toJson(ShowDisplayAnswer.json.root.show, JsonShow.class));
			startActivity(intent);
		}
		else
		{
			if (!this.episodes.get(arg2 - 1).isSeparator)
			{
				Intent intent = new Intent(this, ActivityEpisodeInfos.class);
				intent.putExtra("json", new Gson().toJson(this.episodes.get(arg2 - 1), JsonEpisode.class));
				intent.putExtra("url", showJson.url);
				intent.putExtra("title", showJson.title);
				intent.putExtra("archived", showJson.archive == 1 ? true : false);
				startActivity(intent);
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		final int position = (Integer) v.getTag();

		if (showJson.archive == 1)
		{
			Toast.makeText(this, "Erreur : Cette serie est archivé. Vous ne pouvez donc la modifier.", Toast.LENGTH_LONG).show();
		}
		else
		{
			if (episodes.get(position).has_seen == 1)
			{
				markSeen(position, false, 0);
			}
			else if (episodes.get(position).date <= Tools.getTimestampOfTodayAtMidnight())
			{
				if (Prefs.getPreferences(this).getBoolean(Prefs.ADD_MARK, true))
				{

					LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
					final View layout = inflater.inflate(R.layout.episode_review, (ViewGroup) findViewById(R.id.episodeReviewLayout));

					AlertDialog alertDialog = new AlertDialog.Builder(this).create();
					alertDialog.setView(layout);
					alertDialog.setTitle("Noter l'épisode");
					alertDialog.setButton("Valider", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							RatingBar ratingBar = (RatingBar) layout.findViewById(R.id.episodeReviewRating);
							int rate = (int) ratingBar.getRating();

							markSeen(position, true, rate);

							dialog.dismiss();
						}
					});
					alertDialog.show();
				}
				else
				{
					markSeen(position, true, 0);
				}
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

		if (info.position == 0) // empecher long press sur le header
			return;

		JsonEpisode item = (JsonEpisode) episodesAdapter.getItem(info.position - 1);

		menu.setHeaderTitle("Episode : " + item.title);

		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.episode_item, menu);

		if (item.has_seen == 1)
		{
			menu.findItem(R.id.episodeMarkSeen).setVisible(false);
		}
		else if (episodes.get(info.position - 1).date <= Tools.getTimestampOfTodayAtMidnight())
		{
			menu.findItem(R.id.episodeMarkUnseen).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.episodeMarkSeen).setVisible(false);
			menu.findItem(R.id.episodeMarkUnseen).setVisible(false);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId())
		{
			case R.id.episodeMarkSeen:

				markSeen(info.position - 1, true, 0);

			break;

			case R.id.episodeMarkUnseen:

				markSeen(info.position - 1, false, 0);

			break;
		}

		return super.onContextItemSelected(item);
	}

	public void markSeen(int position, boolean seen, int rate)
	{
		int beforeActionNbUnseen = getCountUnseen();

		if (seen)
		{
			episodesAdapter.markAsSeen(position);
			this.memberWatched(episodes.get(position), rate);
		}
		else
		{
			episodesAdapter.markAsUnSeen(position);

			// -----
			// SERT A MARQUER LEPISODE PRECEDENT COMME VU, EN PRENANT EN COMPTE
			// QUIL PEUT SAGIR DUN SEPARATOR
			// -----

			int i;
			for (i = 1; i <= episodes.size(); i++)
			{
				if (position + i >= episodes.size()) // on a depasser la taille,
														// il faut donc marquer
														// le S00E00 comme vue
														// pour marquer le
														// S01E01 comme non vu
				{
					JsonEpisode temp = new JsonEpisode();
					temp.season = 0;
					temp.episode = 0;

					this.memberWatched(temp, 0);
					break;
				}
				else if (episodes.get(position + i).isSeparator)
				{
					// on saute
				}
				else
				{
					this.memberWatched(episodes.get(position + i), 0);
					break;
				}
			}
		}

		int afterActionNbUnseen = getCountUnseen();

		int diffNbUnseen = afterActionNbUnseen - beforeActionNbUnseen;

		int nbEpisodesUnseen = Prefs.getPreferences(ActivityEpisodes.this).getInt(Prefs.NB_EPISODES_UNSEEN, 0);

		// On met le badge a 0 car le nombre d'episodes non vu n'est plus
		// "accurate" lorsque l'on marque un episode vu/non vu (en effet marquer
		// un episode comme vu revient a marquer tout les episodes précédents
		// comme vu et on ne sait pas combien se trouve dans le planning)
		Prefs.getPreferences(ActivityEpisodes.this).edit().putInt(Prefs.NB_PLANNING_UNSEEN, 0).commit();

		if (nbEpisodesUnseen != 0 && !Prefs.getPreferences(ActivityEpisodes.this).getBoolean(Prefs.EPISODES_ONLYONE, false))
			Prefs.getPreferences(ActivityEpisodes.this).edit().putInt(Prefs.NB_EPISODES_UNSEEN, (nbEpisodesUnseen + diffNbUnseen)).commit();
	}

	public int getCountUnseen()
	{
		int i;
		int v = 0;

		for (i = 0; i < episodes.size(); i++)
		{
			if (episodes.get(i).has_seen == 0 && !episodes.get(i).isSeparator)
			{
				if (episodes.get(i).date <= Tools.getTimestampOfTodayAtMidnight())
					v++;
			}
		}

		return v;
	}
}
