package com.quanturium.bseries.apis;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.SessionStore;
import com.quanturium.bseries.tools.Constant;
import com.twitter.android.TwitterApp;

public class Social
{
	public static final int	TWITTER		= 1;
	public static final int	FACEBOOK	= 2;

	private Context			context;
	private List<Integer>	networks	= new ArrayList<Integer>();

	public Social(Context context)
	{
		this.context = context;
	}

	public void addNetwork(int network)
	{
		this.networks.add(network);
	}

	public void publish(String message, String name, String caption, String description, String image, String link)
	{
		for (Integer network : networks)
		{
			switch (network)
			{
				case TWITTER:

					publishTwitter(message, name, caption, description, image, link);

				break;

				case FACEBOOK:

					publishFacebook(message, name, caption, description, image, link);

				break;
			}
		}
	}

	private void publishFacebook(String message, String name, String caption, String description, String image, String link)
	{
		Facebook mFacebook = new Facebook(Constant.FACEBOOK_APP_ID);
		SessionStore.restore(mFacebook, context);

		if (mFacebook.isSessionValid())
		{

			AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(mFacebook);

			Bundle params = new Bundle();
			params.putString("message", message);
			params.putString("name", name);
			params.putString("caption", caption);
			params.putString("description", description);
			params.putString("picture", image);

			mAsyncRunner.request("me/feed", params, "POST", new SocialFacebookListener(), null);

		}
	}

	private void publishTwitter(String message, String name, String caption, String description, String image, String link)
	{
		final TwitterApp mTwitter = new TwitterApp(this.context, Constant.TWITTER_CONSUMER_KEY, Constant.TWITTER_SECRET_KEY);
		mTwitter.setListener(new SocialTwitterListener());

		if (mTwitter.hasAccessToken())
		{

			new Thread() {
				@Override
				public void run()
				{
					try
					{
						mTwitter.updateStatus("Test");
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}.start();

		}
	}
}
