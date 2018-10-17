package com.tools.payhelper.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class PreferencesUtils {
	
	
	public static class Keys {

		public static final String IS_LOGIN = "is_login";
		/**
		 * sharedPreference 文件名
		 */
		public static final String USERINFO = "userInfo";
		public static final String OTHER = "other";


	}
	public static class keyat{
		public static String OTHER="other";
	}
	private static SharedPreferences preferences;
	
	/**
	 * 存储布尔值
	 * 
	 * @param mContext
	 * @param key
	 * @param value
	 */
	public static void putBooleanToSPMap(Context mContext, String key,
                                         boolean value) {
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}
	public static void putBooleanToSPMap(Context mContext, String keyat, String key,
                                         boolean value){
		preferences = mContext.getSharedPreferences(keyat,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}

	public static void putIntToSPMap(Context mContext, String key, int value) {
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putInt(key, value);
		edit.commit();
	}
	public static void putIntToSPMap(Context mContext, String KEYAT, String key, int value) {
		preferences = mContext.getSharedPreferences(KEYAT,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putInt(key, value);
		edit.commit();
	}

	public static int getIntFromSPMap(Context mContext, String key) {
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		int value = preferences.getInt(key, 0);
		return value;
	}
	public static int getIntFromSPMap(Context mContext, String KEYAT, String key) {
		preferences = mContext.getSharedPreferences(KEYAT,
				Context.MODE_PRIVATE);
		int value = preferences.getInt(key, 0);
		return value;
	}
	public static void putLongToSPMap(Context mContext, String key, long value){
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putLong(key, value);
		edit.commit();
	}
	public static long getLongFromSPMap(Context mContext, String key, long defaultvalue){
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		long values = preferences.getLong(key, defaultvalue);
		return values;

	}
	public static void putFloatToSPMap(Context mContext, String key, Float value){
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putFloat(key, value);
		edit.commit();
	}
	public static float getFloatFromSPMap(Context mContext, String key){
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		float values = preferences.getFloat(key, 0);
		return values;
		
	}

	/**
	 * 获取布尔值
	 * 
	 * @param mContext
	 * @param key
	 * @return
	 */
	public static Boolean getBooleanFromSPMap(Context mContext, String key) {
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		boolean value = preferences.getBoolean(key, false);
		return value;
	}
	/**
	 * 获取布尔值
	 *
	 * @param mContext
	 * @param key
	 * @return
	 */
	public static Boolean getBooleanFromSPMap(Context mContext, String key, boolean defaultValue) {
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		boolean value = preferences.getBoolean(key, defaultValue);
		return value;
	}
	public static Boolean getBooleanFromSPMap(Context mContext, String keyat, String key, boolean defaultValue) {
		preferences = mContext.getSharedPreferences(keyat,
				Context.MODE_PRIVATE);
		boolean value = preferences.getBoolean(key, defaultValue);
		return value;
	}
	/**
	 * 存储String
	 * 
	 * @param mContext
	 * @param key
	 * @param value
	 */
	public static void putValueToSPMap(Context mContext, String key,
                                       String value) {
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putString(key, value);
		edit.commit();
	}
	/**
	 * 存储String
	 * 
	 * @param mContext
	 * @param key
	 * @param value
	 */
	public static void putOtherValueToSPMap(Context mContext, String key,
                                            String value) {
		putValueToSPMap(Keys.OTHER,mContext,key,value);
	}
	public static void putValueToSPMap(String keyat, Context mContext, String key,
                                       String value) {
		preferences = mContext.getSharedPreferences(keyat,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putString(key, value);
		edit.commit();
	}
	public static String getValueFromSPMap(String keyat, Context mContext, String key) {
		if (null != mContext) {
			preferences = mContext.getSharedPreferences(keyat,
					Context.MODE_PRIVATE);
			String value = preferences.getString(key, "");
			return value;
		} else {
			return null;
		}
	}
	public static String getOtherValueFromSPMap(Context mContext, String key) {
		if (null != mContext) {
			preferences = mContext.getSharedPreferences(Keys.OTHER,
					Context.MODE_PRIVATE);
			String value = preferences.getString(key, "");
			return value;
		} else {
			return null;
		}
	}
	
	/**
	 * 清除全部
	 * 
	 * @param mContext
	 */
	public static void clearSPMap(Context mContext) {
		preferences = mContext.getSharedPreferences(Keys.USERINFO,
				Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.clear();
		edit.commit();
	}

	/**
	 * 指定key清除
	 * 
	 * @param mContext
	 * @param key
	 */
	public static void clearSpMap(Context mContext, String key) {
		putValueToSPMap(mContext, key, "");
	}

	/**
	 * 获取String
	 * 
	 * @param mContext
	 * @param key
	 * @return value
	 */
	public static String getValueFromSPMap(Context mContext, String key) {
	    if (null != mContext) {
	        preferences = mContext.getSharedPreferences(Keys.USERINFO,
	                Context.MODE_PRIVATE);
	        String value = preferences.getString(key, "");
	        return value;
	    } else {
	        return null;
	    }
	}

	/**
	 * 获取最近下注记录玩法Id
	 * @param context
	 * @param lotteryType
	 * @param defaultValue 默认玩法Id
	 * @return
	 */
	public static int getLastBetId(Context context, int lotteryType, int defaultValue)
	{
		preferences = context.getSharedPreferences(keyat.OTHER, Context.MODE_PRIVATE);
		int value = preferences.getInt("lastBet_" + lotteryType, defaultValue);

		return value;
	}

	/**
	 * 保存最近下注玩法Id
	 * @param context
	 * @param lotteryType
	 * @param betId
	 */
	public static void putLasetBetId(Context context, int lotteryType, int betId)
	{
		preferences = context.getSharedPreferences(keyat.OTHER, Context.MODE_PRIVATE);
		Editor edit = preferences.edit();
		edit.putInt("lastBet_" + lotteryType, betId);
		edit.commit();
	}
}
