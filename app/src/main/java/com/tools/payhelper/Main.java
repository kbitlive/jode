package com.tools.payhelper;

import java.io.File;

import org.apache.http.Header;
import org.json.JSONObject;

import com.tools.payhelper.Main.StartQQReceived;
import com.tools.payhelper.utils.PayHelperUtils;
import com.tools.payhelper.utils.QQDBManager;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;
import dalvik.system.BaseDexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Main implements IXposedHookLoadPackage {
	public static String QQ_PACKAGE = "com.tencent.mobileqq";
	public static String QQ_WALLET_PACKAGE = "com.qwallet";
	public static boolean QQ_PACKAGE_ISHOOK = false;
	public static boolean QQ_WALLET_ISHOOK = false;
	
	private final String WECHAT_PACKAGE = "com.tencent.mm";
	private final String ALIPAY_PACKAGE = "com.eg.android.AlipayGphone";
	private boolean WECHAT_PACKAGE_ISHOOK = false;
	private boolean ALIPAY_PACKAGE_ISHOOK = false;
	
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
			throws Throwable {
		if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }
		final String packageName = lpparam.packageName;
        final String processName = lpparam.processName;
        if (WECHAT_PACKAGE.equals(packageName)) {
    		try {
                XposedHelpers.findAndHookMethod(ContextWrapper.class, "attachBaseContext", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Context context = (Context) param.args[0];
                        ClassLoader appClassLoader = context.getClassLoader();
                        if(WECHAT_PACKAGE.equals(processName) && !WECHAT_PACKAGE_ISHOOK){
                        	WECHAT_PACKAGE_ISHOOK=true;
                        	//注册广播
                        	StartWechatReceived stratWechat=new StartWechatReceived();
                    		IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction("com.payhelper.wechat.start");
                            context.registerReceiver(stratWechat, intentFilter);
                        	XposedBridge.log("handleLoadPackage: " + packageName);
                        	Toast.makeText(context, "获取到wechat  监控成功", Toast.LENGTH_LONG).show();
                        	new WechatHook().hook(appClassLoader,context);
                        }
                    }
                });
            } catch (Throwable e) {
                XposedBridge.log(e);
            }
        }else if(ALIPAY_PACKAGE.equals(packageName)){   
    		try {
                XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Context context = (Context) param.args[0];
                        ClassLoader appClassLoader = context.getClassLoader();
                        if(ALIPAY_PACKAGE.equals(processName) && !ALIPAY_PACKAGE_ISHOOK){
                        	ALIPAY_PACKAGE_ISHOOK=true;
                        	//注册广播
                        	StartAlipayReceived startAlipay=new StartAlipayReceived();
                    		IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction("com.payhelper.alipay.start");
							intentFilter.addAction("com.tools.payhelper.transfermoney");
                            context.registerReceiver(startAlipay, intentFilter);
                        	XposedBridge.log("handleLoadPackage: " + packageName);
                        	Toast.makeText(context, "获取到alipay=>>监控成功", Toast.LENGTH_LONG).show();
                        	new AliPayHook().hook(appClassLoader,context);
                        }
                    }
                });
    		}catch (Throwable e) {
                XposedBridge.log(e);
            }
        }else if(QQ_PACKAGE.equals(packageName)){
        	try {
       		 XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                   @Override
                   protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                       super.afterHookedMethod(param);
                       Context context = (Context) param.args[0];
                       ClassLoader appClassLoader = context.getClassLoader();
                       if(QQ_PACKAGE.equals(processName) && !QQ_PACKAGE_ISHOOK){
                       	QQ_PACKAGE_ISHOOK=true;
                       	//注册广播
                       	StartQQReceived startQQ=new StartQQReceived();
                   		IntentFilter intentFilter = new IntentFilter();
                           intentFilter.addAction("com.payhelper.qq.start");
                           context.registerReceiver(startQQ, intentFilter);
                       	XposedBridge.log("handleLoadPackage: " + packageName);
//                       	PayHelperUtils.sendmsg(context, "QQHook成功，当前QQ版本:"+PayHelperUtils.getVerName(context));
   						new QQHook().hook(appClassLoader,context);
                       }
                   }
               });
       		 
   		 XposedHelpers.findAndHookConstructor("dalvik.system.BaseDexClassLoader",
                    lpparam.classLoader, String.class, File.class, String.class, ClassLoader.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {

                    if (param.args[0].toString().contains("qwallet_plugin.apk")) {
                        ClassLoader classLoader = (BaseDexClassLoader) param.thisObject;
                        new QQPlugHook().hook(classLoader);
                    }
                }
            });
   		}catch (Exception e) {
               XposedBridge.log(e);
           }
        }
	}
	
	 //自定义接受订单通知广播
    class StartWechatReceived extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	XposedBridge.log("启动微信Activity");
        	try {
				Intent intent2=new Intent(context, XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", context.getClassLoader()));
				intent2.putExtra("mark", intent.getStringExtra("mark"));
				intent2.putExtra("money", intent.getStringExtra("money"));
				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent2);
				XposedBridge.log("启动微信成功");
			} catch (Exception e) {
				XposedBridge.log("启动微信失败："+e.getMessage());
			}
        }
    }
    //自定义接受订单通知广播
    class StartAlipayReceived extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		XposedBridge.log("启动支付宝Activity");
			String action = intent.getAction();
			if ("com.payhelper.alipay.start".equals(action)) {
				Intent intent2 = new Intent(context, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", context.getClassLoader()));
				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent2.putExtra("mark", intent.getStringExtra("mark"));
				intent2.putExtra("money", intent.getStringExtra("money"));
				context.startActivity(intent2);
			}else if ("com.tools.payhelper.transfermoney".equals(action)){//付款
				Intent intent2 = new Intent(context, XposedHelpers.findClass("com.alipay.mobile.nebulacore.ui.H5Activity", context.getClassLoader()));
				intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				XposedBridge.log("打开付款app界面");
				context.startActivity(intent2);
			}
    	}
    }
    
    class StartQQReceived extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		XposedBridge.log("启动QQActivity");
    		try {
    			
    			String money=intent.getStringExtra("money");
    			String mark=intent.getStringExtra("mark");
    			if(!TextUtils.isEmpty(money) && !TextUtils.isEmpty(mark)){
    				QQDBManager qqdbManager=new QQDBManager(context);
        			qqdbManager.addQQMark(intent.getStringExtra("money"),intent.getStringExtra("mark"));
    				long l=System.currentTimeMillis();
        			String url="mqqapi://wallet/open?src_type=web&viewtype=0&version=1&view=7&entry=1&seq=" + l;
        			Intent intent2=new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        			intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			context.startActivity(intent2);
    			}
    			
    		} catch (Exception e) {
    			PayHelperUtils.sendmsg(context, "StartQQReceived异常"+e.getMessage());
			}
    	}
    }
}
