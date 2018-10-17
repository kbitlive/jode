package com.tools.payhelper.mina;

/**
 * Created by Administrator on 2016/7/21 0021.
 */
public interface MinaMessageListener {
     void onReceive(int cmd, byte[] messge);
}
