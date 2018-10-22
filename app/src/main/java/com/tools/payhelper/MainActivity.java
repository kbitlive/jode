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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.idescout.sql.SqlScoutServer;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.tools.payhelper.eventbus.AlipayReciveMoney;
import com.tools.payhelper.eventbus.Logging;
import com.tools.payhelper.eventbus.NetOffLine;
import com.tools.payhelper.utils.AbSharedUtil;
import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.MD5;
import com.tools.payhelper.utils.OrderBean;
import com.tools.payhelper.utils.PayHelperUtils;
import com.tools.payhelper.utils.PreferencesUtils;
import com.tools.payhelper.utils.QrCodeBean;
import com.tools.payhelper.utils.URLRequest;
import com.tools.payhelper.view.BillListActivity;
import com.tools.payhelper.view.DialogActivity;
import com.tools.payhelper.view.QrcodeListActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
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
	public final  static String BILLRECEIVED_COOKIE="com.tools.payhelper.cookie";
	public final  static String transfermoney="com.tools.payhelper.transfermoney";

	public static int WEBSEERVER_PORT = 8080;
//	private WebServer mVideoServer;
	
	private String currentWechat="";
	private String currentAlipay="";
	private String currentQQ="";
	public DBManager dbManager;
	private RadioGroup rd_group;
	private TextView server_status;
	private TextView tv_alipay_status;
	private TextView tv_wechatpay_status;
	private String opentype;
	private RadioButton rb_off;
	private RadioButton rb_ali;
	private RadioButton rb_wechat;
	double chargemoney;
	private TextView tv_account;
	private String ps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		setContentView(R.layout.activity_main);
		regist();
		console=(TextView) findViewById(R.id.console);
		initdata();
		findViewById();
		setListener();
		initUI();
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
        intentFilter.addAction(BILLRECEIVED_COOKIE);
        registerReceiver(billReceived, intentFilter);
        startService(new Intent(this, DaemonService.class));
	}

	private void initUI() {
		opentype = PreferencesUtils.getValueFromSPMap(this, PreferencesUtils.Keys.TYPE);
		if (TextUtils.isEmpty(opentype)){//关闭所有
			setNotOnLine(tv_wechatpay_status);
			setNotOnLine(tv_alipay_status);
			rb_off.setChecked(true);
		}else if ("alipay".equals(opentype)){//支付宝
			setOnLine(server_status);
			setNotOnLine(tv_wechatpay_status);
			rb_ali.setChecked(true);
		}else{//微信
			setOnLine(tv_wechatpay_status);
			setNotOnLine(tv_alipay_status);
			rb_wechat.setChecked(true);
		}
	}

	private void findViewById() {
		tv_account = findViewById(R.id.tv_account);
		rb_ali = findViewById(R.id.rb_ali);
		rb_off = findViewById(R.id.rb_off);
		rb_wechat = findViewById(R.id.rb_wechat);
		tv_alipay_status = findViewById(R.id.tv_alipay_status);
		server_status = findViewById(R.id.server_status);
		tv_wechatpay_status = findViewById(R.id.tv_wechatpay_status);
		rd_group = findViewById(R.id.rd_group);
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
//				startActivity(new Intent(MainActivity.this,BillListActivity.class));
				PayHelperUtils.startAPP(MainActivity.this, "com.eg.android.AlipayGphone");
				Intent intel=new Intent();
				intel.setAction(transfermoney);
				sendBroadcast(intel);
			}
		});
		findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {//查询
			@Override
			public void onClick(View v) {
//				String cookei="zone=RZ33B; ALIPAYJSESSIONID=RZ33OFOsEETEj0fileX40CiLlteEvy63mobilegwRZ13; ssl_upgrade=0; spanner=66Nfs4uYUOGLnjElrpaMXrra34qAe212";
//				tradeOrderQuery(MainActivity.this,cookei);
				System.exit(0);
//				Intent intel=new Intent(MainActivity.this,DialogActivity.class);
//				intel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(intel );


			}
		});
		findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {//清空数据库数据
			@Override
			public void onClick(View v) {
				chargemoney=0;
				tv_account.setText("金额:0.00");
//				dbManager.clearAll();
//                ArrayList<QrCodeBean> qrCodeBeans = dbManager.FindQrcodeAll();
//                XposedBridge.log(qrCodeBeans.toString());
//                SingtonData.getInstance().setName("helloword");
            }
		});
		rd_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				View viewById = rd_group.findViewById(checkedId);
				if (!viewById.isPressed()){
					return;
				}
				String type="";
				if (checkedId==R.id.rb_ali){//支付宝
					type="alipay";
					System.out.println("打开支付宝");
					setOnLine(tv_alipay_status);
					setNotOnLine(tv_wechatpay_status);
				}else if (checkedId==R.id.rb_wechat){//微信
					System.out.println("打开微信");
					type="weixin";
					setOnLine(tv_wechatpay_status);
					setNotOnLine(tv_alipay_status);
				}else{//关闭通道
					System.out.println("关闭总开关");
					type="";
					setNotOnLine(tv_alipay_status);
					setNotOnLine(tv_wechatpay_status);
				}
				PreferencesUtils.putValueToSPMap(MainActivity.this,PreferencesUtils.Keys.TYPE,type);
				URLRequest.getInstance().send210(MainActivity.this,type);
			}
		});
	}

	private void initdata() {
		dbManager = new DBManager(CustomApplcation.getInstance().getApplicationContext());
		ConFigNet configNet = new ConFigNet();
		String uname = configNet.getuname(this, "uname");
		((TextView)findViewById(R.id.tv_uname)).setText("当前账号 : "+uname);
		int payorder = dbManager.getcount("payorder");
		int qrcode = dbManager.getcount("qrcode");
		int tradeno = dbManager.getcount("tradeno");
		if (payorder>1000) dbManager.delete300("payorder");
		if (qrcode>1000)dbManager.delete300("qrcode");
		if (tradeno>1000)dbManager.delete300("tradeno");
	}

	private void regist() {
		EventBus.getDefault().register(this);
		SqlScoutServer.create(this, getPackageName());
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
			ps = parm.getString("ps");
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
			PayHelperUtils.sendAppMsg(money, mark, type,this);
		} catch (JSONException e) {
			e.printStackTrace();
			XposedBridge.log("json解析报错");
		}

	}
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void LogingBack(Logging logging){
		PreferencesUtils.putBooleanToSPMap(MainActivity.this, PreferencesUtils.Keys.IS_LOGIN, true);
		setOnLine(server_status);
		initUI();

	}
	@Subscribe(threadMode = ThreadMode.MAIN)
	public void NotOnLine(NetOffLine data){
		setNotOnLine(server_status);
	}

	public void setOnLine(TextView tv){
		tv.setText("在线");
		tv.setTextColor(getColor(R.color.main_blue));
	}
	public void setNotOnLine(TextView tv){
		tv.setText("离线");
		tv.setTextColor(getColor(R.color.gray_text));
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
						dbManager.addOrder(new OrderBean(money, mark, type, no, dt, "start", 0));
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
					jsonObject.put("ps",ps);
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
		     		}else if (intent.getAction().contentEquals(BILLRECEIVED_COOKIE)){//收到支付宝 传过来的cookie
					String cookie = intent.getStringExtra("cookie");
					XposedBridge.log("通过广播收到的cookid"+cookie);
					tradeOrderQuery(MainActivity.this,cookie);

				}
        	} catch (Exception e) {
        		XposedBridge.log(e.getMessage());
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
			sendmsg("发送异步通知异常，异步通知地址为空"+notifyurl);
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
		chargemoney+=Double.valueOf(money);
		tv_account.setText("金额:"+chargemoney);
		XposedBridge.log("开始通知服务器："+"mark="+mark+"&money="+money);
		httpUtils.send(HttpMethod.POST, notifyurl, params, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				sendmsg("发送异步通知异常，服务器异常"+arg1);
				update(no, "errow:"+arg1);
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				String result=arg0.result;
				if(result.contains("success")){
					sendmsg("发送异步通知成功，服务器返回"+result);
					XposedBridge.log("发送服务器成功");
				}else{
					sendmsg("发送异步通知失败，服务器返回"+result);
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
	public void tradeOrderQuery(final Context context, final String cookie) {
		XposedBridge.log("cookie:" + cookie);
		long currentTimeMillis = System.currentTimeMillis();
		long j = currentTimeMillis - 864000000;
		String a = AliPayHook.formatDate();
		String str2 = "https://mbillexprod.alipay.com/enterprise/simpleTradeOrderQuery.json?beginTime=" + j + "&limitTime=" + currentTimeMillis + "&pageSize=10&pageNum=1&channelType=ALL";

		HttpUtils httpUtils = new HttpUtils(15000);
		httpUtils.configResponseTextCharset("GBK");
		RequestParams requestParams = new RequestParams();
		requestParams.addHeader("Cookie", cookie);
		requestParams.addHeader("Referer", "https://render.alipay.com/p/z/merchant-mgnt/simple-order.html?beginTime=" + a + "&endTime=" + a + "&fromBill=true&channelType=ALL");
		XposedBridge.log("-----------------------------开始请求订单信息-----------------------");
		final long time = System.currentTimeMillis();
		httpUtils.send(HttpRequest.HttpMethod.GET, str2, requestParams, new RequestCallBack<String>() {


			public void onSuccess(ResponseInfo<String> responseInfo) {
				String str = (String) responseInfo.result;
				XposedBridge.log("成功响应时间"+(System.currentTimeMillis()-time));
				XposedBridge.log("历史订单:" + str);
				try {
					JSONArray jSONArray = new JSONObject(str).getJSONObject("result").getJSONArray("list");
					if (jSONArray != null && jSONArray.length() > 0) {
						XposedBridge.log("最近历史订单数量:" + jSONArray.length());
//                        JSONObject object =jSONArray.optJSONObject(0);
//                        String tradeNo =object.optString("tradeNo");
//                        getDetial(context,tradeNo,cookie);
						long lastTime = 0;
						for (int i = 0; i < jSONArray.length(); i++) {
							JSONObject object = jSONArray.optJSONObject(i);
							long gmtCreateStamp = object.optLong("gmtCreateStamp");
							if (i==0)lastTime=gmtCreateStamp;
							if (lastTime-gmtCreateStamp>3*24*60*60*1000)break;//和第一单相差3天的订单不查询
							String tradeNo =object.optString("tradeNo");
							if (!dbManager.isExistTradeNo(tradeNo)) {
								XposedBridge.log("请求详细信息" + i+"tradeNo:"+tradeNo);
								AliPayHook.getDetial(context, tradeNo, cookie);
							}
						}
					}
				} catch (Exception e) {
					XposedBridge.log(  "获取tradeNo异常" + e.getMessage());
				}
			}

			@Override
			public void onFailure(com.lidroid.xutils.exception.HttpException e, String s) {
				XposedBridge.log("错误响应时间"+(System.currentTimeMillis()-time));
				XposedBridge.log("查询订单错误 原因："+s);

			}
		});
	}

}
