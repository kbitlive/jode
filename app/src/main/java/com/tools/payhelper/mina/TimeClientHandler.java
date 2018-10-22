package com.tools.payhelper.mina;


import android.content.Context;
import android.content.Intent;

import com.tools.payhelper.CustomApplcation;
import com.tools.payhelper.MainActivity;
import com.tools.payhelper.eventbus.AlipayReciveMoney;
import com.tools.payhelper.eventbus.Logging;
import com.tools.payhelper.eventbus.NetOffLine;
import com.tools.payhelper.eventbus.ServerBroadCast;
import com.tools.payhelper.utils.PreferencesUtils;
import com.tools.payhelper.utils.URLRequest;
import com.tools.payhelper.view.DialogActivity;
import com.tools.payhelper.view.SystemDialog;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.jsoup.Connection;

import java.util.ArrayList;
import java.util.List;
public class
TimeClientHandler extends IoHandlerAdapter {
    Context mContext;
    NioSocketConnector mConnector;
    private String skIp;
    private long offlineTime = 0;// 退出判断
    private List<MinaMessageListener> listeners = new ArrayList<MinaMessageListener>();
    private String userip = "192";
    private short equipment = 0;

    public TimeClientHandler(Context context, NioSocketConnector connector) {
        mContext = context;

        mConnector = connector;
    }

    public void init(Context context, NioSocketConnector connector) {
        mContext = context;
        mConnector = connector;
    }

    public void setcontext(Context context) {
        mContext = context;
    }

    public TimeClientHandler() {

    }

    public void addMessageListener(MinaMessageListener minaCallback) {
        if (!listeners.contains(minaCallback)) {
            listeners.add(minaCallback);
        }
    }

    public void removeMessageListener(MinaMessageListener callback) {
        listeners.remove(callback);

    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        // TODO Auto-generated method stub
        super.sessionCreated(session);
        session.getConfig().setWriterIdleTime(15);
        session.getConfig().setReaderIdleTime(35);
//		}
    }

    @Override
    public void exceptionCaught(final IoSession session, Throwable cause)
            throws Exception {
        // TODO Auto-generated method stub
        super.exceptionCaught(session, cause);
        System.out.println("断线了");
        EventBus.getDefault().post(new NetOffLine());
        session.close(true);
    }

    @SuppressWarnings("static-access")
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
//		System.out.println(message);// 显示接收到的消息
        try {
            byte[] ioBuffer = (byte[]) message;
            int tt = 0;
//			System.out.println("打印包的长度："+ioBuffer.length);
            byte[] utf_8 = BaseNetTool.getUTF_8(ioBuffer, tt, 1);
            if ("|".equals(new String(utf_8))) {
                tt += 1;
                int length = BaseNetTool.Getint(ioBuffer, tt);
                tt += 4;
                byte[] reciveData = BaseNetTool.getbyte(ioBuffer, tt, length);
                tt += length;
                int k = 0;
                for (k = 0; k < reciveData.length; k++) {
                    reciveData[k] ^= 255;
                }
                int position = 0;
                int cmd = BaseNetTool.Getint(reciveData, position);
                System.out.println("收到cmd................." + cmd+".....................length:"+length);
                position += 4;
                switch (cmd) {
                    case 100:
                        Logging logging = new Logging(reciveData);
                        String jsonData = logging.getJsonData();
                        JSONObject json=new JSONObject(jsonData);
                        int loginStatus = json.getInt("loginStatus");
                        if (loginStatus==1) {
                            String opentype = json.getString("payType");
                            PreferencesUtils.putValueToSPMap(mContext.getApplicationContext(), PreferencesUtils.Keys.TYPE, opentype);
                        }
                        EventBus.getDefault().post(logging);
                        break;
                    case 101://心跳包返回
                        int lenth = BaseNetTool.Getint(reciveData, position);
                        position+=4;
                        byte[] data = BaseNetTool.getUTF_8(reciveData, position, lenth);
                        URLRequest.getInstance().send103(mContext,new String(data));
                        break;
                    case 104://广播
                        ServerBroadCast cast=new ServerBroadCast(reciveData);
                        JSONObject note=new JSONObject(cast.getData());
                        String message1 = note.getString("message");
                        Intent intent_note=new Intent(mContext,SystemDialog.class);
                        intent_note.putExtra("title","温馨提示");
                        intent_note.putExtra("message",message1);
                        intent_note.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent_note);
                        break;
                    case 102:
                        int status = BaseNetTool.Getint(reciveData, position);
                        if (1==status){
                            CustomApplcation.getInstance().setDisConnect(true);
                            MinaClient.getinstance().reLease();
                            PreferencesUtils.putBooleanToSPMap(mContext,PreferencesUtils.Keys.IS_LOGIN,false);
                            Intent intel=new Intent(mContext,DialogActivity.class);
                            intel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.getApplicationContext().startActivity(intel );
                        }
                        break;
                    case 202:
                        EventBus.getDefault().post(new AlipayReciveMoney(reciveData));
                        break;
                }
//                Intent arg0 = new Intent(String.valueOf(cmd));
//                arg0.putExtra("packge", com.tencent.charge.BuildConfig.FLAVOR);
//                arg0.putExtra("data", reciveData);
//                mContext.sendBroadcast(arg0);


            } else {
                System.out.println("解析错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
