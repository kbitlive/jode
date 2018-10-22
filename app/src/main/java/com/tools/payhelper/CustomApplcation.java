package com.tools.payhelper;


import android.app.Application;
import android.content.Context;
import android.util.Log;

import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class CustomApplcation extends Application {

	public static CustomApplcation mInstance;
	private static Context context;
	private boolean isDisConnect;//是否主动断开
	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		mInstance = this;
		SQLiteStudioService.instance().start(this);
	}

	public static CustomApplcation getInstance() {
		return mInstance;
	}

	public static Context getContext() {
		return context;
	}

	public static CustomApplcation getmInstance() {
		return mInstance;
	}

	public static void setmInstance(CustomApplcation mInstance) {
		CustomApplcation.mInstance = mInstance;
	}

	public static void setContext(Context context) {
		CustomApplcation.context = context;
	}

	public boolean isDisConnect() {
		return isDisConnect;
	}

	public void setDisConnect(boolean disConnect) {
		isDisConnect = disConnect;
	}
}
