package com.tools.payhelper;


import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;

public class CustomApplcation extends Application {
//	static {//static 代码段可以防止内存泄露
//		//设置全局的Header构建器
//		SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
//			@NonNull
//			@Override
//			public RefreshHeader createRefreshHeader(@NonNull Context context, @NonNull RefreshLayout layout) {
//				layout.setPrimaryColorsId(R.color.black, android.R.color.white);//全局设置主题颜色
//				return new ClassicsHeader(context).setSpinnerStyle(SpinnerStyle.Translate);//指定为经典Header，默认是 贝塞尔雷达Header
//			}
//		});
//		SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
//			@NonNull
//			@Override
//			public RefreshFooter createRefreshFooter(@NonNull Context context, @NonNull RefreshLayout layout) {
//				return new ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.Translate);
//			}
//		});
//	}
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
