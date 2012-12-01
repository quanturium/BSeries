package com.quanturium.bseries;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(formKey = "dC12SjBHckV3VF91SHJPMDB3djY5UGc6MQ") 
public class MyApplication extends Application
{
	@Override
	public void onCreate()
	{
		ACRA.init(this);
		super.onCreate();
	}
}
