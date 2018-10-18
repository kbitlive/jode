package com.tools.payhelper.eventbus;

/**
 * cmd=202
 */
public class AlipayReciveMoney extends baseVo {

    private String json;//json字符串


    public AlipayReciveMoney(byte[] buffer) {
        super(buffer);
        json=mTcprespond.getUTF_8();

    }

    public String getJson() {
        return json;
    }

    @Override
    public String toString() {
        return "AlipayReciveMoney{" +
                "json='" + json + '\'' +
                ", cmd=" + cmd +
                ", mTcprespond=" + mTcprespond +
                '}';
    }
}
