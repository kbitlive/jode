package com.tools.payhelper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.tools.payhelper.eventbus.AlipayReciveMoney;
import com.tools.payhelper.utils.AbSharedUtil;
import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.MD5;
import com.tools.payhelper.utils.OrderBean;
import com.tools.payhelper.utils.PayHelperUtils;
import com.tools.payhelper.utils.QrCodeBean;
import com.tools.payhelper.utils.URLRequest;
import com.tools.payhelper.view.BillListActivity;
import com.tools.payhelper.view.QrcodeListActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.robv.android.xposed.XposedBridge;

public class MainActivity extends Activity {
	
	public static TextView console;
	private BillReceived billReceived;
	public static String BILLRECEIVED_ACTION = "com.tools.payhelper.billreceived";
	public static String QRCODERECEIVED_ACTION = "com.tools.payhelper.qrcodereceived";
	public static String MSGRECEIVED_ACTION = "com.tools.payhelper.msgreceived";
	public static String TRADENORECEIVED_ACTION = "com.tools.payhelper.tradenoreceived";
	public static String LOGINIDRECEIVED_ACTION = "com.tools.payhelper.loginidreceived";
	public static String NOTIFY_ACTION = "com.tools.payhelper.notify";
	public static int WEBSEERVER_PORT = 8080;
//	private WebServer mVideoServer;
	
	private String currentWechat="";
	private String currentAlipay="";
	private String currentQQ="";
	private DBManager dbManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		setContentView(R.layout.activity_main);
		regist();
		console=(TextView) findViewById(R.id.console);
		initdata();
		setListener();
//		try {
//			mVideoServer=new WebServer(this,WEBSEERVER_PORT);
//            mVideoServer.start();
//            sendmsg("web服务器启动成功，端口:"+WEBSEERVER_PORT);
//		}catch (Exception e) {
//			sendmsg("web服务器启动失败，错误:"+e.getMessage());
//        }
//		this.findViewById(R.id.setting).setOnClickListener(
//				new View.OnClickListener() {
//
//					@Override
//					public void onClick(View arg0) {
//						Intent intent=new Intent(MainActivity.this, SettingActivity.class);
//						startActivity(intent);
//					}
//				});
		//注册广播
		billReceived=new BillReceived();
		IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BILLRECEIVED_ACTION);
        intentFilter.addAction(MSGRECEIVED_ACTION);
        intentFilter.addAction(QRCODERECEIVED_ACTION);
        intentFilter.addAction(LOGINIDRECEIVED_ACTION);
        registerReceiver(billReceived, intentFilter);
        startService(new Intent(this, DaemonService.class));
	}

	private void setListener() {
		this.findViewById(R.id.start_qq).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent broadCastIntent = new Intent();
						broadCastIntent.setAction("com.payhelper.qq.start");
						String time=System.currentTimeMillis()/10000L+"";
						broadCastIntent.putExtra("mark", "test"+time);
						broadCastIntent.putExtra("money", "0.01");
						sendBroadcast(broadCastIntent);
					}
				});
		this.findViewById(R.id.start_alipay).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent broadCastIntent = new Intent();
						broadCastIntent.setAction("com.payhelper.alipay.start");
						String time=System.currentTimeMillis()/10000L+"";
						broadCastIntent.putExtra("mark", "test"+time);
						broadCastIntent.putExtra("money", "0.01");
						sendBroadcast(broadCastIntent);
					}
				});
		this.findViewById(R.id.start_wechat).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent broadCastIntent = new Intent();
						broadCastIntent.setAction("com.payhelper.wechat.start");
						String time=System.currentTimeMillis()/10000L+"";
						broadCastIntent.putExtra("mark", "test"+time);
						broadCastIntent.putExtra("money", "0.01");
						sendBroadcast(broadCastIntent);
					}
				});
		findViewById(R.id.btn_qrcode).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this,QrcodeListActivity.class));
			}
		});
		findViewById(R.id.btn_sendserver).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this,BillListActivity.class));
			}
		});
		findViewById(R.id.btn_chaxun).setOnClickListener(new View.OnClickListener() {//查询
			@Override
			public void onClick(View v) {
				String cookei="zone=RZ33B; ALIPAYJSESSIONID=RZ33OFOsEETEj0fileX40CiLlteEvy63mobilegwRZ13; ssl_upgrade=0; spanner=66Nfs4uYUOGLnjElrpaMXrra34qAe212";
				AliPayHook.tradeOrderQuery(MainActivity.this,cookei);
			}
		});
		findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {//清空数据库数据
			@Override
			public void onClick(View v) {
				dbManager.clearAll();
			}
		});
	}

	private void initdata() {
		dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
		ConFigNet configNet = new ConFigNet();
		String uname = configNet.getuname(this, "uname");
		((TextView)findViewById(R.id.tv_uname)).setText("当前账号 : "+uname);
	}

	private void regist() {
		EventBus.getDefault().register(this);
	}

	public static Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			String txt=msg.getData().getString("log");
			if(console!=null){
				if(console.getText()!=null){
					if(console.getText().toString().length()>100000){
						console.setText("日志定时清理完成..."+"\n\n"+txt);
					}else{
						console.setText(console.getText().toString()+"\n\n"+txt);
					}
					
				}else{
					console.setText(txt);
				}
			}
			super.handleMessage(msg);
		}
		
	};
	@Override
	protected void onDestroy() {
		unregisterReceiver(billReceived);
		EventBus.getDefault().unregister(this);
//		mVideoServer.stop();
		super.onDestroy();
	}
	String sign="";
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void EventReciveMoney(AlipayReciveMoney data){
		System.out.println(data.getJson());
		try {
			JSONObject parm =new JSONObject(data.getJson());
			String mark = parm.getString("mark");
			String type = parm.getString("type");
			String money = parm.getString("money");
			String notifyUrl = parm.getString("notifyUrl");
			 sign = parm.getString("sign");
			if (!TextUtils.isEmpty(notifyUrl)){
				ConFigNet.notifyurl=notifyUrl;
			}
			double m=Double.parseDouble(money);
			if(type.equals("alipay") && !PayHelperUtils.isAppRunning(this, "com.eg.android.AlipayGphone")){
				XposedBridge.log("启动支付宝");
				PayHelperUtils.startAPP(this, "com.eg.android.AlipayGphone");
			}else if(type.equals("wechat") && !PayHelperUtils.isAppRunning(this, "com.tencent.mm")){
				PayHelperUtils.startAPP(this, "com.tencent.mm");
			}else if(type.equals("qq") && !PayHelperUtils.isAppRunning(this, "com.tencent.mobileqq")){
				PayHelperUtils.startAPP(this, "com.tencent.mobileqq");
			}
			String t=System.currentTimeMillis()+"";
			t=t.substring(3);
			//mark=mark+"|"+t;
			List<QrCodeBean> qrCodeBeans=new ArrayList<QrCodeBean>();
			PayHelperUtils.sendAppMsg(money, mark, type, this);
		} catch (JSONException e) {
			e.printStackTrace();
			XposedBridge.log("json解析报错");
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	
	public static void sendmsg(String txt) {
		Message msg = new Message();
		msg.what = 1;
		Bundle data = new Bundle();
		long l = System.currentTimeMillis();
		Date date = new Date(l);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String d = dateFormat.format(date);
		data.putString("log", d + ":" + "  结果:" + txt);
		msg.setData(data);
		try {
			handler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 过滤按键动作
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	 //自定义接受订单通知广播
    class BillReceived extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	try {
	        	if (intent.getAction().contentEquals(BILLRECEIVED_ACTION)) {
	    			 String no = intent.getStringExtra("bill_no");
					 String money = intent.getStringExtra("bill_money");
					 String mark = intent.getStringExtra("bill_mark");
					 String type = intent.getStringExtra("bill_type");
					 DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());
					 String dt=System.currentTimeMillis()+"";
					 sendmsg("打印收到的数据："+mark);
					if (!dbManager.isExistTradeNo(no)) {
						dbManager.addTradeNo(no, "1");
						dbManager.addOrder(new OrderBean(money, mark, type, no, dt, "", 0));
						if (type.equals("alipay")) {
							type = "支付宝";
						} else if (type.equals("wechat")) {
							type = "微信";
						} else if (type.equals("qq")) {
							type = "QQ";
						}
						sendmsg("收到" + type + "订单,订单号：" + no + "金额：" + money + "备注：" + mark);
						MainActivity.this.notify(type, no, money, mark, dt);
					}
	        	 }else if(intent.getAction().contentEquals(QRCODERECEIVED_ACTION)){
					 String money = intent.getStringExtra("money");
					 String mark = intent.getStringExtra("mark");
					 String type = intent.getStringExtra("type");
					 String payurl = intent.getStringExtra("payurl");
					 DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());

						String dt = System.currentTimeMillis() + "";
						dbManager.addQrCode(new QrCodeBean(money, mark, type, payurl, dt));
						sendmsg("生成成功,金额:" + money + "备注:" + mark);
						JSONObject jsonObject=new JSONObject();
					jsonObject.put("msg", "获取成功");
					jsonObject.put("payurl", payurl);
					jsonObject.put("mark", mark);
					jsonObject.put("money", money);
					jsonObject.put("type", type);
					jsonObject.put("account", "");
					URLRequest.getInstance().send202(ConFigNet.socketip,jsonObject.toString(),MainActivity.this,sign);
						//notify(type, payurl, money, mark, dt);

	        	 }else if (intent.getAction().contentEquals(MSGRECEIVED_ACTION)) {
	     			String msg = intent.getStringExtra("msg");
	     			sendmsg(msg);
	     		 }else if (intent.getAction().contentEquals(LOGINIDRECEIVED_ACTION)) {
		     			String loginid = intent.getStringExtra("loginid");
		     			String type = intent.getStringExtra("type");
		     			if(!TextUtils.isEmpty(loginid)){
		     				if(type.equals("wechat") && !loginid.equals(currentWechat)){
		     					sendmsg("当前登录微信账号："+loginid);
		     					currentWechat=loginid;
		     					AbSharedUtil.putString(getApplicationContext(), type, loginid);
		     				}else if(type.equals("alipay") && !loginid.equals(currentAlipay)){
		     					sendmsg("当前登录支付宝账号："+loginid);
		     					currentAlipay=loginid;
		     					AbSharedUtil.putString(getApplicationContext(), type, loginid);
		     				}
		     				else if(type.equals("qq") && !loginid.equals(currentQQ)){
		     					sendmsg("当前登QQ账号："+loginid);
		     					currentQQ=loginid;
		     					AbSharedUtil.putString(getApplicationContext(), type, loginid);
		     				}
		     			}
		     		}
        	} catch (Exception e) {
        		XposedBridge.log(e.getMessage());
			}
        }
    }
	public void notify(String type, final String no, String money, String mark, String dt) {
//			String notifyurl=AbSharedUtil.getString(getApplicationContext(), "notifyurl");
//			String notifyurl="http://www.szdpay.com/Pay/Pay/zpay_return";
		String notifyurl=ConFigNet.notifyurl;
//			String signkey=AbSharedUtil.getString(getApplicationContext(), "signkey");
		String signkey=ConFigNet.signkey;
		if(TextUtils.isEmpty(notifyurl) || TextUtils.isEmpty(signkey)){
			sendmsg("发送异步通知异常，异步通知地址为空");
			update(no, "异步通知地址为空");
			return;
		}
		String wxid=AbSharedUtil.getString(getApplicationContext(), "account");
		if(type.equals("qq")){
			wxid=AbSharedUtil.getString(getApplicationContext(), "qq");
		}
		HttpUtils httpUtils=new HttpUtils(15000);

		String sign=MD5.md5("dt="+dt+"&mark="+mark+"&money="+money+"&no="+no+"&type="+type+signkey);
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
		XposedBridge.log("开始通知服务器："+params.toString());
		httpUtils.send(HttpMethod.POST, notifyurl, params, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				sendmsg("发送异步通知异常，服务器异常"+arg1);
				update(no, arg1);
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String result=arg0.result;
				if(result.contains("success")){
					sendmsg("发送异步通知成功，服务器返回"+result);
					XposedBridge.log("发送服务器成功");
				}else{
					sendmsg("发送异步通知失败，服务器返回"+result);
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
