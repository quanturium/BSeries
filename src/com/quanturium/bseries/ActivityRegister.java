package com.quanturium.bseries;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quanturium.bseries.apis.APIBetaSeries;
import com.quanturium.bseries.apis.APIBetaSeriesAnswer;
import com.quanturium.bseries.tools.GAnalytics;
import com.quanturium.bseries.tools.Prefs;

public class ActivityRegister extends Activity
{
	TextView				username;
	TextView				email;
	TextView				password;
	Button					validate;

	public APIBetaSeries	BetaSeries			= APIBetaSeries.getInstance();
	APIBetaSeriesAnswer		answer;

	static final int		ERROR				= -1;
	static final int		REGISTER_SUCCESS	= 0;

	ProgressDialog			dialog;
	Handler					answerHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		this.username = (EditText) findViewById(R.id.registerUsername);
		this.email = (EditText) findViewById(R.id.registerEmail);
		this.password = (EditText) findViewById(R.id.registerPassword);
		this.validate = (Button) findViewById(R.id.registerValidate);
		
		if (android.os.Build.VERSION.SDK_INT >= 14)
			getActionBar().setDisplayHomeAsUpEnabled(true);
		
		InputFilter filter = new InputFilter() { 
		    public CharSequence filter(CharSequence source, int start, int end, 
		        Spanned dest, int dstart, int dend)
		    {
		        for (int i = start; i < end; i++) { 
		            if (!Character.isLetter(source.charAt(i)) && !Character.isDigit(source.charAt(i))) { 
		                return ""; 
		            }
		        }
		        return null; 
		    } 
		}; 
		this.username.setFilters(new InputFilter[]{filter});

		setHandler();
		setActions();
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

	private void setActions()
	{
		this.validate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				if (!username.getText().toString().trim().equals("") && !password.getText().toString().trim().equals("") && !email.getText().toString().trim().equals(""))
				{
					memberSignup(username.getText().toString().trim(), email.getText().toString().trim(), password.getText().toString().trim());
				}
				else
				{
					if (username.getText().toString().trim().equals(""))
						username.setError("Champ obligatoire");

					if (email.getText().toString().trim().equals(""))
						email.setError("Champ obligatoire");

					if (password.getText().toString().trim().equals(""))
						password.setError("Champ obligatoire");
				}
			}
		});

		this.username.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (username.getText().toString().trim().equals("") && !username.isFocused())
					username.setError("Champ obligatoire");

				if (email.getText().toString().trim().equals("") && !email.isFocused())
					email.setError("Champ obligatoire");

				if (password.getText().toString().trim().equals("") && !password.isFocused())
					password.setError("Champ obligatoire");
			}
		});

		this.email.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (username.getText().toString().trim().equals("") && !username.isFocused())
					username.setError("Champ obligatoire");

				if (email.getText().toString().trim().equals("") && !email.isFocused())
					email.setError("Champ obligatoire");

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

				if (email.getText().toString().trim().equals("") && !email.isFocused())
					email.setError("Champ obligatoire");

				if (password.getText().toString().trim().equals("") && !password.isFocused())
					password.setError("Champ obligatoire");
			}
		});
	}

	private void memberSignup(final String username, final String email, final String password)
	{
		dialog = ProgressDialog.show(ActivityRegister.this, "", "Inscription en cours", true, false);

		new Thread(new Runnable() {

			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				answer = BetaSeries.memberSignup(username, email, password);

				if (answer.json.root.code == 1)
				{
					GAnalytics.getInstance().Track(ActivityRegister.this, "/register/ok");
					answerHandler.sendEmptyMessage(REGISTER_SUCCESS);
				}
				else
				{
					GAnalytics.getInstance().Track(ActivityRegister.this, "/register/error?code=" + answer.errorNumber);
					answerHandler.sendEmptyMessage(ERROR);
				}

				dialog.dismiss();
			}
		}).start();
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

						Toast.makeText(ActivityRegister.this, answer.errorNumber + " : " + answer.errorString, Toast.LENGTH_LONG).show();

					break;

					case REGISTER_SUCCESS:

						Prefs.getPreferences(ActivityRegister.this).edit().putString(Prefs.USERNAME, username.getText().toString()).commit();
						Prefs.getPreferences(ActivityRegister.this).edit().putString(Prefs.PASSWORD, password.getText().toString()).commit();

						setResult(ActivityHome.ACTIVITY_RESULT_LOGIN_CODE);
						finish();

					break;
				}
			};
		};
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		GAnalytics.getInstance().Track(this, "/register");
	}
}
