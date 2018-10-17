package com.tools.payhelper.utils;
import com.tools.payhelper.mina.BaseNetTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanyingmao on 2017/2/17.
 */
//tcp 请求参数封装
public class TcpParam {
    private int cmd;
    private List<Byte> listByte;
    private int sizePosition = 0;

    public TcpParam(int cmd) {
        this.cmd = cmd;
        listByte = new ArrayList<>();
        write(cmd);
    }

    //重写write，通过参数来类型来调用对应方法，调用顺序为参数位置顺序
    public void write(short data) {
        byte[] tmpSend = new byte[2];
        BaseNetTool.writeShort(data, tmpSend, 0);
        addByte(tmpSend);
    }

    public void write(int data) {
        byte[] tmpSend = new byte[4];
        BaseNetTool.writeInt(data, tmpSend, 0);
        addByte(tmpSend);
    }

    public void write(float data) {
        byte[] tmpSend = new byte[4];
        BaseNetTool.writeFloat(data, tmpSend, 0);
        addByte(tmpSend);
    }

    public void write(double data) {
        byte[] tmpSend = new byte[8];
        BaseNetTool.writeDouble(data, tmpSend, 0);
        addByte(tmpSend);
    }

    public void write(String data) {
        int len = data.getBytes().length;
        write(len);//先写入长度
        byte[] tmpSend = new byte[len];
        BaseNetTool.writeUTF8_2(data, tmpSend, 0);
        addByte(tmpSend);
    }

    //对对应位置数据更改
    public void update(int index,int data){
        byte[] tmpSend = new byte[4];
        BaseNetTool.writeInt(data, tmpSend, 0);
        addByte(index,tmpSend);
    }

    public void writeS(String data) {
        short len = (short) data.getBytes().length;
        write(len);//先写入长度
        byte[] tmpSend = new byte[len];
        BaseNetTool.writeUTF8_2(data, tmpSend, 0);
        addByte(tmpSend);
    }
    private void addByte(byte[] tmpSend) {
        for (int i = 0; i < tmpSend.length; i++) {
            listByte.add(tmpSend[i]);
        }
    }

    private void addByte(int index,byte[] tmpSend) {
        for (int i = 0; i < tmpSend.length; i++) {
            listByte.remove(index+i);
            listByte.add(index+i,tmpSend[i]);
        }
    }
    //设置size位置
    public void setSize() {
        sizePosition = listByte.size();
    }

    //插入入size；
    private void dealSize(){
        if(sizePosition != 0){
            int size = listByte.size() + 4;
            byte[] tmpSend = new byte[4];
            BaseNetTool.writeInt(size, tmpSend, 0);
            for (int i = 0; i < tmpSend.length; i++) {
                listByte.add(sizePosition + i, tmpSend[i]);
            }
            sizePosition = 0;
        }
    }


    //获得参数
    public byte[] getParam() {
        dealSize();
        byte[] param = new byte[listByte.size()];
        for (int i = 0; i < listByte.size(); i++) {
            param[i] = listByte.get(i);
        }
        return param;
    }

    //获得参数
    public byte[] getParam2() {
        byte[] param = new byte[listByte.size()];
        for (int i = 0; i < listByte.size(); i++) {
            param[i] = listByte.get(i);
        }
        return param;
    }

    public int getCmd() {
        return cmd;
    }

    @Override
    public String toString() {
        return "TcpParam{" +
                "cmd=" + cmd +
                ", listByte=" + listByte +
                ", sizePosition=" + sizePosition +
                '}';
    }
}
