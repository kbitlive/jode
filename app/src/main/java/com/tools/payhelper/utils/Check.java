package com.tools.payhelper.utils;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Check.java
 * @description:
 * @author Andrew Lee
 * @version 
 * 2014-10-22 上午10:31:22
 */
public class Check {

	private SharedPreferences preferences;

	public static boolean is_login(Context context){
		if (context == null) {
			return false;
		}
		if(PreferencesUtils.getBooleanFromSPMap(context, PreferencesUtils.Keys.IS_LOGIN)){
			return true;
		}else{
			return false;
		}
		
	}
}
