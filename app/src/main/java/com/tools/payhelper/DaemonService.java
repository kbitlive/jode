package com.tools.payhelper;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.tools.payhelper.utils.AbSharedUtil;
import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.MD5;
import com.tools.payhelper.utils.OrderBean;
import com.tools.payhelper.utils.PayHelperUtils;
import com.tools.payhelper.utils.RSAUtil;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;

public class DaemonService extends Service {  
	public static String NOTIFY_ACTION = "com.tools.payhelper.notify";
    private static final String TAG = "DaemonService";  
    public static final int NOTICE_ID = 100;
    private QueryThread query;
    private DBManager db;
    @Nullable  
    @Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }  
  
  
    @Override  
    public void onCreate() {  
        super.onCreate();  
        //如果API大于18，需要弹出一个可见通知  
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){  
            Notification.Builder builder = new Notification.Builder(this);  
            builder.setSmallIcon(R.drawable.ic_launcher);  
            builder.setContentTitle("收款助手");  
            builder.setContentText("收款助手正在运行中...");  
            builder.setAutoCancel(false);
            builder.setOngoing(true);
            startForeground(NOTICE_ID,builder.build());  
        }else{  
            startForeground(NOTICE_ID,new Notification());  
        } 
        PayHelperUtils.sendmsg(getApplicationContext(), "启动定时任务");
        AlarmReceiver alarmReceiver=new AlarmReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NOTIFY_ACTION);
        registerReceiver(alarmReceiver, intentFilter);
        
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time=AbSharedUtil.getInt(getApplicationContext(), "time");
        int triggerTime = 3 * 60 * 1000; 
        if(time!=0){
        	triggerTime = time * 1000;
        }
        Intent i = new Intent(NOTIFY_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        manager.setRepeating(AlarmManager.RTC_WAKEUP , System.currentTimeMillis(), triggerTime, pi);
        if (null!=query){
            query.stop();
            query=null;
        }
        query = new QueryThread();
        query.start();
        db = new DBManager(getApplicationContext());
    }  
  
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        // 如果Service被终止  
        // 当资源允许情况下，重启service  
        return START_STICKY;  
    }  
  
  
    @Override  
    public void onDestroy() {  
        super.onDestroy();  
        // 如果Service被杀死，干掉通知  
        XposedBridge.log("任务被杀死");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){  
            NotificationManager mManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);  
            mManager.cancel(NOTICE_ID);  
        }
        query.stop();
        // 重启自己  
        Intent intent = new Intent(getApplicationContext(),DaemonService.class);  
        startService(intent);

    }
    class QueryThread extends Thread{
        @Override
        public void run() {
            super.run();
            while (true){
                try {
                    Thread.sleep(60*1000);//1分钟查询一次失败订单
                    ArrayList<OrderBean> orderBeans = db.FindFailOrders();
                    XposedBridge.log("查询失败订单："+orderBeans.toString());
                    for (int i = 0; i < orderBeans.size(); i++) {
                        OrderBean orderBean = orderBeans.get(i);
                        String type = orderBean.getType();
                        if (type.equals("alipay")) {
                            type = "支付宝";
                        } else if (type.equals("wechat")) {
                            type = "微信";
                        } else if (type.equals("qq")) {
                            type = "QQ";
                        }
                        notify(type,orderBean.getNo(),orderBean.getMoney(),orderBean.getMark(),orderBean.getDt());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }


        }

        public void notify(String type, final String no, String money, final String mark, String dt) {
//			String notifyurl=AbSharedUtil.getString(getApplicationContext(), "notifyurl");
//			String notifyurl="http://www.szdpay.com/Pay/Pay/zpay_return";
            String notifyurl=ConFigNet.notifyurl;
//			String signkey=AbSharedUtil.getString(getApplicationContext(), "signkey");
            String signkey=ConFigNet.signkey;
            if(TextUtils.isEmpty(notifyurl) || TextUtils.isEmpty(signkey)){
                update(no, "异步通知地址为空");
                return;
            }
            String wxid=AbSharedUtil.getString(getApplicationContext(), "account");
            if(type.equals("qq")){
                wxid=AbSharedUtil.getString(getApplicationContext(), "qq");
            }
            HttpUtils httpUtils=new HttpUtils(15000);

            String sign=MD5.md5("dt="+dt+"&mark="+mark+"&money="+money+"&no="+no+"&type="+type+signkey);
            JSONObject jsonParams=new JSONObject();
            RequestParams params=new RequestParams();
            try {
                jsonParams.put("type",type);
                jsonParams.put("no",no);
                jsonParams.put("money",money);
                jsonParams.put("mark",mark);
                jsonParams.put("dt",dt);
                if(!TextUtils.isEmpty(wxid)){
                    jsonParams.put("account", wxid);
                }
                jsonParams.put("sign",sign);
                String encrypt = RSAUtil.encryptLong(RSAUtil.DEFAULT_PRIVATE_KEY, jsonParams.toString());
                params.addBodyParameter("data", encrypt);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            XposedBridge.log("开始通知服务器："+"mark="+mark+"&money="+money);
            httpUtils.send(HttpRequest.HttpMethod.POST, notifyurl, params, new RequestCallBack<String>() {

                @Override
                public void onFailure(HttpException arg0, String arg1) {
                    update(no, "errow:"+arg1);
                }

                @Override
                public void onSuccess(ResponseInfo<String> arg0) {
                    String result=arg0.result;
                    if(result.contains("success")){
                        XposedBridge.log("发送服务器成功");
                    }else{
                        XposedBridge.log("发送服务器失败"+result+"备注："+mark);
                    }
                    update(no, result);
                }
            });
        }
        private void update(String no,String result){
            DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());
            dbManager.updateOrder(no,result);
        }
    }
}  