package com.quanturium.bseries;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;
import com.quanturium.bseries.json.JsonMember;

public class ActivityFriends extends Activity
{

	private JsonMember	memberInfosJson;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_episodes);
		setTitle("BSeries : Friends");

		Intent i = getIntent();
		Gson gson = new Gson();
		String temp = i.getStringExtra("json");

		if (temp != null)
			memberInfosJson = gson.fromJson(temp, JsonMember.class);
		else
			finish();

	}
}
