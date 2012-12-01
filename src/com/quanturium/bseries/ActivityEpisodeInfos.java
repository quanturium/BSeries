package com.quanturium.bseries;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.quanturium.bseries.apis.APIBetaSeries;
import com.quanturium.bseries.apis.APIBetaSeriesAnswer;
import com.quanturium.bseries.apis.Social;
import com.quanturium.bseries.json.JsonComment;
import com.quanturium.bseries.json.JsonEpisode;
import com.quanturium.bseries.json.JsonSub;
import com.quanturium.bseries.tools.Cache;
import com.quanturium.bseries.tools.GAnalytics;
import com.quanturium.bseries.tools.Prefs;
import com.quanturium.bseries.tools.Tools;

public class ActivityEpisodeInfos extends Activity implements OnClickListener
{
	private Handler				handler;

	public APIBetaSeries		BetaSeries					= APIBetaSeries.getInstance();

	private RelativeLayout		loadingScreen;
	private FrameLayout			contentScreen;

	Bitmap						bitmap;

	private String				showUrl;
	private String				showTitle;
	private boolean				showArchived;
	private JsonEpisode			episodeJson;
	private APIBetaSeriesAnswer	showEpisodeAnswer;
	private APIBetaSeriesAnswer	commentsEpisodeAnswer;

	private static final int	ERROR						= -1;
	private static final int	ACTION_EPISODE_LOADED		= 0;
	private static final int	ACTION_BANNER_LOADED		= 3;
	private static final int	ACTION_COMMENTS_LOADED		= 4;

	private static final int	SCREEN_LOADER				= 1;
	private static final int	SCREEN_CONTENT				= 2;

	ImageView					background;
	ImageView					banner;
	TextView					title;
	ImageView					episodeIcon;
	TextView					episodeTitle;
	TextView					episodeDate;
	TextView					episodeSeason;
	TextView					episodeEpisode;
	TextView					episodeDescription;

	TextView					episodeTab1;
	TextView					episodeTab2;
	TextView					episodeTab3;

	LinearLayout				episodeTabContent1;
	LinearLayout				episodeTabContent2;
	LinearLayout				episodeTabContent3;

	RelativeLayout				episodeCommentairesLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_episode_infos);

		Intent i = getIntent();
		Gson gson = new Gson();
		String temp = i.getStringExtra("json");
		String temp2 = i.getStringExtra("url");
		boolean temp3 = i.getBooleanExtra("archived", true);
		String temp4 = i.getStringExtra("title");

		if (temp != null)
			episodeJson = gson.fromJson(temp, JsonEpisode.class);
		else
			finish();

		if (temp2 != null)
			showUrl = temp2;
		else
			finish();

		showArchived = temp3;

		if (temp4 != null)
			showTitle = temp4;
		else
			finish();

		setTitle("BSeries : Episode : " + episodeJson.title);

		if (android.os.Build.VERSION.SDK_INT >= 14)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		loadingScreen = (RelativeLayout) findViewById(R.id.loadingScreen);
		contentScreen = (FrameLayout) findViewById(R.id.contentScreen);

		background = (ImageView) findViewById(R.id.episodeHeaderBackground);
		banner = (ImageView) findViewById(R.id.episodeHeaderBanner);
		title = (TextView) findViewById(R.id.itemEpisodeSeparatorTitle);
		episodeIcon = (ImageView) findViewById(R.id.itemEpisodeIcon);
		episodeTitle = (TextView) findViewById(R.id.itemEpisodeTitle);
		episodeDate = (TextView) findViewById(R.id.itemEpisodeDate);
		episodeSeason = (TextView) findViewById(R.id.itemEpisodeRight1Text);
		episodeEpisode = (TextView) findViewById(R.id.itemEpisodeRight2Text);
		episodeDescription = (TextView) findViewById(R.id.episodeDescription);

		episodeTab1 = (TextView) findViewById(R.id.episodeTab1);
		episodeTab2 = (TextView) findViewById(R.id.episodeTab2);
		episodeTab3 = (TextView) findViewById(R.id.episodeTab3);
		episodeTabContent1 = (LinearLayout) findViewById(R.id.episodeTabContent1);
		episodeTabContent2 = (LinearLayout) findViewById(R.id.episodeTabContent2);
		episodeTabContent3 = (LinearLayout) findViewById(R.id.episodeTabContent3);

		episodeTab1.setOnClickListener(this);
		episodeTab2.setOnClickListener(this);
		episodeTab3.setOnClickListener(this);

		episodeCommentairesLoading = (RelativeLayout) findViewById(R.id.episodeCommentairesLoading);

		setHandler();

		showEpisode();
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

	private void showEpisode()
	{
		((TextView) loadingScreen.getChildAt(0)).setText(R.string.loading_default);
		setScreen(SCREEN_LOADER);

		new Thread(new Runnable() {

			@Override
			public void run()
			{
				showEpisodeAnswer = BetaSeries.showEpisode(showUrl, "" + episodeJson.season, "" + episodeJson.episode, Prefs.getPreferences(ActivityEpisodeInfos.this).getString(Prefs.TOKEN, ""), 0);

				if (showEpisodeAnswer.json.root.code == 1)
				{
					handler.sendEmptyMessage(ACTION_EPISODE_LOADED);
				}
				else
				{
					handler.sendEmptyMessage(ERROR);
				}
			}
		}).start();
	}

	private void commentsEpisode()
	{
		episodeCommentairesLoading.setVisibility(View.VISIBLE);
		((TextView) episodeCommentairesLoading.getChildAt(0)).setText(R.string.loading_default);

		new Thread(new Runnable() {

			@Override
			public void run()
			{
				commentsEpisodeAnswer = BetaSeries.commentsEpisode(showUrl, "" + showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).season, "" + showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).episode, Prefs.getPreferences(ActivityEpisodeInfos.this).getString(Prefs.TOKEN, ""));

				if (commentsEpisodeAnswer.json.root.code == 1)
				{
					handler.sendEmptyMessage(ACTION_COMMENTS_LOADED);
				}
				else
				{
					handler.sendEmptyMessage(ERROR);
				}
			}
		}).start();
	}

	private void setEpisodeImage(final String link, final String show, final String season, final String episode)
	{
		if ((bitmap = Cache.getCachedBitmap(ActivityEpisodeInfos.this, "bitmap." + show + "." + season + "." + episode + ".jpg", Cache.CACHE_TIME_BITMAP)) != null)
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

						Cache.setCachedBitmap(ActivityEpisodeInfos.this, "bitmap." + show + "." + season + "." + episode + ".jpg", bitmap);

					}
					catch (MalformedURLException e)
					{
						e.printStackTrace();
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banner_episode_nopreview);
					}
					catch (IOException e)
					{
						e.printStackTrace();
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banner_episode_nopreview);
					}
					catch (NullPointerException e)
					{
						e.printStackTrace();
						bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banner_episode_nopreview);
					}
					finally
					{
						handler.sendEmptyMessage(ACTION_BANNER_LOADED);
					}
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

		social.publish("Je viens de regarder cet Žpisode :", jsonEpisode.number + " " + jsonEpisode.title, jsonEpisode.show, "", "http://api.betaseries.com/pictures/episode/" + jsonEpisode.url + ".jpg?season=" + jsonEpisode.season + "&episode=" + jsonEpisode.episode, "https://www.betaseries.com/episode/" + jsonEpisode.url + "/" + jsonEpisode.number);		
		
		new Thread(new Runnable() {

			@Override
			public void run()
			{
				APIBetaSeriesAnswer answer = BetaSeries.memberWatched(showUrl, "" + jsonEpisode.season, "" + jsonEpisode.episode, rate, Prefs.getPreferences(ActivityEpisodeInfos.this).getString(Prefs.TOKEN, ""));

				if (answer.json.root.code != 1)
				{
					handler.post(new Runnable() {

						@Override
						public void run()
						{
							Toast.makeText(ActivityEpisodeInfos.this, "Echec lors du marquage de l'Žpisode comme lu / non lu", Toast.LENGTH_LONG).show();
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
		}
	}

	private void setTab(LinearLayout tab)
	{
		episodeTabContent1.setVisibility(View.GONE);
		episodeTabContent2.setVisibility(View.GONE);
		episodeTabContent3.setVisibility(View.GONE);

		tab.setVisibility(View.VISIBLE);

		episodeTab1.setTextColor(getResources().getColor(R.color.black));
		episodeTab2.setTextColor(getResources().getColor(R.color.black));
		episodeTab3.setTextColor(getResources().getColor(R.color.black));

		episodeTab1.setBackgroundColor(android.R.color.transparent);
		episodeTab2.setBackgroundColor(android.R.color.transparent);
		episodeTab3.setBackgroundColor(android.R.color.transparent);

		if (tab.equals(episodeTabContent1))
		{
			episodeTab1.setTextColor(getResources().getColor(R.color.white));
			episodeTab1.setBackgroundColor(R.color.gray2);
		}
		else if (tab.equals(episodeTabContent2))
		{
			episodeTab2.setTextColor(getResources().getColor(R.color.white));
			episodeTab2.setBackgroundColor(R.color.gray2);
		}
		else if (tab.equals(episodeTabContent3))
		{
			episodeTab3.setTextColor(getResources().getColor(R.color.white));
			episodeTab3.setBackgroundColor(R.color.gray2);
		}
	}

	private void setHandler()
	{
		handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg)
			{

				switch (msg.what)
				{
					case ERROR:

						if (showEpisodeAnswer.errorNumber == 10)
						{
							if (!isFinishing())
							{
								AlertDialog alertDialog = new AlertDialog.Builder(ActivityEpisodeInfos.this).create();
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
						else if (commentsEpisodeAnswer.errorNumber == 10)
						{
							episodeTabContent2.removeView(episodeCommentairesLoading);
							Toast.makeText(ActivityEpisodeInfos.this, "Erreur lors du chargement des commentaires", Toast.LENGTH_SHORT).show();
						}
						else
						{
							Toast.makeText(ActivityEpisodeInfos.this, showEpisodeAnswer.errorNumber + " : " + showEpisodeAnswer.errorString, Toast.LENGTH_LONG).show();
						}

					break;

					case ACTION_EPISODE_LOADED:

						// petit hack
						showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).season = showEpisodeAnswer.json.root.seasons.get(0).number;
						
						title.setText(showTitle);
						episodeTitle.setText(showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).title);
						episodeSeason.setText(String.format("%02d", showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).season));
						episodeEpisode.setText(String.format("%02d", showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).episode));
						episodeDescription.setText(showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).description);

						if (showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).description.equals(""))
							episodeDescription.setText("Aucune description disponible");

						if (showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).has_seen == 1)
						{
							changeIcon(1);
						}
						else if (showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).date <= Tools.getTimestampOfTodayAtMidnight())
						{
							changeIcon(2);
						}
						else
						{
							changeIcon(3);
						}

						if (showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).date > Tools.getTimestampOfTodayAtMidnight())
							episodeDate.setText("Dans " + Tools.timestamp2timeleft(Integer.valueOf(showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).date)));
						else if (showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).date == Tools.getTimestampOfTodayAtMidnight())
							episodeDate.setText("Aujourd'hui");
						else
							episodeDate.setText(Tools.timestamp2date(showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).date));

						setScreen(SCREEN_CONTENT);

						if (showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).screen != null)
							setEpisodeImage(showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).screen, showUrl, "" + showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).season, "" + showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).episode);
						else
						{
							bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.banner_episode_nopreview);
							banner.setImageBitmap(bitmap);
							banner.setVisibility(View.VISIBLE);
						}

						setTab(episodeTabContent1);

						if (showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).comments == 0)
						{
							commentsEmpty();
						}
						else
						{
							commentsEpisode();
						}

						if (showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).subs != null)
						{
							int nbSubs = showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).subs.size();
							for (int i = 0; i < nbSubs; i++)
							{
								JsonSub jsonSub = showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0).subs.get(i);

								subtitleAdd(jsonSub.url, jsonSub.file, jsonSub.source, jsonSub.language);
							}
						}

					break;

					case ACTION_BANNER_LOADED:

						banner.setImageBitmap(bitmap);

						background.setVisibility(View.VISIBLE);
						banner.setVisibility(View.VISIBLE);

					break;

					case ACTION_COMMENTS_LOADED:

						if (commentsEpisodeAnswer.json.root.comments.size() == 0)
						{
							commentsEmpty();
						}
						else
						{
							episodeTabContent2.removeView(episodeCommentairesLoading);

							for (int i = 0; i < commentsEpisodeAnswer.json.root.comments.size(); i++)
							{
								JsonComment jsonComment = commentsEpisodeAnswer.json.root.comments.get(i);

								commentsAdd(jsonComment.login, jsonComment.date, jsonComment.text);
							}
						}

					break;

				}
			}
		};
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.itemEpisodeIcon:

				if (showArchived == true)
				{
					Toast.makeText(this, "Erreur : Cette serie est archivŽ. Vous ne pouvez donc la modifier.", Toast.LENGTH_LONG).show();
				}
				else
				{
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

								memberWatched(showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0), rate);
								changeIcon(1);

								// On met le badge a 0 car le nombre d'episodes non vu n'est plus
								// "accurate" lorsque l'on marque un episode vu/non vu (en effet
								// marquer un episode comme vu revient a marquer tout les episodes
								// prŽcŽdents comme vu)
								Prefs.getPreferences(ActivityEpisodeInfos.this).edit().putInt(Prefs.NB_PLANNING_UNSEEN, 0).commit();
								Prefs.getPreferences(ActivityEpisodeInfos.this).edit().putInt(Prefs.NB_EPISODES_UNSEEN, 0).commit();

								Prefs.getPreferences(ActivityEpisodeInfos.this).edit().putBoolean(Prefs.NEED_RELOAD_EPISODES, true).commit();

								dialog.dismiss();
							}
						});
						alertDialog.show();
					}
					else
					{
						memberWatched(showEpisodeAnswer.json.root.seasons.get(0).episodes.get(0), 0);
						changeIcon(1);

						// On met le badge a 0 car le nombre d'episodes non vu n'est plus
						// "accurate" lorsque l'on marque un episode vu/non vu (en effet
						// marquer un episode comme vu revient a marquer tout les episodes
						// prŽcŽdents comme vu)
						Prefs.getPreferences(ActivityEpisodeInfos.this).edit().putInt(Prefs.NB_PLANNING_UNSEEN, 0).commit();
						Prefs.getPreferences(ActivityEpisodeInfos.this).edit().putInt(Prefs.NB_EPISODES_UNSEEN, 0).commit();

						Prefs.getPreferences(this).edit().putBoolean(Prefs.NEED_RELOAD_EPISODES, true).commit();
					}
				}

			break;

			case R.id.episodeTab1:

				setTab(episodeTabContent1);

			break;

			case R.id.episodeTab2:

				setTab(episodeTabContent2);

			break;

			case R.id.episodeTab3:

				setTab(episodeTabContent3);

			break;

			case R.id.subtitleDownload:

				String fileUrl = (String) v.getTag();

				Intent i = new Intent(Intent.ACTION_VIEW);  
				i.setData(Uri.parse(fileUrl));  
				startActivity(i);  

			break;
		}

	}

	private void commentsEmpty()
	{
		Resources r = getResources();
		int px5 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());

		TextView noComments = new TextView(this);
		LinearLayout.LayoutParams noCommentsParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		noComments.setText("Aucun commentaire");
		noCommentsParams.setMargins(px5, px5, px5, px5);
		noComments.setLayoutParams(noCommentsParams);

		episodeTabContent2.removeAllViews();
		episodeTabContent2.addView(noComments);
	}

	private void commentsAdd(String username, int date, String text)
	{
		Resources r = getResources();
		int px2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, r.getDisplayMetrics());
		int px5 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());

		RelativeLayout itemLayout = new RelativeLayout(this);
		TextView usernameTextView = new TextView(this);
		TextView dateTextView = new TextView(this);
		TextView contentTextView = new TextView(this);
		View separatorView = new View(this);

		RelativeLayout.LayoutParams usernameTextViewParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams dateTextViewParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams contentTextViewParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams separatorViewParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, px2);

		usernameTextViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		usernameTextViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		usernameTextViewParams.setMargins(0, 0, 0, px5);

		dateTextViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		dateTextViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

		usernameTextView.setId(84674); // random
		contentTextViewParams.addRule(RelativeLayout.BELOW, usernameTextView.getId());

		usernameTextView.setLayoutParams(usernameTextViewParams);
		dateTextView.setLayoutParams(dateTextViewParams);
		contentTextView.setLayoutParams(contentTextViewParams);

		separatorView.setLayoutParams(separatorViewParams);
		separatorView.setBackgroundResource(R.color.gray3);

		usernameTextView.setText(username);
		usernameTextView.setTypeface(null, Typeface.BOLD);
		dateTextView.setText(Tools.timestamp2dateAndTime(date));
		dateTextView.setTypeface(null, Typeface.ITALIC);
		contentTextView.setText(text);

		itemLayout.addView(usernameTextView);
		itemLayout.addView(dateTextView);
		itemLayout.addView(contentTextView);

		itemLayout.setPadding(px5, px5, px5, px5);

		episodeTabContent2.addView(itemLayout);
		episodeTabContent2.addView(separatorView);
	}

	private void subtitleAdd(String url, String file, String source, String language)
	{
		View item = getLayoutInflater().inflate(R.layout.subtitle_item, null);

		((TextView) item.findViewById(R.id.subtitleTitle)).setText(file);
		((TextView) item.findViewById(R.id.subtitleSource)).setText(source);
		((ImageButton) item.findViewById(R.id.subtitleDownload)).setTag(url);

		language = language.toLowerCase();

		if (language.equals("vo"))
			((ImageView) item.findViewById(R.id.subtitleIcon)).setImageResource(R.drawable.vo);
		else if (language.equals("vf"))
			((ImageView) item.findViewById(R.id.subtitleIcon)).setImageResource(R.drawable.vf);
		else if (language.equals("vovf") || language.equals("vfvo"))
			((ImageView) item.findViewById(R.id.subtitleIcon)).setImageResource(R.drawable.vovf);
		else
			((ImageView) item.findViewById(R.id.subtitleIcon)).setVisibility(View.INVISIBLE);

		((ImageButton) item.findViewById(R.id.subtitleDownload)).setOnClickListener(this);

		episodeTabContent3.addView(item);
	}

	public void changeIcon(int state)
	{

		int pl = episodeIcon.getPaddingLeft();
		int pt = episodeIcon.getPaddingTop();
		int pr = episodeIcon.getPaddingRight();
		int pb = episodeIcon.getPaddingBottom();

		switch (state)
		{
			case 1:

				episodeIcon.setImageResource(R.drawable.seen);
				episodeIcon.setOnClickListener(null);
				episodeIcon.setBackgroundDrawable(null);

			break;

			case 2:

				episodeIcon.setImageResource(R.drawable.to_see);
				episodeIcon.setOnClickListener(ActivityEpisodeInfos.this);
				episodeIcon.setBackgroundDrawable(ActivityEpisodeInfos.this.getResources().getDrawable(R.drawable.episode_icon));

			break;

			case 3:

				episodeIcon.setImageResource(R.drawable.not_aired_yet);
				episodeIcon.setOnClickListener(null);
				episodeIcon.setBackgroundDrawable(null);

			break;
		}

		episodeIcon.setPadding(pl, pt, pr, pb);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		GAnalytics.getInstance().Track(this, "/episode/infos?url=" + showUrl);
	}
}
