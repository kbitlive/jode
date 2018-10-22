package com.tools.payhelper.eventbus;

public class Logging extends baseVo{
    private String jsonData;
    public Logging(byte[] buffer) {
        super(buffer);
        jsonData=mTcprespond.getUTF_8();
    }

    public String getJsonData() {
        return jsonData;
    }
}
