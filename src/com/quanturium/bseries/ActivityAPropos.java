package com.quanturium.bseries;

import com.quanturium.bseries.tools.GAnalytics;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class ActivityAPropos extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.pref_item_a_propos));
		
		if (android.os.Build.VERSION.SDK_INT >= 14)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_a_propos);

		GAnalytics.getInstance().Track(this, "/about");
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
