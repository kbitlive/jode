package com.tools.payhelper.utils;

import android.content.Context;
import android.text.TextUtils;

import com.tools.payhelper.BuildConfig;
import com.tools.payhelper.ConFigNet;
import com.tools.payhelper.mina.BaseNetTool;
import com.tools.payhelper.mina.MinaClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class URLRequest {
    public static ExecutorService getCachedThreadPool() {
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newFixedThreadPool(20); //new 出一个新的线程池
//            cachedThreadPool = Executors.newCachedThreadPool(); //new 出一个新的线程池
        }
        return cachedThreadPool;
    }

    private static URLRequest instance;
    public static ExecutorService cachedThreadPool;

    public static synchronized URLRequest getInstance() {//单例设计模式
        if (instance == null) {
            instance = new URLRequest();
        }
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newFixedThreadPool(20); //new 出一个新的线程池
//            cachedThreadPool = Executors.newCachedThreadPool(); //new 出一个新的线程池
        }
        return instance;
    }

    public void  send101(final String sockip, final Context context){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                byte[]data=new byte[16];
                int position=0;
                BaseNetTool.writeInt(101,data,position);
                position+=4;
                BaseNetTool.writeInt(1,data,position);
                position+=4;
                BaseNetTool.writeLong(System.currentTimeMillis(),data,position);
                position+=8;
                try {
                    byte[] bytes = BaseNetTool.appendHead2(data);
                    MinaClient.getinstance().sendMessage(sockip,bytes,context);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });
    }
    public void send103(final Context context, final String data){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                TcpParam param=new TcpParam(103);
                param.write(data);
                try {
                    byte[] bytes = BaseNetTool.appendHead2(param.getParam2());
                    MinaClient.getinstance().sendMessage(ConFigNet.socketip,bytes,context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public void send100(final String sockip, final Context context, final String uname, final String password){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                int length = uname.getBytes().length;
                String devicesID = ConFigNet.getDevicesID(context);
                int device_len =  devicesID.getBytes().length;
                int password_len =  password.getBytes().length;
                byte[] data=new byte[24+length+device_len+password_len];
                int position=0;
                BaseNetTool.writeInt(100,data,position);
                position+=4;
                BaseNetTool.writeInt(9231238,data,position);
                position+=4;
                BaseNetTool.writeInt(1,data,position);//支付宝 写死1
                position+=4;
                BaseNetTool.writeInt( length,data,position);
                position+=4;
                BaseNetTool.writeUTF8_2(uname,data,position);
                position+=length;
                BaseNetTool.writeInt(device_len,data,position);
                position+=4;
                BaseNetTool.writeUTF8_2(devicesID,data,position);
                position+=device_len;
                BaseNetTool.writeInt( password_len,data,position);
                position+=4;
                BaseNetTool.writeUTF8_2(password,data,position);
                position+=password_len;
//                SingletonData.getinstance().setUname(uname);
                new ConFigNet().savedData(context,uname,password);
                try {
                    byte[] bytes = BaseNetTool.appendHead2(data);
                    MinaClient.getinstance().sendMessage(sockip,bytes,context);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
//    private int getPlatForm(){
//        if (BuildConfig.FLAVOR.equals(ConfigNet.alipay))return 1;
//        else if (BuildConfig.FLAVOR.equals(ConfigNet.charge_wechat))return 2;
//        else if (BuildConfig.FLAVOR.equals(ConfigNet.charge_cloudpay))return 3;
//        else{
//            return 0;
//        }
//
//    }
    public  void send102(final String sockip, final String wechat_url, final Context context, final String sign){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                short length = (short) wechat_url.getBytes().length;
                if (TextUtils.isEmpty(sign))return;
                int slen = sign.getBytes().length;
                byte[] data=new byte[10+length+slen];
                int position=0;
                BaseNetTool.writeInt(102,data,position);
                position+=4;
                BaseNetTool.writeShort(length,data,position);
                position+=2;
                BaseNetTool.writeUTF8_2(wechat_url,data,position);
                position+=length;
                BaseNetTool.writeInt(slen,data,position);
                position+=4;
                BaseNetTool.writeUTF8_2(sign,data,position);
                position+=slen;
                System.out.println("打印url："+wechat_url+"sign："+sign);
                try {
                    byte[] bytes = BaseNetTool.appendHead2(data);
                    MinaClient.getinstance().sendMessage(sockip,bytes,context);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


    }
    public  void send202(final String sockip, final String wechat_url, final Context context, final String sign){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                int length =  wechat_url.getBytes().length;
                int slen = sign.getBytes().length;
                byte[] data=new byte[12+length+slen];
                int position=0;
                BaseNetTool.writeInt(202,data,position);
                position+=4;
                BaseNetTool.writeInt(length,data,position);
                position+=4;
                BaseNetTool.writeUTF8_2(wechat_url,data,position);
                position+=length;
                BaseNetTool.writeInt(slen,data,position);
                position+=4;
                BaseNetTool.writeUTF8_2(sign,data,position);
                position+=slen;
                System.out.println("打印url："+wechat_url+"sign:"+sign);
                try {
                    byte[] bytes = BaseNetTool.appendHead2(data);
                    MinaClient.getinstance().sendMessage(sockip,bytes,context);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void send210(final Context context, final String paytype){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                TcpParam tcp=new TcpParam(210);
                try {
                    JSONObject json=new JSONObject();
                    json.put("payType",paytype);
                    tcp.write(json.toString());
                    try {
                        byte[] bytes = BaseNetTool.appendHead2(tcp.getParam2());
                        MinaClient.getinstance().sendMessage(ConFigNet.socketip,bytes,context);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });


    }

    /**
     * 支付宝收到金额 并发送服务端
     * @param sockip
     * @param context
     * @param money
     */
    public void send401(final String sockip, final Context context, final String money) {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                TcpParam tcp = new TcpParam(401);
                tcp.writeS(money);
                System.out.println("发送服务端金额:"+money);
                try {
                    byte[] bytes = BaseNetTool.appendHead2(tcp.getParam2());
                    MinaClient.getinstance().sendMessage(sockip, bytes, context);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public void send300(final String sockip, final Context context, final String ordid, final String money, final String note, final String payNumber){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                TcpParam tcp=new TcpParam(300);
                tcp.writeS(ordid);
                tcp.writeS(money);
                tcp.writeS(note);
                tcp.writeS(payNumber);
                try {
                    byte[] bytes = BaseNetTool.appendHead2(tcp.getParam2());
                    MinaClient.getinstance().sendMessage(sockip,bytes,context);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public void send302(final String sockip, final String cloud_url, final Context context, final String sign){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                short length = (short) cloud_url.getBytes().length;
                if (TextUtils.isEmpty(sign))return;
                int slen = sign.getBytes().length;
                byte[] data=new byte[10+length+slen];
                int position=0;
                BaseNetTool.writeInt(302,data,position);
                position+=4;
                BaseNetTool.writeShort(length,data,position);
                position+=2;
                BaseNetTool.writeUTF8_2(cloud_url,data,position);
                position+=length;
                BaseNetTool.writeInt(slen,data,position);
                position+=4;
                BaseNetTool.writeUTF8_2(sign,data,position);
                position+=slen;
                System.out.println("打印url："+cloud_url+"sign:"+sign);
                try {
                    byte[] bytes = BaseNetTool.appendHead2(data);
                    MinaClient.getinstance().sendMessage(sockip,bytes,context);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
    public void send310(final String sockip, final Context context){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                byte[] data=new byte[8];
                int position=0;
                BaseNetTool.writeInt(310,data,position);
                position+=4;
                try {
                    byte[] bytes = BaseNetTool.appendHead2(data);
                    MinaClient.getinstance().sendMessage(sockip,bytes,context);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }
    public void send311(final String sockip, final Context context){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                byte[] data=new byte[4];
                int position=0;
                BaseNetTool.writeInt(311,data,position);
                try {
                    byte[] bytes = BaseNetTool.appendHead2(data);
                    MinaClient.getinstance().sendMessage(sockip,bytes,context);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void send199(final String sockip, final Context context){
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                byte[]data=new byte[4];
                int position=0;
                BaseNetTool.writeInt(199,data,position);
//                position+=4;
//                BaseNetTool.writeInt(1,data,position);
                try {
                    byte[] bytes = BaseNetTool.appendHead2(data);
                    MinaClient.getinstance().sendMessage(sockip,bytes,context);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private static final int MIN_DELAY_TIME= 8000;  // 两次点击间隔不能少于8000ms
    private static long lastClickTime;

    public static boolean isNormlScrol() {
        boolean flag = false;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= MIN_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = currentClickTime;
        return flag;
    }
    private static final int TIME= 2*60*1000;  //
//    private static final int TIME= 20*1000;  // 两次点击间隔不能少于8000ms

    public static boolean isWaitEWM(long lastTime) {
        boolean flag = false;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastTime) <= TIME) {
            flag = true;
        }

        return flag;
    }


    public byte[] addSize(byte[] old,int size){
        int length = old.length;
        int newsize=length+size;
        byte[] bytes = Arrays.copyOf(old, newsize);
        return bytes;
    }

}
