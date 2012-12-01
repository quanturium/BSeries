package com.quanturium.bseries;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quanturium.bseries.apis.APIBetaSeries;
import com.quanturium.bseries.apis.APIBetaSeriesAnswer;
import com.quanturium.bseries.tools.Cache;
import com.quanturium.bseries.tools.GAnalytics;
import com.quanturium.bseries.tools.Prefs;
import com.quanturium.bseries.tools.Tools;

public class ActivityUser extends Activity
{

	RelativeLayout				header;
	TextView					infoUserName;
	TextView					infosUserNbSeries;
	TextView					infoUserNbSaisons;
	TextView					infoUserNbEpisodes;
	TextView					infoUserNbFriends;
	TextView					infoUserProgress;
	ProgressBar					infoUserProgressbar;
	TextView					infoTimeOnTV;
	TextView					infoTimeToSpend;

	private RelativeLayout		loadingScreen;
	private LinearLayout		contentScreen;

	private Bitmap				bitmap;

	public APIBetaSeries		BetaSeries				= APIBetaSeries.getInstance();
	private APIBetaSeriesAnswer	answer;

	private Handler				handler;

	private static final int	ERROR					= -1;
	private static final int	ACTION_INFOS_LOADED		= 0;
	private static final int	ACTION_AVATAR_LOADED	= 1;

	private static final int	SCREEN_LOADER			= 1;
	private static final int	SCREEN_CONTENT			= 2;
	private static final int	NO_SCREEN				= 3;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_infos);
		setTitle("BSeries : Profil");
		
		if (android.os.Build.VERSION.SDK_INT >= 14)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		loadingScreen = (RelativeLayout) findViewById(R.id.loadingScreen);
		contentScreen = (LinearLayout) findViewById(R.id.contentScreen);

		this.header = (RelativeLayout) findViewById(R.id.infoUserHeader);
		this.infoUserName = (TextView) findViewById(R.id.headerUsername);
		this.infosUserNbSeries = (TextView) findViewById(R.id.infoUserNbSeries);
		this.infoUserNbSaisons = (TextView) findViewById(R.id.infoUserNbSaisons);
		this.infoUserNbEpisodes = (TextView) findViewById(R.id.infoUserNbEpisodes);
		this.infoUserNbFriends = (TextView) findViewById(R.id.infoUserNbFriends);
		this.infoUserProgress = (TextView) findViewById(R.id.infoUserProgress);
		this.infoUserProgressbar = (ProgressBar) findViewById(R.id.infoUserProgressbar);
		this.infoTimeOnTV = (TextView) findViewById(R.id.time_on_tv);
		this.infoTimeToSpend = (TextView) findViewById(R.id.time_to_spend);

		setHandler();

		memberInfos();
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

	private void setContent()
	{
		this.infoUserName.setText(Prefs.getPreferences(ActivityUser.this).getString(Prefs.USERNAME, "n/a"));

		this.infosUserNbSeries.setText(String.valueOf(this.answer.json.root.member.stats.shows));
		this.infoUserNbSaisons.setText(String.valueOf(this.answer.json.root.member.stats.seasons));
		this.infoUserNbEpisodes.setText(String.valueOf(this.answer.json.root.member.stats.episodes));
		this.infoUserNbFriends.setText(String.valueOf(this.answer.json.root.member.stats.friends));
		this.infoUserProgress.setText(this.answer.json.root.member.stats.progress);

		this.infoUserProgressbar.setMax(this.answer.json.root.member.stats.time_on_tv + this.answer.json.root.member.stats.time_to_spend);
		this.infoUserProgressbar.setProgress(this.answer.json.root.member.stats.time_on_tv);

		this.infoTimeOnTV.setText(getText(R.string.time_on_tv) + Tools.durationToString(this.answer.json.root.member.stats.time_on_tv));
		this.infoTimeToSpend.setText(getText(R.string.time_to_spend) + Tools.durationToString(this.answer.json.root.member.stats.time_to_spend));
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
				answer = BetaSeries.memberInfos(Prefs.getPreferences(ActivityUser.this).getString(Prefs.TOKEN, ""));

				if (answer.json.root.code == 1)
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

	private void setAvatar(final String link)
	{
		if ((bitmap = Cache.getCachedBitmap(ActivityUser.this, "bitmap.user.avatar.jpg", Cache.CACHE_TIME_BITMAP)) != null)
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

						Cache.setCachedBitmap(ActivityUser.this, "bitmap.user.avatar.jpg", bitmap);

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

	public void setHandler()
	{
		handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg)
			{
				switch (msg.what)
				{
					case ERROR:

						if (answer.errorNumber == 10)
						{
							if (!isFinishing())
							{
								AlertDialog alertDialog = new AlertDialog.Builder(ActivityUser.this).create();
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
							setScreen(NO_SCREEN);
							Toast.makeText(ActivityUser.this, answer.errorNumber + " : " + answer.errorString, Toast.LENGTH_LONG).show();
						}

					break;

					case ACTION_INFOS_LOADED:

						setContent();

						if (answer.json.root.member.options.decalage == 1)
						{
							Prefs.getPreferences(ActivityUser.this).edit().putBoolean(Prefs.DECALLAGE_1_JOUR_ENABLED, false).commit();
							Prefs.getPreferences(ActivityUser.this).edit().putBoolean(Prefs.DECALLAGE_1_JOUR, true).commit();
						}
						else
						{
							Prefs.getPreferences(ActivityUser.this).edit().putBoolean(Prefs.DECALLAGE_1_JOUR_ENABLED, true).commit();
						}

						if (answer.json.root.member.avatar != null && !answer.json.root.member.avatar.equals(""))
							setAvatar(answer.json.root.member.avatar);
						else
							Cache.remove(ActivityUser.this, "bitmap.user.avatar.jpg");

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
	protected void onResume()
	{
		super.onResume();
		GAnalytics.getInstance().Track(this, "/user");
	}
}
