package com.quanturium.bseries;

import com.quanturium.bseries.tools.GAnalytics;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class ActivityTutorial extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
		
		if (android.os.Build.VERSION.SDK_INT >= 14)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		GAnalytics.getInstance().Track(this, "/help");
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
}
