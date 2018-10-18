package com.tools.payhelper.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.tools.payhelper.CustomApplcation;
import com.tools.payhelper.MainActivity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Base64;

public class PayHelperUtils {
	
	public static String WECHATSTART_ACTION = "com.payhelper.wechat.start";
	public static String ALIPAYSTART_ACTION = "com.payhelper.alipay.start";
	public static String QQSTART_ACTION = "com.payhelper.qq.start";
	public static String MSGRECEIVED_ACTION = "com.tools.payhelper.msgreceived";
	public static String LOGINIDRECEIVED_ACTION = "com.tools.payhelper.loginidreceived";
	public static List<QrCodeBean> qrCodeBeans=new ArrayList<QrCodeBean>();
	public static List<OrderBean> orderBeans=new ArrayList<OrderBean>();
	
	public static String getQQLoginId(Context context) {
		String loginId="";
		try {
			SharedPreferences sharedPreferences=context.getSharedPreferences("Last_Login", 0);
			loginId = sharedPreferences.getString("uin", "");
		} catch (Exception e) {
			PayHelperUtils.sendmsg(context, e.getMessage());
		}
		return loginId;
	}
	
	public static void sendLoginId(String loginId, String type, Context context) {
		Intent broadCastIntent = new Intent();
		broadCastIntent.setAction(LOGINIDRECEIVED_ACTION);
		broadCastIntent.putExtra("type", type);
		broadCastIntent.putExtra("loginid", loginId);
		context.sendBroadcast(broadCastIntent);
	}
	
	public static int isActivityTop(Context context) {
		try {
			ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> infos = manager.getRunningTasks(100);
			for (RunningTaskInfo runningTaskInfo : infos) {
				if (runningTaskInfo.topActivity.getClassName()
						.equals("cooperation.qwallet.plugin.QWalletPluginProxyActivity")) {
					return runningTaskInfo.numActivities;
				}
			}
			return 0;
		} catch (SecurityException e) {
			sendmsg(context, e.getMessage());
			return 0;
		}
	}
	
	public static String getVerName(Context context) {
		String verName = "";
		try {
			verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			sendmsg(context, "getVerName异常" + e.getMessage());
		}
		return verName;
	}
	/*
	 * 启动一个app
	 */
	public static void startAPP() {
		try {
			Intent intent = new Intent(CustomApplcation.getInstance().getApplicationContext(), MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			CustomApplcation.getInstance().getApplicationContext().startActivity(intent);
		} catch (Exception e) {
		}
	}

	/**
	 * 将图片转换成Base64编码的字符串
	 * 
	 * @param path
	 * @return base64编码的字符串
	 */
	public static String imageToBase64(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		InputStream is = null;
		byte[] data = null;
		String result = null;
		try {
			is = new FileInputStream(path);
			// 创建一个字符流大小的数组。
			data = new byte[is.available()];
			// 写入数组
			is.read(data);
			// 用默认的编码格式进行编码
			result = Base64.encodeToString(data, Base64.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		result = "\"data:image/gif;base64," + result + "\"";
		return result;
	}
	
	public static void sendAppMsg(String money,String mark,String type,Context context){
		Intent broadCastIntent = new Intent();
		if(type.equals("alipay")){
			broadCastIntent.setAction(ALIPAYSTART_ACTION);
		}else if(type.equals("wechat")){
			broadCastIntent.setAction(WECHATSTART_ACTION);
		}else if(type.equals("qq")){
			broadCastIntent.setAction(QQSTART_ACTION);
		}
        broadCastIntent.putExtra("mark", mark);
        broadCastIntent.putExtra("money", money);
        context.sendBroadcast(broadCastIntent);
	}
	
	 /* 
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt*1000);
        res = simpleDateFormat.format(date);
        return res;
    }
    
    /**  
     * 方法描述：判断某一应用是否正在运行  
     *  
     * @param context     上下文  
     * @param packageName 应用的包名  
     * @return true 表示正在运行，false表示没有运行  
     */  
    public static boolean isAppRunning(Context context, String packageName) {  
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);  
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);  
        if (list.size() <= 0) {  
            return false;  
        }  
        for (ActivityManager.RunningTaskInfo info : list) {  
            if (info.baseActivity.getPackageName().equals(packageName)) {  
                return true;  
            }  
        }  
        return false;  
    }  
    
    /*
	 * 启动一个app
	 */
	public static void startAPP(Context context, String appPackageName) {
		try {
			Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
			context.startActivity(intent);
		} catch (Exception e) {
		}
	}
	public static void notify(final Context context, String type, final String no, String money, String mark, String dt) {
		String notifyurl=AbSharedUtil.getString(context, "notifyurl");
		String signkey=AbSharedUtil.getString(context, "signkey");
		sendmsg(context,"订单"+no+"重试发送异步通知...");
		if(TextUtils.isEmpty(notifyurl) || TextUtils.isEmpty(signkey)){
			sendmsg(context,"发送异步通知异常，异步通知地址为空");
			update(no, "异步通知地址为空");
			return;
		}
		String wxid=AbSharedUtil.getString(context, "wxid");
		HttpUtils httpUtils=new HttpUtils(15000);
		
		String sign=MD5.md5(dt+mark+money+no+type+signkey);
		RequestParams params=new RequestParams();
		params.addBodyParameter("type", type);
		params.addBodyParameter("no", no);
		params.addBodyParameter("money", money);
		params.addBodyParameter("mark", mark);
		params.addBodyParameter("dt", dt);
		if(!TextUtils.isEmpty(wxid)){
			params.addBodyParameter("account", wxid);
		}
		params.addBodyParameter("sign", sign);
		httpUtils.send(HttpMethod.POST, notifyurl, params, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				sendmsg(context,"发送异步通知异常，服务器异常"+arg1);
				update(no, arg1);
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String result=arg0.result;
				if(result.contains("success")){
					sendmsg(context,"发送异步通知成功，服务器返回"+result);
				}else{
					sendmsg(context,"发送异步通知失败，服务器返回"+result);
				}
				update(no, result);
			}
		});
	}
	
	private static void update(String no,String result){
		 DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());
		 dbManager.updateOrder(no,result);
	}
	
	public static void sendmsg(Context context,String msg){
		Intent broadCastIntent = new Intent();
		broadCastIntent.putExtra("msg", msg);
        broadCastIntent.setAction(MSGRECEIVED_ACTION);
        context.sendBroadcast(broadCastIntent);
	}
}
