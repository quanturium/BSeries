package com.quanturium.bseries;

import com.quanturium.bseries.tools.GAnalytics;
import com.quanturium.bseries.tools.Prefs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;

public class ActivityLogin extends Activity
{
	EditText				username;
	EditText				password;
	Button					validate;
	Button					register;

	public static final int	ACTIVITY_RESULT_REGISTER_CODE	= 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		if (android.os.Build.VERSION.SDK_INT >= 14)
			getActionBar().setDisplayHomeAsUpEnabled(true);

		this.username = (EditText) findViewById(R.id.loginUsername);
		this.password = (EditText) findViewById(R.id.loginPassword);
		this.validate = (Button) findViewById(R.id.loginValidate);
		this.register = (Button) findViewById(R.id.loginRegister);

		setDefaults();
		setActions();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == resultCode)
		{
			setDefaults();

			setResult(ActivityHome.ACTIVITY_RESULT_LOGIN_CODE);
			finish();
		}
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

	private void setDefaults()
	{
		this.username.setText(Prefs.getPreferences(this).getString(Prefs.USERNAME, ""));
		this.password.setText(Prefs.getPreferences(this).getString(Prefs.PASSWORD, ""));
	}

	private void setActions()
	{
		this.validate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				if (!username.getText().toString().trim().equals("") && !password.getText().toString().trim().equals(""))
				{
					Prefs.getPreferences(ActivityLogin.this).edit().putString(Prefs.USERNAME, username.getText().toString().trim()).commit();
					Prefs.getPreferences(ActivityLogin.this).edit().putString(Prefs.PASSWORD, password.getText().toString().trim()).commit();

					setResult(ActivityHome.ACTIVITY_RESULT_LOGIN_CODE);
					finish();
				}
				else
				{
					if (username.getText().toString().trim().equals(""))
						username.setError("Champ obligatoire");

					if (password.getText().toString().trim().equals(""))
						password.setError("Champ obligatoire");
				}
			}
		});

		this.register.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				startActivityForResult(new Intent(ActivityLogin.this, ActivityRegister.class), ACTIVITY_RESULT_REGISTER_CODE);
			}
		});

		this.username.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (username.getText().toString().trim().equals("") && !username.isFocused())
					username.setError("Champ obligatoire");

				if (password.getText().toString().trim().equals("") && !password.isFocused())
					password.setError("Champ obligatoire");
			}
		});

		this.password.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (username.getText().toString().trim().equals("") && !username.isFocused())
					username.setError("Champ obligatoire");

				if (password.getText().toString().trim().equals("") && !password.isFocused())
					password.setError("Champ obligatoire");
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		GAnalytics.getInstance().Track(this, "/login");
	}
}
