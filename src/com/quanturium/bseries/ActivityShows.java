package com.quanturium.bseries;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.quanturium.bseries.adapters.ShowsAdapter;
import com.quanturium.bseries.apis.APIBetaSeries;
import com.quanturium.bseries.apis.APIBetaSeriesAnswer;
import com.quanturium.bseries.json.JsonMember;
import com.quanturium.bseries.json.JsonShow;
import com.quanturium.bseries.tools.Cache;
import com.quanturium.bseries.tools.GAnalytics;
import com.quanturium.bseries.tools.Prefs;

public class ActivityShows extends Activity implements OnItemClickListener
{
	private Handler				handler;

	public APIBetaSeries		BetaSeries				= APIBetaSeries.getInstance();

	private ListView			listView;
	private List<JsonShow>		shows					= new ArrayList<JsonShow>();	
	private ShowsAdapter		showsAdapter;

	private RelativeLayout		loadingScreen;
	private RelativeLayout		contentScreen;
	private RelativeLayout		emptyviewScreen;
	private TextView			username;

	private APIBetaSeriesAnswer	memberInfosAnswer;
	private APIBetaSeriesAnswer	showArchiveAnswer;
	private APIBetaSeriesAnswer	showRemoveAnswer;

	private Bitmap				bitmap;

	private static final int	SCREEN_LOADER			= 1;
	private static final int	SCREEN_CONTENT			= 2;
	private static final int	NO_SCREEN				= 0;

	private static final int	ERROR					= -1;
	private static final int	ACTION_INFOS_LOADED		= 3;
	private static final int	ACTION_AVATAR_LOADED	= 4;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shows);
		setTitle(R.string.title_shows);
		
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

		memberInfos();

		Prefs.getPreferences(this).edit().putBoolean(Prefs.NEED_RELOAD_SHOWS, false).commit();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (Prefs.getPreferences(this).getBoolean(Prefs.NEED_RELOAD_SHOWS, false))
		{
			Prefs.getPreferences(this).edit().putBoolean(Prefs.NEED_RELOAD_SHOWS, false).commit();
			memberInfos();
		}

		GAnalytics.getInstance().Track(this, "/shows");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.shows, menu);
		
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		// TODO Auto-generated method stub

		if (contentScreen.getVisibility() == View.VISIBLE)
		{
			menu.findItem(R.id.addSerie).setEnabled(true);
		}
		else
		{
			menu.findItem(R.id.addSerie).setEnabled(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.addSerie:
				
				onSearchRequested();

			break;
			
			case android.R.id.home :
				
				finish();
				
			break;
		}

		return true;
	}

	@Override
	public boolean onSearchRequested()
	{
		// ON REMET A 0 LE CONTENU DU HASHMAP DU JSON car quand un élément est supprimé, on ne peut pas le supprimer du hashmap. Lorsque l'on fait une recherche, on transmet ce hashmap. Il doit etre à jour ! (aka : il ne doit pas comprendre les éléments supprimés)

		memberInfosAnswer.json.root.member.shows = new HashMap<Integer, JsonShow>();
		int i;
		for (i = 0; i < shows.size(); i++)
			memberInfosAnswer.json.root.member.shows.put(i, shows.get(i));

		Bundle appData = new Bundle();
		appData.putString("json", new Gson().toJson(memberInfosAnswer.json.root.member, JsonMember.class));
		startSearch(null, false, appData, false);
		return true;
	}

	private void memberInfos()
	{
		((TextView) loadingScreen.getChildAt(0)).setText(R.string.loading_default);
		setScreen(SCREEN_LOADER);

		new Thread(new Runnable() {

			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				memberInfosAnswer = BetaSeries.memberInfos(Prefs.getPreferences(ActivityShows.this).getString(Prefs.TOKEN, ""));

				if (memberInfosAnswer.json.root.code == 1)
				{
					handler.sendEmptyMessage(ACTION_INFOS_LOADED);
				}
				else
				{
					handler.sendEmptyMessage(ERROR);
				}
			}
		}).start();
	}

	private void showArchive(final int position)
	{
		new Thread(new Runnable() {

			@Override
			public void run()
			{
				showArchiveAnswer = BetaSeries.showArchive(shows.get(position).url, Prefs.getPreferences(ActivityShows.this).getString(Prefs.TOKEN, ""));

				if (showArchiveAnswer.json.root.code != 1)
				{
					handler.post(new Runnable() {

						@Override
						public void run()
						{
							Toast.makeText(ActivityShows.this, "Echec de l'archivage de " + shows.get(position).title, Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}

	private void showUnarchive(final int position)
	{
		new Thread(new Runnable() {

			@Override
			public void run()
			{
				showArchiveAnswer = BetaSeries.showUnarchive(shows.get(position).url, Prefs.getPreferences(ActivityShows.this).getString(Prefs.TOKEN, ""));

				if (showArchiveAnswer.json.root.code != 1)
				{
					handler.post(new Runnable() {

						@Override
						public void run()
						{
							Toast.makeText(ActivityShows.this, "Echec du désarchivage de " + shows.get(position).title, Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}

	private void showRemove(final String url)
	{
		new Thread(new Runnable() {

			@Override
			public void run()
			{
				showRemoveAnswer = BetaSeries.showRemove(url, Prefs.getPreferences(ActivityShows.this).getString(Prefs.TOKEN, ""));

				if (showRemoveAnswer.json.root.code != 1)
				{
					handler.post(new Runnable() {

						@Override
						public void run()
						{
							Toast.makeText(ActivityShows.this, "Echec de la suppression de " + url, Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}).start();
	}
	
	

	private void setAvatar(final String link)
	{
		if ((bitmap = Cache.getCachedBitmap(ActivityShows.this, "bitmap.user.avatar.jpg", Cache.CACHE_TIME_BITMAP)) != null)
		{
			handler.sendEmptyMessage(ACTION_AVATAR_LOADED);
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

						Cache.setCachedBitmap(ActivityShows.this, "bitmap.user.avatar.jpg", bitmap);

					}
					catch (MalformedURLException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					handler.sendEmptyMessage(ACTION_AVATAR_LOADED);

				}
			}).start();
		}
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

			case NO_SCREEN:

				contentScreen.setVisibility(View.GONE);
				loadingScreen.setVisibility(View.GONE);

			break;
		}
		
		if (android.os.Build.VERSION.SDK_INT >= 11)
			invalidateOptionsMenu();
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

						setScreen(NO_SCREEN);

						if (memberInfosAnswer.errorNumber == 10)
						{
							if (!isFinishing())
							{
								AlertDialog alertDialog = new AlertDialog.Builder(ActivityShows.this).create();
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
							Toast.makeText(ActivityShows.this, memberInfosAnswer.errorNumber + " : " + memberInfosAnswer.errorString, Toast.LENGTH_LONG).show();
						}

					break;

					case ACTION_INFOS_LOADED:

						if (memberInfosAnswer.json.root.member.options.decalage == 1)
						{
							Prefs.getPreferences(ActivityShows.this).edit().putBoolean(Prefs.DECALLAGE_1_JOUR_ENABLED, false).commit();
							Prefs.getPreferences(ActivityShows.this).edit().putBoolean(Prefs.DECALLAGE_1_JOUR, true).commit();
						}
						else
						{
							Prefs.getPreferences(ActivityShows.this).edit().putBoolean(Prefs.DECALLAGE_1_JOUR_ENABLED, true).commit();
							Prefs.getPreferences(ActivityShows.this).edit().putBoolean(Prefs.DECALLAGE_1_JOUR, false).commit();
						}

						ActivityShows.this.shows = new ArrayList<JsonShow>();
						Map<Integer, JsonShow> shows = memberInfosAnswer.json.root.member.shows;
						int i;

						for (i = 0; i <= shows.size() - 1; i++)
						{
							ActivityShows.this.shows.add(shows.get(i));
						}

						showsAdapter = new ShowsAdapter(ActivityShows.this, ActivityShows.this.shows);
						listView.setAdapter(showsAdapter);

						username = (TextView) findViewById(R.id.headerUsername);
						username.setText(Prefs.getPreferences(ActivityShows.this).getString(Prefs.USERNAME, "n/a"));

						if (memberInfosAnswer.json.root.member.avatar != null && !memberInfosAnswer.json.root.member.avatar.equals(""))
							setAvatar(memberInfosAnswer.json.root.member.avatar);
						else
							Cache.remove(ActivityShows.this, "bitmap.user.avatar.jpg");

						setScreen(SCREEN_CONTENT);

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
		// TODO Auto-generated method stub

		if (arg2 != 0) // Il s'agit du header de la listView, donc on redirige
						// vers l'activity ActivityUser
		{
			Intent intent = new Intent(this, ActivityEpisodes.class);
			intent.putExtra("json", new Gson().toJson(shows.get(arg2 - 1), JsonShow.class));
			startActivity(intent);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		// TODO Auto-generated method stub
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

		if (info.position == 0) // empecher long press sur le header
			return;

		JsonShow item = (JsonShow) showsAdapter.getItem(info.position - 1);

		menu.setHeaderTitle("Serie : " + item.title);

		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.show_item, menu);

		if (item.archive == 1)
		{
			menu.findItem(R.id.showArchive).setVisible(false);
		}
		else
		{
			menu.findItem(R.id.showUnarchive).setVisible(false);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		// TODO Auto-generated method stub

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId())
		{
			case R.id.showDelete:

				showRemove(shows.get(info.position - 1).url);
				showsAdapter.removeItem(info.position - 1);

			break;

			case R.id.showRecommend:

			break;

			case R.id.showArchive:

				archive(info.position - 1, true);

			break;

			case R.id.showUnarchive:

				archive(info.position - 1, false);

			break;
		}

		return super.onContextItemSelected(item);
	}

//	public void onClick(View v)
//	{
//		int position = (Integer) v.getTag();
//
//		if (shows.get(position).archive == 1)
//		{
//			archive(position, false);
//		}
//		else
//		{
//			archive(position, true);
//		}
//	}

	public void archive(int position, boolean archive)
	{
		if (archive)
		{
			showsAdapter.archiveItem(position);
			this.showArchive(position);
		}
		else
		{
			showsAdapter.unarchiveItem(position);
			this.showUnarchive(position);
		}
	}
}
