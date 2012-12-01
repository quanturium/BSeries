package com.quanturium.bseries.apis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class SocialFacebookListener implements RequestListener
{

	@Override
	public void onComplete(String response, Object state)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onIOException(IOException e, Object state)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFileNotFoundException(FileNotFoundException e, Object state)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMalformedURLException(MalformedURLException e, Object state)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFacebookError(FacebookError e, Object state)
	{
		// TODO Auto-generated method stub
		
	}

}
