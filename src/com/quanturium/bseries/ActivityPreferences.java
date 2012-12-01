package com.quanturium.bseries;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionStore;
import com.quanturium.bseries.tools.Cache;
import com.quanturium.bseries.tools.Constant;
import com.quanturium.bseries.tools.Prefs;
import com.twitter.android.TwitterApp;
import com.twitter.android.TwitterApp.TwDialogListener;

public class ActivityPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	private ProgressDialog			mProgress;

	private Facebook				mFacebook				= null;
	private TwitterApp				mTwitter				= null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		if (android.os.Build.VERSION.SDK_INT >= 14)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		PreferenceManager.setDefaultValues(ActivityPreferences.this, R.xml.preferences, false);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		Preference cache_clear = findPreference("cache_clear");

		cache_clear.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference)
			{

				AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPreferences.this);
				builder.setMessage(getString(R.string.pref_confirm_cache_clear)).setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						Cache.removeAll(ActivityPreferences.this);
						dialog.cancel();
						Toast.makeText(ActivityPreferences.this, getString(R.string.pref_confirm_cache_cleared), Toast.LENGTH_SHORT).show();
					}
				}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id)
					{
						dialog.cancel();
					}
				});

				AlertDialog alert = builder.create();
				alert.show();

				return true;
			}
		});

		Preference a_propos = findPreference("a_propos");

		a_propos.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference)
			{

				startActivity(new Intent(ActivityPreferences.this, ActivityAPropos.class));

				return true;
			}
		});

		Preference decallage_1_jour = findPreference("decallage_1_jour");

		if (!Prefs.getPreferences(this).getBoolean(Prefs.DECALLAGE_1_JOUR_ENABLED, true))
		{
			decallage_1_jour.setEnabled(false);
			decallage_1_jour.setSummary(R.string.pref_sum_decalage_1_jour_disabled);
		}

		setFacebookAction();
		setTwitterAction();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(Prefs.PLANNING_BADGE) || key.equals(Prefs.EPISODES_BADGE) || key.equals(Prefs.BADGE_RELOAD_INTERVAL))
			Prefs.getPreferences(this).edit().putBoolean(Prefs.SERVICE_CHANGED, true).commit();
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

	/**** FACEBOOK ******/

	public void setFacebookAction()
	{
		Preference social_facebook = findPreference(Prefs.SOCIAL_FACEBOOK);
		mProgress = new ProgressDialog(this);
		mFacebook = new Facebook(Constant.FACEBOOK_APP_ID);

		mProgress.setCancelable(false);
		mProgress.requestWindowFeature(Window.FEATURE_NO_TITLE);

		SessionStore.restore(mFacebook, this);

		if (mFacebook.isSessionValid())
		{
			String username = SessionStore.getName(this);
			username = (username.equals("")) ? "N/A" : username;

			Prefs.getPreferences(ActivityPreferences.this).edit().putString(Prefs.SOCIAL_FACEBOOK_NAME, username).commit();
			social_facebook.setSummary("Connecté à Facebook (" + username + ")");
		}

		social_facebook.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(final Preference preference)
			{
				preference.setEnabled(false);

				if (mFacebook.isSessionValid()) // deconnexion
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPreferences.this);

					builder.setMessage("Êtes-vous sur de vouloir vous deconnecter ?").setCancelable(false).setPositiveButton("Oui", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id)
						{
							fbLogout();
						}
					}).setNegativeButton("Non", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id)
						{
							dialog.cancel();
							preference.setEnabled(true);
						}
					});

					final AlertDialog alert = builder.create();

					alert.show();
				}
				else
				// connexion
				{
					mFacebook.authorize(ActivityPreferences.this, Constant.FACEBOOK_PERMISSIONS, -1, new FbLoginDialogListener());
				}

				return true;
			}
		});

	}

	private final class FbLoginDialogListener implements DialogListener
	{
		public void onComplete(Bundle values)
		{
			SessionStore.save(mFacebook, ActivityPreferences.this);
			getFbName();
		}

		public void onFacebookError(FacebookError error)
		{
			Toast.makeText(ActivityPreferences.this, "Erreur lors de la connexion à Facebook", Toast.LENGTH_SHORT).show();

			findPreference(Prefs.SOCIAL_FACEBOOK).setEnabled(true);
		}

		public void onError(DialogError error)
		{
			Toast.makeText(ActivityPreferences.this, "Erreur lors de la connexion à Facebook", Toast.LENGTH_SHORT).show();

			findPreference(Prefs.SOCIAL_FACEBOOK).setEnabled(true);
		}

		public void onCancel()
		{
			findPreference(Prefs.SOCIAL_FACEBOOK).setEnabled(true);
		}
	}

	private void getFbName()
	{
		mProgress.setMessage("Finalisation de la connexion");
		mProgress.show();

		new Thread() {
			@Override
			public void run()
			{
				String name = "";
				int what = 1;

				try
				{
					String me = mFacebook.request("me");

					JSONObject jsonObj = (JSONObject) new JSONTokener(me).nextValue();
					name = jsonObj.getString("name");
					what = 0;
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}

				mFbHandler.sendMessage(mFbHandler.obtainMessage(what, name));
			}
		}.start();
	}

	private void fbLogout()
	{
		mProgress.setMessage("Deconnexion de Facebook");
		mProgress.show();

		new Thread() {
			@Override
			public void run()
			{
				SessionStore.clear(ActivityPreferences.this);

				int what = 1;

				try
				{
					mFacebook.logout(ActivityPreferences.this);

					what = 0;
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what));
			}
		}.start();
	}

	private Handler	mFbHandler	= new Handler() {
									@Override
									public void handleMessage(Message msg)
									{
										mProgress.dismiss();
										findPreference(Prefs.SOCIAL_FACEBOOK).setEnabled(true);

										if (msg.what == 0)
										{
											String username = (String) msg.obj;
											username = (username.equals("")) ? "N/A" : username;

											SessionStore.saveName(username, ActivityPreferences.this);

											Prefs.getPreferences(ActivityPreferences.this).edit().putString(Prefs.SOCIAL_FACEBOOK_NAME, username).commit();
											findPreference(Prefs.SOCIAL_FACEBOOK).setSummary("Connecté à Facebook (" + username + ")");

											Toast.makeText(ActivityPreferences.this, "Connecté à Facebook (" + username + ")", Toast.LENGTH_SHORT).show();
										}
										else
										{
											Toast.makeText(ActivityPreferences.this, "Connecté à Facebook", Toast.LENGTH_SHORT).show();
										}
									}
								};

	private Handler	mHandler	= new Handler() {
									@Override
									public void handleMessage(Message msg)
									{
										mProgress.dismiss();
										findPreference(Prefs.SOCIAL_FACEBOOK).setEnabled(true);

										if (msg.what == 1)
										{
											Toast.makeText(ActivityPreferences.this, "Erreur lor de la deconnexion à Facebook", Toast.LENGTH_SHORT).show();
										}
										else
										{
											Prefs.getPreferences(ActivityPreferences.this).edit().putString(Prefs.SOCIAL_FACEBOOK_NAME, "").commit();
											findPreference(Prefs.SOCIAL_FACEBOOK).setSummary(R.string.pref_sum_facebook);
											Toast.makeText(ActivityPreferences.this, "Déconnecté de Facebook", Toast.LENGTH_SHORT).show();
										}
									}
								};

	/**** TWITTER ******/

	public void setTwitterAction()
	{
		Preference social_twitter = findPreference("social_twitter");

		mTwitter = new TwitterApp(this, Constant.TWITTER_CONSUMER_KEY, Constant.TWITTER_SECRET_KEY);
		mTwitter.setListener(mTwLoginDialogListener);

		if (mTwitter.hasAccessToken())
		{
			String username = mTwitter.getUsername();
			username = (username.equals("")) ? "Unknown" : username;

			Prefs.getPreferences(ActivityPreferences.this).edit().putString(Prefs.SOCIAL_TWITTER_NAME, username).commit();
			social_twitter.setSummary("Connecté à Twitter (" + username + ")");
		}

		social_twitter.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(final Preference preference)
			{
				preference.setEnabled(false);

				if (mTwitter.hasAccessToken())
				{
					final AlertDialog.Builder builder = new AlertDialog.Builder(ActivityPreferences.this);

					builder.setMessage("Êtes-vous sur de vouloir vous deconnecter ?").setCancelable(false).setPositiveButton("Oui", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id)
						{
							mTwitter.resetAccessToken();
							preference.setEnabled(true);
							preference.setSummary(R.string.pref_sum_twitter);
							Toast.makeText(ActivityPreferences.this, "Déconnecté de Twitter", Toast.LENGTH_LONG).show();
							Prefs.getPreferences(ActivityPreferences.this).edit().putString(Prefs.SOCIAL_TWITTER_NAME, "").commit();
						}
					}).setNegativeButton("Non", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id)
						{
							dialog.cancel();
							preference.setEnabled(true);
						}
					});

					final AlertDialog alert = builder.create();

					alert.show();
				}
				else
				{
					mTwitter.authorize();
				}

				return true;
			}
		});
	}

	private final TwDialogListener	mTwLoginDialogListener	= new TwDialogListener() {
																@Override
																public void onComplete(String value)
																{
																	String username = mTwitter.getUsername();
																	username = (username.equals("")) ? "No Name" : username;

																	Prefs.getPreferences(ActivityPreferences.this).edit().putString(Prefs.SOCIAL_TWITTER_NAME, username).commit();
																	findPreference(Prefs.SOCIAL_TWITTER).setEnabled(true);
																	findPreference(Prefs.SOCIAL_TWITTER).setSummary("Connecté à Twitter (" + username + ")");

																	Toast.makeText(ActivityPreferences.this, "Connecté à Twitter (" + username + ")", Toast.LENGTH_LONG).show();
																}

																@Override
																public void onError(String value)
																{
																	Toast.makeText(ActivityPreferences.this, "Erreur lors de la connexion à Twitter", Toast.LENGTH_LONG).show();
																	findPreference(Prefs.SOCIAL_TWITTER).setEnabled(true);
																}

																@Override
																public void onCancel()
																{
																	findPreference(Prefs.SOCIAL_TWITTER).setEnabled(true);
																}
															};
}
