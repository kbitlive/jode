package com.tools.payhelper;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 *
 * @author suxia_3vuneo4
 *
 */

public class AliPayHook {

    public static String BILLRECEIVED_ACTION = "com.tools.payhelper.billreceived";
    public static String QRCODERECEIVED_ACTION = "com.tools.payhelper.qrcodereceived";

    public void hook(final ClassLoader classLoader, final Context context) {
        securityCheckHook(classLoader);
        try {
            Class<?> insertTradeMessageInfo = XposedHelpers.findClass("com.alipay.android.phone.messageboxstatic.biz.dao.TradeDao", classLoader);
            XposedBridge.hookAllMethods(insertTradeMessageInfo, "insertMessageInfo", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        XposedBridge.log("======start=========");
                        Object object = param.args[0];
                        String MessageInfo = (String) XposedHelpers.callMethod(object, "toString");
                        XposedBridge.log(MessageInfo);
                        String content= StringUtils.getTextCenter(MessageInfo, "content='", "'");
                        if(content.contains("二维码收款") || content.contains("收到一笔转账")){
                            JSONObject jsonObject=new JSONObject(content);
                            String money=jsonObject.getString("content");
                            String mark=jsonObject.getString("assistMsg2");
                            String tradeNo=StringUtils.getTextCenter(MessageInfo,"tradeNO=","&");
                            XposedBridge.log("收到支付宝支付订单："+tradeNo+"=="+money+"=="+mark);

                            Intent broadCastIntent = new Intent();
                            broadCastIntent.putExtra("bill_no", tradeNo);
                            broadCastIntent.putExtra("bill_money", money);
                            broadCastIntent.putExtra("bill_mark", mark);
                            broadCastIntent.putExtra("bill_type", "alipay");
                            broadCastIntent.setAction(BILLRECEIVED_ACTION);
                            context.sendBroadcast(broadCastIntent);
                        }
                        XposedBridge.log("======end=========");
                    } catch (Exception e) {
                        XposedBridge.log(e.getMessage());
                    }
                    super.beforeHookedMethod(param);
                }
            });
            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.alipay.android.phone.messageboxstatic.biz.dao.ServiceDao", classLoader), "insertMessageInfo", new XC_MethodHook() {

                protected void beforeHookedMethod(MethodHookParam methodHookParam) {
                    try {
                        XposedBridge.log("======商户服务start=========");
                        String replace = getTextCenter((String) XposedHelpers.callMethod(methodHookParam.args[0], "toString", new Object[0]), "extraInfo='", "'").replace("\\", "");
                        XposedBridge.log("商户服务"+replace);
                        if (replace.contains("收钱到账") || replace.contains("收款到账")) {
                            tradeOrderQuery(context, getCookie(classLoader));
                        }
                        XposedBridge.log("======商户服务end=========");
                    } catch (Exception e) {
                        XposedBridge.log("商家服务回调异常:" + e.getMessage());
                        XposedBridge.log(e.getMessage());
                    }

                }
            });

            XposedHelpers.findAndHookMethod("com.alipay.android.phone.messageboxstatic.biz.dao.ServiceDao", classLoader, "queryMsginfoByOffset",  String.class,long.class, long.class,new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String time = param.args[1] + "";
                    if (time.equals("0")) {
                        List list = (List) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mbxsgsg.d.b", classLoader), "a", param.getResult());
                        for (Object o : list) {

                            String content = (String) XposedHelpers.getObjectField(o, "content");
                            String msgState = (String) XposedHelpers.getObjectField(o, "msgState");
                            String msgId = (String) XposedHelpers.getObjectField(o, "msgId");
                            String s = new Gson().toJson(o);
                            XposedBridge.log(s.contains("收款到账") + " 消息 " + s);
                            // if (content!=null&&content.contains("收款到账")&&msgState.equals("INIT")&&!msgList.contains(msgId)){
                            if (s.contains("收款到账")) {
                                String extraInfo = (String) XposedHelpers.getObjectField(o, "extraInfo");
                                XposedBridge.log(extraInfo);
                                JSONObject object = new JSONObject(extraInfo);
                                object.put("action", "transferMessage");
                                tradeOrderQuery(context,getCookie(classLoader));
                            }
                        }
                    }
                }
            });
            // hook设置金额和备注的onCreate方法，自动填写数据并点击
            XposedHelpers.findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("Hook支付宝开始......");
                    Field jinErField = XposedHelpers.findField(param.thisObject.getClass(), "b");
                    final Object jinErView = jinErField.get(param.thisObject);
                    Field beiZhuField = XposedHelpers.findField(param.thisObject.getClass(), "c");
                    final Object beiZhuView = beiZhuField.get(param.thisObject);
                    Intent intent = ((Activity) param.thisObject).getIntent();
                    String mark=intent.getStringExtra("mark");
                    String money=intent.getStringExtra("money");
                    //设置支付宝金额和备注
                    XposedHelpers.callMethod(jinErView, "setText", money);
                    XposedHelpers.callMethod(beiZhuView, "setText", mark);
                    //点击确认
                    Field quRenField = XposedHelpers.findField(param.thisObject.getClass(), "e");
                    final Button quRenButton = (Button) quRenField.get(param.thisObject);
                    quRenButton.performClick();
                    XposedBridge.log("打印初始值:"+CustomApplcation.getInstance().getData());
                }
            });

            // hook获得二维码url的回调方法
            XposedHelpers.findAndHookMethod("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", classLoader, "a",
                    XposedHelpers.findClass("com.alipay.transferprod.rpc.result.ConsultSetAmountRes", classLoader), new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                            Field moneyField = XposedHelpers.findField(param.thisObject.getClass(), "g");
                            String money = (String) moneyField.get(param.thisObject);

                            Field markField = XposedHelpers.findField(param.thisObject.getClass(), "c");
                            Object markObject = markField.get(param.thisObject);
                            String mark=(String) XposedHelpers.callMethod(markObject, "getUbbStr");

                            Object consultSetAmountRes = param.args[0];
                            Field consultField = XposedHelpers.findField(consultSetAmountRes.getClass(), "qrCodeUrl");
                            String payurl = (String) consultField.get(consultSetAmountRes);

                            XposedBridge.log(money+"  "+mark+"  "+payurl);
                            XposedBridge.log("调用增加数据方法==>支付宝");
                            Intent broadCastIntent = new Intent();
                            broadCastIntent.putExtra("money", money);
                            broadCastIntent.putExtra("mark", mark);
                            broadCastIntent.putExtra("type", "alipay");
                            broadCastIntent.putExtra("payurl", payurl);
                            broadCastIntent.setAction(QRCODERECEIVED_ACTION);
                            context.sendBroadcast(broadCastIntent);
                        }
                    });

        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }

    private void securityCheckHook(ClassLoader classLoader) {
        try {
            Class<?> securityCheckClazz = XposedHelpers.findClass("com.alipay.mobile.base.security.CI", classLoader);
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", String.class, String.class, String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object object = param.getResult();
                    XposedHelpers.setBooleanField(object, "a", false);
                    param.setResult(object);
                    super.afterHookedMethod(param);
                }
            });

            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", Class.class, String.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return (byte) 1;
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", ClassLoader.class, String.class, new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return (byte) 1;
                }
            });
            XposedHelpers.findAndHookMethod(securityCheckClazz, "a", new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    return false;
                }
            });

        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }
    public static void tradeOrderQuery(final Context context, final String cookie) {
        XposedBridge.log("cookie:" + cookie);
        long currentTimeMillis = System.currentTimeMillis();
        long j = currentTimeMillis - 864000000;
        String a = formatDate();
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
                    DBManager dbManager=new DBManager(context);
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
                            if (lastTime-gmtCreateStamp>2000)break;//3秒前的订单不查询
                                String tradeNo =object.optString("tradeNo");
                                    XposedBridge.log("请求详细信息"+i);
                                    getDetial(context,tradeNo,cookie);
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
    public static String getCookie(ClassLoader classLoader) {
        String str = "";
        XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mobile.common.transportext.biz.appevent.AmnetUserInfo", classLoader), "getSessionid", new Object[0]);
        Context context = (Context) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mobile.common.transportext.biz.shared.ExtTransportEnv", classLoader), "getAppContext", new Object[0]);
        if (context == null) {
            XposedBridge.log(" context为空");
        } else if (XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mobile.common.helper.ReadSettingServerUrl", classLoader), "getInstance", new Object[0]) != null) {
            return (String) XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.alipay.mobile.common.transport.http.GwCookieCacheHelper", classLoader), "getCookie", new Object[]{".alipay.com"});
        } else {
            XposedBridge.log(" readSettingServerUrl为空");
            XposedBridge.log("readSettingServerUrl为空");
        }
        return str;
    }
    public static String formatDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
    }


    public static void getDetial(final Context context, String tradeNo, String cookie) {
        String str2 = "https://tradeeportlet.alipay.com/wireless/tradeDetail.htm?tradeNo=" +tradeNo;
        long currentTimeMillis = System.currentTimeMillis();
        long j = currentTimeMillis - 864000000;
        String Referer = "https://mbillexprod.alipay.com/enterprise/simpleTradeOrderQuery.json?beginTime=" + j + "&limitTime=" + currentTimeMillis + "&pageSize=20&pageNum=1&channelType=ALL";
        HttpUtils httpUtils = new HttpUtils(15000);
        httpUtils.configResponseTextCharset("GBK");
        RequestParams requestParams = new RequestParams();
        requestParams.addHeader("Cookie", cookie);
        requestParams.addHeader("Referer", Referer);
        httpUtils.send(HttpRequest.HttpMethod.GET, str2, requestParams, new RequestCallBack<String>() {


            public void onSuccess(ResponseInfo<String> responseInfo) {
                String str = (String) responseInfo.result;
//                 XposedBridge.log("详细信息:" + str);

              /*  try {
                     writeResultStringtoFile(str);
                } catch (IOException e) {

                }*/
                Document document = Jsoup.parse(str);
                String  name = document.getElementsByClass("b-name").first().html();
                String amount =document.getElementsByClass("amount").first().html();
                XposedBridge.log("name ="+name+" amount ="+ amount);
                Elements element =document.getElementsByClass("trade-info-value");
                for (int i=0;i<element.size();i++){
                    //  XposedBridge.log(" "+element.get(i).html());
                }
                String state =element.get(0).html();
                String time =element.get(1).html();
                String money =element.get(2).html();
                String remark =element.get(3).html();
                String trandno =element.get(4).html();
                XposedBridge.log(" time ="+time+" money="+money+" remark="+remark+" trandno ="+trandno);
                Intent localIntent = new Intent();
                localIntent.putExtra("bill_no", trandno);
                localIntent.putExtra("bill_money", money);
                localIntent.putExtra("bill_time", time);
                localIntent.putExtra("bill_type", "alipay");
                localIntent.putExtra("bill_mark", remark);
                localIntent.setAction(AliPayHook.BILLRECEIVED_ACTION);
                context.sendBroadcast(localIntent);
            }

            @Override
            public void onFailure(com.lidroid.xutils.exception.HttpException e, String s) {
                XposedBridge.log("详细信息 错误:" +  s);
            }
        });
    }
    public static void writeResultStringtoFile(String str) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory() + "/tradeResult.txt");

        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        FileOutputStream ops =new FileOutputStream(file,false);
        ops.write(str.getBytes());
        ops.flush();
        ops.close();


    }
    public static String getTextCenter(String str, String str2, String str3) {
        try {
            int indexOf = str.indexOf(str2) + str2.length();
            return str.substring(indexOf, str.indexOf(str3, indexOf));
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}