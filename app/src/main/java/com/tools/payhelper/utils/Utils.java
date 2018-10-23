package com.tools.payhelper.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @Author SunnyCoffee
 * @Date 2014-1-28
 * @version 1.0
 * @Desc 工具类
 */

public class Utils {

	public static String getCurrentTime(String format) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		String currentTime = sdf.format(date);
		return currentTime;
	}
	public  static  String getsjctotime(Long time,String formate){
		SimpleDateFormat sdf = new SimpleDateFormat(formate);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+08"));
		String date = sdf.format(new Date(time));
		return date;
	}
	public static String getCurrentTime() {
		return getCurrentTime("yyyy-MM-dd  HH:mm:ss");
	}
}
