package com.quanturium.bseries.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs
{
	public final static String	FIRST_RUN					= "first_run";
	public final static String	USERNAME					= "username";
	public final static String	PASSWORD					= "password";
	public final static String	TOKEN						= "token";
	public final static String	SERVICE_CHANGED				= "service_changed";
	public final static String	NEED_RELOAD_SHOWS			= "need_reload_shows";
	public final static String	NEED_RELOAD_EPISODES		= "need_reload_episodes";
	public final static String	NB_EPISODES_UNSEEN			= "nb_episodes_unseen";
	public final static String	NB_PLANNING_UNSEEN			= "nb_planning_unseen";
	public final static String	DECALLAGE_1_JOUR			= "decallage_1_jour";
	public final static String	DECALLAGE_1_JOUR_ENABLED	= "decallage_1_jour_enabled";
	public final static String	PLANNING_HIDE_NOTAIRED		= "planning_hide_notaired";
	public final static String	PLANNING_BADGE				= "planning_badge";
	public final static String	EPISODES_BADGE				= "episodes_badge";
	public final static String	EPISODES_ONLYONE			= "episodes_onlyone";
	public final static String	BADGE_RELOAD_INTERVAL		= "badge_reload_interval";
	public final static String	ADD_MARK					= "add_mark";
	public final static String	NOTIFICATIONS_ENABLED		= "notifications_enabled";
	public final static String	NOTIFICATIONS_COUNT			= "notifications_count";

	public final static String	SOCIAL_FACEBOOK				= "social_facebook";
	public final static String	SOCIAL_FACEBOOK_NAME		= "social_facebook_name";
	public final static String	SOCIAL_TWITTER				= "social_twitter";
	public final static String	SOCIAL_TWITTER_NAME			= "social_twitter_name";
	
	public final static String	VERSION_KEY			= "version_key";

	public static SharedPreferences getPreferences(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context);
	}
}
