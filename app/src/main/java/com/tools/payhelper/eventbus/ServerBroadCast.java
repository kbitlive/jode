package com.tools.payhelper.eventbus;

/**
 * 104系统广播消息
 */
public class ServerBroadCast extends baseVo {
        String data;
    public ServerBroadCast(byte[] buffer) {
        super(buffer);
        data=mTcprespond.getUTF_8();
    }
    public String getData() {
        return data;
    }
}
