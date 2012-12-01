package com.quanturium.bseries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.quanturium.bseries.adapters.PlanningAdapter;
import com.quanturium.bseries.apis.APIBetaSeries;
import com.quanturium.bseries.apis.APIBetaSeriesAnswer;
import com.quanturium.bseries.apis.Social;
import com.quanturium.bseries.json.JsonEpisode;
import com.quanturium.bseries.tools.Cache;
import com.quanturium.bseries.tools.GAnalytics;
import com.quanturium.bseries.tools.Prefs;
import com.quanturium.bseries.tools.Tools;

public class ActivityPlanning extends Activity implements OnItemClickListener, OnClickListener
{

	private Handler				handler;

	public APIBetaSeries		BetaSeries							= APIBetaSeries.getInstance();

	private List<JsonEpisode>	planning							= new ArrayList<JsonEpisode>();
	private PlanningAdapter		planningAdapter;

	private ListView			listView;
	private RelativeLayout		loadingScreen;
	private RelativeLayout		contentScreen;
	private RelativeLayout		emptyviewScreen;
	private TextView			username;

	private Bitmap				bitmap;

	private APIBetaSeriesAnswer	planningMemberAnswer;

	private static final int	ERROR								= -1;
	private static final int	ACTION_PLANNING_LOADED				= 0;
	private static final int	ACTION_MARK_EPISODE_WATCHED_SUCCESS	= 1;
	private static final int	ACTION_MARK_EPISODE_WATCHED_FAILED	= 2;
	private static final int	ACTION_AVATAR_LOADED				= 3;

	private static final int	SCREEN_LOADER						= 1;
	private static final int	SCREEN_CONTENT						= 2;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_episodes);
		setTitle("BSeries : Planning");

		if (android.os.Build.VERSION.SDK_INT >= 14)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		loadingScreen = (RelativeLayout) findViewById(R.id.loadingScreen);
		contentScreen = (RelativeLayout) findViewById(R.id.contentScreen);
		emptyviewScreen = (RelativeLayout) findViewById(R.id.emptyviewScreen);
		listView = (ListView) findViewById(R.id.listView);
		username = (TextView) findViewById(R.id.headerUsername);

		View headerView = View.inflate(this, R.layout.include_user_header, null);
		headerView.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		listView.addHeaderView(headerView, "", false);
		listView.setOnItemClickListener(this);

		registerForContextMenu(listView);

		setHandler();

		planningMember();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void planningMember()
	{
		((TextView) loadingScreen.getChildAt(0)).setText(R.string.loading_default);
		setScreen(SCREEN_LOADER);

		new Thread(new Runnable() {

			@Override
			public void run()
			{
				// TODO Auto-generated method stub

				planningMemberAnswer = BetaSeries.planningMember("unseen", Prefs.getPreferences(ActivityPlanning.this).getString(Prefs.TOKEN, ""));

				if (planningMemberAnswer.json.root.code == 1)
				{
					handler.sendEmptyMessage(ACTION_PLANNING_LOADED);
				}
				else
				{
					handler.sendEmptyMessage(ERROR);
				}
			}
		}).start();
	}

	private void memberWatched(final JsonEpisode jsonEpisode, final int rate)
	{
		Social social = new Social(this);

		if (!Prefs.getPreferences(this).getString(Prefs.SOCIAL_FACEBOOK_NAME, "").equals(""))
			social.addNetwork(Social.FACEBOOK);

		if (!Prefs.getPreferences(this).getString(Prefs.SOCIAL_TWITTER_NAME, "").equals(""))
			social.addNetwork(Social.TWITTER);

		social.publish("Je viens de regarder cet Žpisode :", jsonEpisode.number + " " + jsonEpisode.title, jsonEpisode.show, "", "http://api.betaseries.com/pictures/episode/" + jsonEpisode.url + ".jpg?season=" + jsonEpisode.season + "&episode=" + jsonEpisode.episode, "https://www.betaseries.com/episode/" + jsonEpisode.url + "/" + jsonEpisode.number);

		new Thread(new Runnable() {

			@Override
			public void run()
			{
				// TODO Auto-generated method stub

				APIBetaSeriesAnswer answer = BetaSeries.memberWatched(jsonEpisode.url, "" + jsonEpisode.season, "" + jsonEpisode.episode, rate, Prefs.getPreferences(ActivityPlanning.this).getString(Prefs.TOKEN, ""));

				if (answer.json.root.code == 1)
				{
					handler.sendEmptyMessage(ACTION_MARK_EPISODE_WATCHED_SUCCESS);
				}
				else
				{
					handler.sendEmptyMessage(ACTION_MARK_EPISODE_WATCHED_FAILED);
				}
			}
		}).start();
	}

	private void setAvatar()
	{
		if ((bitmap = Cache.getCachedBitmap(ActivityPlanning.this, "bitmap.user.avatar.jpg", Cache.CACHE_TIME_BITMAP)) != null)
		{
			handler.sendEmptyMessage(ACTION_AVATAR_LOADED);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

		if (info.position == 0) // empecher long press sur le header
			return;

		JsonEpisode item = planning.get(info.position - 1);

		menu.setHeaderTitle("Episode : " + item.title);

		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.episode_item, menu);

		menu.findItem(R.id.episodeMarkUnseen).setVisible(false);

		if (planning.get(info.position - 1).date > Tools.getTimestampOfTodayAtMidnight())
		{
			menu.findItem(R.id.episodeMarkSeen).setVisible(false);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId())
		{
			case R.id.episodeMarkSeen:

				markSeen(info.position - 1, 0);

			break;
		}

		return super.onContextItemSelected(item);
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
	}

	public void setHandler()
	{
		handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg)
			{
				switch (msg.what)
				{
					case ERROR:

						if (planningMemberAnswer.errorNumber == 10)
						{
							if (!isFinishing())
							{
								AlertDialog alertDialog = new AlertDialog.Builder(ActivityPlanning.this).create();
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
							Toast.makeText(ActivityPlanning.this, planningMemberAnswer.errorNumber + " : " + planningMemberAnswer.errorString, Toast.LENGTH_LONG).show();
						}

					break;

					case ACTION_PLANNING_LOADED:

						Map<Integer, JsonEpisode> planning = planningMemberAnswer.json.root.planning;
						Map<Integer, List<JsonEpisode>> planningSorted = new HashMap<Integer, List<JsonEpisode>>();
						List<Integer> keys = new ArrayList<Integer>();

						JsonEpisode separator = null;
						int i,
						j;
						boolean hideNotAired = Prefs.getPreferences(ActivityPlanning.this).getBoolean(Prefs.PLANNING_HIDE_NOTAIRED, false);

						int frenchTime = (Prefs.getPreferences(ActivityPlanning.this).getBoolean(Prefs.DECALLAGE_1_JOUR, false) ? (3600 * 24) : 0);

						for (i = 0; i < planning.size(); i++)
						{
							// DECALLAGE POUR CORRESPONDRE A LA DATE FRANCAISE
							planning.get(i).date += frenchTime;

							if (planning.get(i).date > Tools.getTimestampOfTodayAtMidnight() && hideNotAired) // Si
																												// l'episode
																												// n'est
																												// pas
																												// sortie
																												// enore
																												// et
																												// que
																												// l'utilisateur
																												// a
																												// choisit
																												// de
																												// masquer
																												// les
																												// non
																												// sortie,
																												// alors
																												// on
																												// continue;
								continue;

							if (!planningSorted.containsKey(planning.get(i).date))
							{
								planningSorted.put(planning.get(i).date, new ArrayList<JsonEpisode>());
								keys.add(planning.get(i).date);

							}

							planningSorted.get(planning.get(i).date).add(planning.get(i));
						}

						for (i = 0; i < planningSorted.size(); i++)
						{
							separator = new JsonEpisode();
							separator.isSeparator = true;
							separator.date = planningSorted.get(keys.get(i)).get(0).date;
							ActivityPlanning.this.planning.add(separator);

							for (j = 0; j < planningSorted.get(keys.get(i)).size(); j++)
							{
								ActivityPlanning.this.planning.add(planningSorted.get(keys.get(i)).get(j));
							}
						}

						planningAdapter = new PlanningAdapter(ActivityPlanning.this, ActivityPlanning.this.planning, ActivityPlanning.this);
						listView.setAdapter(planningAdapter);

						username = (TextView) findViewById(R.id.headerUsername);
						username.setText(Prefs.getPreferences(ActivityPlanning.this).getString(Prefs.USERNAME, "n/a"));

						Prefs.getPreferences(ActivityPlanning.this).edit().putInt(Prefs.NB_PLANNING_UNSEEN, getCountUnseen()).commit();

						setAvatar();

						setScreen(SCREEN_CONTENT);

					break;

					case ACTION_MARK_EPISODE_WATCHED_SUCCESS:

					break;

					case ACTION_MARK_EPISODE_WATCHED_FAILED:

					break;

					case ACTION_AVATAR_LOADED:

						ImageView avatar = (ImageView) findViewById(R.id.headerAvatar);
						avatar.setImageBitmap(bitmap);

					break;
				}
			};
		};
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		if (!this.planning.get(arg2 - 1).isSeparator)
		{
			Intent intent = new Intent(this, ActivityEpisodeInfos.class);
			intent.putExtra("json", new Gson().toJson(planning.get(arg2 - 1), JsonEpisode.class));
			intent.putExtra("url", planning.get(arg2 - 1).url);
			intent.putExtra("title", planning.get(arg2 - 1).show);
			intent.putExtra("archived", false);
			startActivity(intent);
		}
	}

	@Override
	public void onClick(final View v)
	{
		switch (v.getId())
		{
			case R.id.itemEpisodeIcon:

				final int position = (Integer) v.getTag();

				if (Prefs.getPreferences(this).getBoolean(Prefs.ADD_MARK, true))
				{

					LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
					final View layout = inflater.inflate(R.layout.episode_review, (ViewGroup) findViewById(R.id.episodeReviewLayout));

					AlertDialog alertDialog = new AlertDialog.Builder(this).create();
					alertDialog.setView(layout);
					alertDialog.setTitle("Noter l'Žpisode");
					alertDialog.setButton("Valider", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							RatingBar ratingBar = (RatingBar) layout.findViewById(R.id.episodeReviewRating);
							int rate = (int) ratingBar.getRating();

							markSeen(position, rate);

							dialog.dismiss();
						}
					});
					alertDialog.show();
				}
				else
				{
					markSeen(position, 0);
				}

			break;
		}

	}

	public void markSeen(int position, int rate)
	{
		int beforeActionNbUnseen = getCountUnseen();

		memberWatched(planning.get(position), rate);
		planningAdapter.markAsSeen(position);

		int afterActionNbUnseen = getCountUnseen();

		int diffNbUnseen = afterActionNbUnseen - beforeActionNbUnseen;

		int nbEpisodesUnseen = Prefs.getPreferences(ActivityPlanning.this).getInt(Prefs.NB_EPISODES_UNSEEN, 0);

		Prefs.getPreferences(ActivityPlanning.this).edit().putInt(Prefs.NB_PLANNING_UNSEEN, getCountUnseen()).commit();

		if (nbEpisodesUnseen != 0)
			Prefs.getPreferences(ActivityPlanning.this).edit().putInt(Prefs.NB_EPISODES_UNSEEN, (nbEpisodesUnseen + diffNbUnseen)).commit();
	}

	public int getCountUnseen()
	{
		int i;
		int v = 0;

		for (i = 0; i < planning.size(); i++)
		{
			if (planning.get(i).has_seen == 0 && !planning.get(i).isSeparator)
			{
				if (planning.get(i).date <= Tools.getTimestampOfTodayAtMidnight())
					v++;
			}
		}

		return v;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		GAnalytics.getInstance().Track(this, "/planning");
	}
}
