package com.tools.payhelper.eventbus;
import com.tools.payhelper.utils.TcpRespond;

public class baseVo {
    protected int cmd;
   protected TcpRespond mTcprespond;
    baseVo(byte[] buffer){
        mTcprespond =new TcpRespond(buffer);
        mTcprespond.getInt();
    }

    public baseVo() {
    }

    public int getCmd() {
        return cmd;
    }
}
