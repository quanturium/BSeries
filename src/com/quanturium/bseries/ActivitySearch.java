package com.quanturium.bseries;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.quanturium.bseries.adapters.SearchAdapter;
import com.quanturium.bseries.apis.APIBetaSeries;
import com.quanturium.bseries.apis.APIBetaSeriesAnswer;
import com.quanturium.bseries.json.JsonMember;
import com.quanturium.bseries.json.JsonShow;
import com.quanturium.bseries.tools.GAnalytics;
import com.quanturium.bseries.tools.Prefs;

public class ActivitySearch extends Activity implements OnClickListener
{
	private Handler				showsSearchHandler;

	public APIBetaSeries		BetaSeries				= APIBetaSeries.getInstance();
	private APIBetaSeriesAnswer	showsSearchAnswer;
	private JsonMember			memberInfosJson;

	private List<JsonShow>		shows					= new ArrayList<JsonShow>();
	private SearchAdapter		searchAdapter;

	private ListView			listView;
	private RelativeLayout		loadingScreen;
	private RelativeLayout		contentScreen;

	private static final int	ERROR					= -1;
	private static final int	ACTION_SEARCH_SUCCESS	= 0;

	private static final int	NO_SCREEN				= 0;
	private static final int	SCREEN_LOADER			= 1;
	private static final int	SCREEN_CONTENT			= 2;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			String query = intent.getStringExtra(SearchManager.QUERY);
			Bundle appData = getIntent().getBundleExtra(SearchManager.APP_DATA);

			if (appData != null)
			{
				memberInfosJson = new Gson().fromJson(appData.getString("json"), JsonMember.class);
			}
			else
				finish();

			setTitle("BSeries : Recherche");
			
			if (android.os.Build.VERSION.SDK_INT >= 14)
				getActionBar().setDisplayHomeAsUpEnabled(true);

			loadingScreen = (RelativeLayout) findViewById(R.id.loadingScreen);
			contentScreen = (RelativeLayout) findViewById(R.id.contentScreen);

			listView = (ListView) findViewById(R.id.listView);

			setHandler();
			showsSearch(query);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home :
				finish();
				return true;
			
			default : 
				return super.onOptionsItemSelected(item);
		}				
	}

	private void showsSearch(final String query)
	{
		if (query.length() < 2)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Votre recherche doit faire au moins 2 charactres");
			builder.setCancelable(false);
			builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					// TODO Auto-generated method stub
					finish();
				}
			});

			AlertDialog alert = builder.create();
			alert.show();
		}
		else
		{
			try
			{
				GAnalytics.getInstance().Track(this, "/show/search?query=" + URLEncoder.encode(query, "UTF-8"));
			}
			catch (UnsupportedEncodingException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			((TextView) loadingScreen.getChildAt(0)).setText(R.string.loading_default);
			setScreen(SCREEN_LOADER);
			setTitle("BSeries : Recherche de <" + query + ">");

			new Thread(new Runnable() {

				@Override
				public void run()
				{
					// TODO Auto-generated method stub

					try
					{
						showsSearchAnswer = BetaSeries.showSearch(query);
					}
					catch (UnsupportedEncodingException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (showsSearchAnswer.json.root.code == 1)
					{
						showsSearchHandler.sendEmptyMessage(ACTION_SEARCH_SUCCESS);
					}
					else
					{
						showsSearchHandler.sendEmptyMessage(ERROR);
					}
				}
			}).start();
		}
	}

	private void showAdd(final String url, final String token)
	{
		new Thread(new Runnable() {

			@Override
			public void run()
			{
				// TODO Auto-generated method stub

				showsSearchAnswer = BetaSeries.showAdd(url, token);

				if (showsSearchAnswer.json.root.code != 1)
				{
					showsSearchHandler.post(new Runnable() {

						@Override
						public void run()
						{
							Toast.makeText(ActivitySearch.this, "Echec de l'ajout de la serie " + url, Toast.LENGTH_LONG).show();
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

			break;

			case SCREEN_LOADER:

				contentScreen.setVisibility(View.GONE);
				loadingScreen.setVisibility(View.VISIBLE);

			break;

			case NO_SCREEN:

				contentScreen.setVisibility(View.GONE);
				loadingScreen.setVisibility(View.GONE);

			break;
		}
	}

	private void setHandler()
	{
		showsSearchHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg)
			{

				switch (msg.what)
				{
					case ERROR:

						setScreen(NO_SCREEN);

						if (showsSearchAnswer.errorNumber == 10)
						{
							if (!isFinishing())
							{
								AlertDialog alertDialog = new AlertDialog.Builder(ActivitySearch.this).create();
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
							Toast.makeText(ActivitySearch.this, showsSearchAnswer.errorNumber + " : " + showsSearchAnswer.errorString, Toast.LENGTH_LONG).show();
						}

					break;

					case ACTION_SEARCH_SUCCESS:

						Map<Integer, JsonShow> shows = showsSearchAnswer.json.root.shows;
						int i;
						JsonShow show;

						for (i = 0; i < shows.size(); i++)
						{
							show = shows.get(i);

							int status = getAlreadyInTheUserAccount(show.url);

							if (status > 0)
								show.isAlreadyInTheUserAccount = true;

							switch (status)
							{
								case 1:
									show.archive = 0;
								break;

								case 2:
									show.archive = 1;
								break;
							}

							ActivitySearch.this.shows.add(shows.get(i));
						}

						searchAdapter = new SearchAdapter(ActivitySearch.this, ActivitySearch.this.shows, ActivitySearch.this);
						listView.setAdapter(searchAdapter);

						setScreen(SCREEN_CONTENT);

					break;
				}
			};
		};
	}

	protected int getAlreadyInTheUserAccount(String url)
	{
		Map<Integer, JsonShow> shows = memberInfosJson.shows;
		int j;

		for (j = 0; j < shows.size(); j++)
		{
			if (shows.get(j).url.equals(url))
			{
				if (shows.get(j).archive == 0)
					return 1;
				else
					return 2;
			}
		}

		return 0;
	}

	@Override
	public void onClick(View v)
	{
		int position = (Integer) v.getTag();
		JsonShow show = shows.get(position);

		if (show.isAlreadyInTheUserAccount == false)
		{
			showAdd(show.url, Prefs.getPreferences(ActivitySearch.this).getString(Prefs.TOKEN, ""));

			show.archive = 0;
			show.isAlreadyInTheUserAccount = true;

			searchAdapter.notifyDataSetChanged();

			Prefs.getPreferences(this).edit().putBoolean(Prefs.NEED_RELOAD_SHOWS, true).commit();

		}
	}
}
