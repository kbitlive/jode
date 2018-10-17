package com.tools.payhelper.utils;

import com.tools.payhelper.mina.BaseNetTool;

/**
 * Created by  on 2017/2/17.
 * 获得返回结果封装类,按顺序取出结果
 */
public class TcpRespond {
    private byte[] data;
    private int pos;
    public TcpRespond(byte[] data) {
        this.data = data;
        pos = 0;
    }
    public TcpRespond(byte[] data,int position) {
        this.data = data;
        pos = position;
    }
    public short getShort(){
        short res = BaseNetTool.getShort(data,pos);
        pos+=2;
        return res;
    }

    public int getInt(){
        int res = BaseNetTool.Getint(data,pos);
        pos+=4;
        return res;
    }

    public float getFloat(){
        float res = BaseNetTool.getFloat(data,pos);
        pos+=4;
        return res;
    }
    public double getDouble(){
        double res = BaseNetTool.getDouble(data,pos);
        pos+=8;
        return res;
    }
    public String getUTF_8(){
        int len = getInt();
        String res = new String(BaseNetTool.getUTF_8(data,pos,len));
        pos+=len;
        return res;
    }
    public String getSUTF_8(){
        short len = getShort();
        String res = new String(BaseNetTool.getUTF_8(data,pos,len));
        pos+=len;
        return res;
    }


    public void reSet(){
        pos = 0;
    }
}
