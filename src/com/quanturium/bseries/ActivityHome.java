package com.quanturium.bseries;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quanturium.bseries.adapters.HomeAdapter;
import com.quanturium.bseries.apis.APIBetaSeries;
import com.quanturium.bseries.apis.APIBetaSeriesAnswer;
import com.quanturium.bseries.tools.Cache;
import com.quanturium.bseries.tools.Constant;
import com.quanturium.bseries.tools.GAnalytics;
import com.quanturium.bseries.tools.Prefs;
import com.quanturium.bseries.tools.Tools;

public class ActivityHome extends Activity implements OnItemClickListener, OnClickListener
{

	private HomeAdapter				homeAdapter;

	public APIBetaSeries			BetaSeries					= APIBetaSeries.getInstance();
	private APIBetaSeriesAnswer		answer;

	private Handler					answerHandler;

	public List<HomeItem>			items						= new ArrayList<HomeItem>();

	private static final int		ERROR						= -1;
	private static final int		ACTION_LOGIN_SUCCESS		= 0;
	private static final int		ACTION_LOGOUT_SUCCESS		= 1;
	private static final int		ACTION_LOGIN_FAILED			= 2;

	private static final int		ACTION_HOME_PLANNING		= 4;
	private static final int		ACTION_HOME_EPISODES		= 9;
	private static final int		ACTION_HOME_SERIES			= 5;
	private static final int		ACTION_HOME_FRIENDS			= 6;
	private static final int		ACTION_HOME_PROFILE			= 7;
	private static final int		ACTION_HOME_BADGES			= 8;

	private static final int		SCREEN_EMPTY				= 1;
	private static final int		SCREEN_LOADER				= 2;
	private static final int		SCREEN_CONTENT				= 3;

	public static final int			ACTIVITY_RESULT_LOGIN_CODE	= 1;

	private RelativeLayout			loadingScreen;
	private RelativeLayout			emptyScreen;
	private GridView				contentScreen;
	private LinearLayout			notificationLayout			= null;

	private int						nbPlanningUnseen			= 0;
	private int						nbEpisodesUnseen			= 0;

	private UpdateBadgesReceiver	updateBadgeReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		loadingScreen = (RelativeLayout) findViewById(R.id.loadingScreen);
		emptyScreen = (RelativeLayout) findViewById(R.id.emptyScreen);
		contentScreen = (GridView) findViewById(R.id.contentScreen);
		notificationLayout = (LinearLayout) findViewById(R.id.notificationLayout);

		if (Prefs.getPreferences(this).getBoolean(Prefs.PLANNING_BADGE, true) || Prefs.getPreferences(this).getBoolean(Prefs.EPISODES_BADGE, true))
			setService();

		setHandler();

		if (Prefs.getPreferences(this).getBoolean(Prefs.FIRST_RUN, true))
			firstRun();
		else
		{
			if (isNewVersion())
				showChangelogDialog();

			if (Prefs.getPreferences(this).getString(Prefs.TOKEN, "").equals(""))
				setScreen(SCREEN_EMPTY);
			else
				setScreen(SCREEN_CONTENT);
		}

		nbPlanningUnseen = Prefs.getPreferences(this).getInt(Prefs.NB_PLANNING_UNSEEN, 0);
		nbEpisodesUnseen = Prefs.getPreferences(this).getInt(Prefs.NB_EPISODES_UNSEEN, 0);

		fillItems();

		this.homeAdapter = new HomeAdapter(ActivityHome.this, items);
		contentScreen.setAdapter(this.homeAdapter);
		contentScreen.setOnItemClickListener(this);

		changeGridColumn(Tools.getScreenOrientation(this));

		notificationLayout.setOnClickListener(this);
	}

	private void setNotifications(boolean enabled)
	{
		if (enabled)
		{
			notificationLayout.setVisibility(View.VISIBLE);

			int nbNotifs = Prefs.getPreferences(this).getInt(Prefs.NOTIFICATIONS_COUNT, 0);
			TextView notificationsView = (TextView) findViewById(R.id.notificationCount);

			notificationsView.setText("" + nbNotifs);

			if (nbNotifs > 0)
			{
				notificationsView.setTextColor(getResources().getColor(R.color.red1));
			}
			else
			{
				notificationsView.setTextColor(getResources().getColor(R.color.white));
			}
		}
		else
		{
			if (notificationLayout != null)
				notificationLayout.setVisibility(View.GONE);
		}
	}

	private void setService()
	{
		Intent intent = new Intent(ActivityHome.this, ServiceUpdates.class);
		intent.setAction(Constant.ACTION_BETASERIES_DATA);
		PendingIntent pintent = PendingIntent.getService(ActivityHome.this, 0, intent, 0);

		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (1000 * 60 * Integer.valueOf(Prefs.getPreferences(this).getString(Prefs.BADGE_RELOAD_INTERVAL, "60"))), pintent);
	}

	@Override
	protected void onResume()
	{
		GAnalytics.getInstance().Track(this, "/");

		super.onResume();

		if (updateBadgeReceiver == null)
			updateBadgeReceiver = new UpdateBadgesReceiver();
		IntentFilter intentFilter = new IntentFilter(Constant.ACTION_UPDATE_BADGES);
		registerReceiver(updateBadgeReceiver, intentFilter);

		if (Prefs.getPreferences(this).getBoolean(Prefs.SERVICE_CHANGED, false))
		{
			Prefs.getPreferences(this).edit().putBoolean(Prefs.SERVICE_CHANGED, false).commit();

			nbPlanningUnseen = -1; // on veut une valeur qui n'existe pas pour
									// actualiser le badge
			nbEpisodesUnseen = -1; // on veut une valeur qui n'existe pas pour
									// actualiser le badge
			setService(); // Remettre a jour l'interval de check
		}

		if (Prefs.getPreferences(this).getInt(Prefs.NB_PLANNING_UNSEEN, 0) != nbPlanningUnseen || Prefs.getPreferences(this).getInt(Prefs.NB_EPISODES_UNSEEN, 0) != nbEpisodesUnseen)
		{
			nbPlanningUnseen = Prefs.getPreferences(this).getInt(Prefs.NB_PLANNING_UNSEEN, 0);
			this.items.get(0).badge = nbPlanningUnseen;

			nbEpisodesUnseen = Prefs.getPreferences(this).getInt(Prefs.NB_EPISODES_UNSEEN, 0);
			this.items.get(1).badge = nbEpisodesUnseen;

			this.homeAdapter.notifyDataSetChanged();
		}

		if (Prefs.getPreferences(this).getBoolean(Prefs.NOTIFICATIONS_ENABLED, true))
		{
			setNotifications(true);
		}
		else
		{
			setNotifications(false);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (updateBadgeReceiver != null)
			unregisterReceiver(updateBadgeReceiver);
	}

	public class UpdateBadgesReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent.getAction().equals(Constant.ACTION_UPDATE_BADGES))
				onResume();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == requestCode)
		{
			memberLogin();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		changeGridColumn(Tools.getScreenOrientation(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		if (Prefs.getPreferences(this).getString(Prefs.TOKEN, "").equals(""))
		{
			menu.findItem(R.id.login).setVisible(true);
			menu.findItem(R.id.logout).setVisible(false);
			menu.findItem(R.id.createAccount).setVisible(true);
		}
		else
		{
			menu.findItem(R.id.login).setVisible(false);
			menu.findItem(R.id.logout).setVisible(true);
			menu.findItem(R.id.createAccount).setVisible(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.parameters:

				startActivity(new Intent(ActivityHome.this, ActivityPreferences.class));

			break;
			case R.id.login:

				startActivityForResult(new Intent(ActivityHome.this, ActivityLogin.class), ACTIVITY_RESULT_LOGIN_CODE);

			break;

			case R.id.logout:

				memberLogout();

			break;

			case R.id.createAccount:

				startActivityForResult(new Intent(ActivityHome.this, ActivityRegister.class), ACTIVITY_RESULT_LOGIN_CODE);

			break;

			case R.id.tutorial:

				startActivity(new Intent(ActivityHome.this, ActivityTutorial.class));

			break;
		}

		return true;
	}

	private void memberLogin()
	{
		((TextView) loadingScreen.getChildAt(0)).setText(R.string.loading_login);
		setScreen(SCREEN_LOADER);

		new Thread(new Runnable() {

			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				answer = BetaSeries.memberLogin(Prefs.getPreferences(ActivityHome.this).getString(Prefs.USERNAME, ""), Prefs.getPreferences(ActivityHome.this).getString(Prefs.PASSWORD, ""));

				if (answer.json.root.code == 1)
				{
					String token = answer.json.root.member.token;
					Prefs.getPreferences(ActivityHome.this).edit().putString(Prefs.TOKEN, token).commit();

					GAnalytics.getInstance().Track(ActivityHome.this, "/login/ok");
					answerHandler.sendEmptyMessage(ACTION_LOGIN_SUCCESS);
				}
				else
				{
					GAnalytics.getInstance().Track(ActivityHome.this, "/login/error?code=" + answer.errorNumber);

					Prefs.getPreferences(ActivityHome.this).edit().putString(Prefs.TOKEN, "").commit();
					answerHandler.sendEmptyMessage(ERROR);

					if (answer.errorNumber == 4002 || answer.errorNumber == 4003)
						answerHandler.sendEmptyMessage(ACTION_LOGIN_FAILED);
				}
			}
		}).start();
	}

	private void memberLogout()
	{
		((TextView) loadingScreen.getChildAt(0)).setText(R.string.loading_logout);
		setScreen(SCREEN_LOADER);

		new Thread(new Runnable() {

			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				answer = BetaSeries.memberLogout(Prefs.getPreferences(ActivityHome.this).getString(Prefs.TOKEN, ""));

				Prefs.getPreferences(ActivityHome.this).edit().putString(Prefs.TOKEN, "").commit();
				Prefs.getPreferences(ActivityHome.this).edit().putInt(Prefs.NB_PLANNING_UNSEEN, 0).commit();
				Prefs.getPreferences(ActivityHome.this).edit().putInt(Prefs.NB_EPISODES_UNSEEN, 0).commit();
				answerHandler.sendEmptyMessage(ACTION_LOGOUT_SUCCESS);

				if (answer.json.root.code == 1)
				{
					GAnalytics.getInstance().Track(ActivityHome.this, "/logout/ok");
				}
				else
				{
					GAnalytics.getInstance().Track(ActivityHome.this, "/logout/error?code=" + answer.errorNumber);
					answerHandler.sendEmptyMessage(ERROR);
				}
			}
		}).start();
	}

	private void firstRun()
	{
		setScreen(SCREEN_EMPTY);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.firstRun_message);
		builder.setCancelable(false);
		builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				Prefs.getPreferences(ActivityHome.this).edit().putBoolean(Prefs.FIRST_RUN, false).commit();
				startActivityForResult(new Intent(ActivityHome.this, ActivityLogin.class), ACTIVITY_RESULT_LOGIN_CODE);
			}
		});

		AlertDialog alert = builder.create();
		alert.show();

		GAnalytics.getInstance().Track(this, "/firstrun");
	}

	private void fillItems()
	{
		items.add(new HomeItem(R.string.home_planning, R.drawable.home_planning, nbPlanningUnseen));
		items.add(new HomeItem(R.string.home_episodes, R.drawable.home_episodes, nbEpisodesUnseen));
		items.add(new HomeItem(R.string.home_series, R.drawable.home_series, 0));
		items.add(new HomeItem(R.string.home_profile, R.drawable.home_profile, 0));
		items.add(new HomeItem(R.string.home_friends, R.drawable.home_friends, 0));
		// items.add(new HomeItem(R.string.home_badges,
		// R.drawable.home_badges,0));
	}

	private void setScreen(int screen)
	{
		switch (screen)
		{
			case SCREEN_CONTENT:

				emptyScreen.setVisibility(View.GONE);
				loadingScreen.setVisibility(View.GONE);
				contentScreen.setVisibility(View.VISIBLE);

			break;

			case SCREEN_EMPTY:

				contentScreen.setVisibility(View.GONE);
				loadingScreen.setVisibility(View.GONE);
				emptyScreen.setVisibility(View.VISIBLE);

			break;

			case SCREEN_LOADER:

				contentScreen.setVisibility(View.GONE);
				emptyScreen.setVisibility(View.GONE);
				loadingScreen.setVisibility(View.VISIBLE);

			break;
		}

		if (android.os.Build.VERSION.SDK_INT >= 11)
			invalidateOptionsMenu();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{

		switch (arg2)
		{
			case 0: // Planning

				answerHandler.sendEmptyMessage(ACTION_HOME_PLANNING);

			break;

			case 1: // Mes episodes

				answerHandler.sendEmptyMessage(ACTION_HOME_EPISODES);

			break;

			case 2: // Mes series

				answerHandler.sendEmptyMessage(ACTION_HOME_SERIES);

			break;

			case 3: // Profil

				answerHandler.sendEmptyMessage(ACTION_HOME_PROFILE);

			break;

			case 4: // Amis

				answerHandler.sendEmptyMessage(ACTION_HOME_FRIENDS);

			break;

			case 5: // Badges

				answerHandler.sendEmptyMessage(ACTION_HOME_BADGES);

			break;

		}

	}

	public void changeGridColumn(int orientation)
	{
		switch (orientation)
		{
			case Configuration.ORIENTATION_LANDSCAPE:
				contentScreen.setNumColumns(5);
			break;

			default:
				contentScreen.setNumColumns(2);
			break;
		}
	}

	public void setHandler()
	{
		answerHandler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg)
			{
				switch (msg.what)
				{
					case ERROR:

						setScreen(SCREEN_EMPTY);
						Toast.makeText(ActivityHome.this, answer.errorNumber + " : " + answer.errorString, Toast.LENGTH_LONG).show();

					break;

					case ACTION_LOGIN_SUCCESS:

						setScreen(SCREEN_CONTENT);
						setService();

					break;

					case ACTION_LOGIN_FAILED:

						setScreen(SCREEN_EMPTY);
						startActivityForResult(new Intent(ActivityHome.this, ActivityLogin.class), ACTIVITY_RESULT_LOGIN_CODE);

					break;

					case ACTION_LOGOUT_SUCCESS:

						setScreen(SCREEN_EMPTY);

					break;

					case ACTION_HOME_PLANNING:

						startActivity(new Intent(ActivityHome.this, ActivityPlanning.class));

					break;

					case ACTION_HOME_EPISODES:

						startActivity(new Intent(ActivityHome.this, ActivityEpisodesUnseen.class));

					break;

					case ACTION_HOME_SERIES:

						startActivity(new Intent(ActivityHome.this, ActivityShows.class));

					break;

					case ACTION_HOME_FRIENDS:

						// startActivity(new Intent(ActivityHome.this,
						// ActivityFriends.class));
						Toast.makeText(ActivityHome.this, "Disponible prochainement", Toast.LENGTH_SHORT).show();

					break;

					case ACTION_HOME_PROFILE:

						startActivity(new Intent(ActivityHome.this, ActivityUser.class));

					break;

					case ACTION_HOME_BADGES:

						// startActivity(new Intent(ActivityHome.this,
						// ActivityBadges.class));
						Toast.makeText(ActivityHome.this, "Disponible prochainement", Toast.LENGTH_SHORT).show();

					break;
				}
			};
		};
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.notificationLayout:

				startActivity(new Intent(ActivityHome.this, ActivityNotifications.class));

			break;
		}
	}

	private boolean isNewVersion()
	{
		int currentVersion = 0;
		int savedVersion = 0;

		try
		{
			currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			savedVersion = Prefs.getPreferences(this).getInt(Prefs.VERSION_KEY, -1);

			if (currentVersion > savedVersion)
			{
				Prefs.getPreferences(this).edit().putInt(Prefs.VERSION_KEY, currentVersion).commit();

				if (savedVersion != -1) // premier lancement
					return true;
			}
		}
		catch (NameNotFoundException e)
		{

		}

		return false;
	}

	private void showChangelogDialog()
	{
		LayoutInflater inflater = LayoutInflater.from(this);

		View view = inflater.inflate(R.layout.dialog_changelog, null);

		Builder builder = new AlertDialog.Builder(this);

		builder.setView(view).setTitle("ChangeLog").setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});

		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
}
