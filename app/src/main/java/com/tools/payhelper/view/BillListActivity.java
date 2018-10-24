package com.tools.payhelper.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.tools.payhelper.ConFigNet;
import com.tools.payhelper.CustomApplcation;
import com.tools.payhelper.R;
import com.tools.payhelper.beans.PayItems;
import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.OrderBean;
import com.tools.payhelper.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BillListActivity extends Activity implements AutoListView.OnRefreshListener,
        AutoListView.OnLoadListener {
    ArrayList<OrderBean> orderBeans;
    private int pages = 1;
    private int toatolPage;
    long lasttime=0;
    String payType="";
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            List<String> result = (List<String>) msg.obj;
            switch (msg.what) {
                case AutoListView.REFRESH:
                    lstv.onLoadComplete();
                    lstv.onRefreshComplete();
                    mlist.clear();
                    break;
                case AutoListView.LOAD:
                    lstv.onLoadComplete();
                    break;
            }
            lstv.setResultSize(result.size());
            adapter.notifyDataSetChanged();
        };
    };
    private AutoListView lstv;
    private MyAdapter adapter;
    private TextView tv_all;
    private TextView tv_alipay;
    private TextView tv_wechat;
    ArrayList<PayItems> mlist =new ArrayList<>();
    private RadioGroup rd_group;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);
        DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());
        orderBeans= dbManager.FindFailOrders();
        lstv = (AutoListView) findViewById(R.id.mlist);
        findView();
        setListener();
        sendToServer(pages,"alipay");

    }

    private void setListener() {
        rd_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View viewById = rd_group.findViewById(checkedId);
                if (!viewById.isPressed()){
                    return;
                }
                if (checkedId==R.id.rb_all){
                    payType="";
                }else if (checkedId==R.id.rb_alipay){
                    payType="alipay";
                }else{
                    payType="weixin";
                }
                mlist.clear();
                sendToServer(pages,payType);
            }
        });
    }

    private void sendToServer(int page,String payType) {
        ConFigNet configNet = new ConFigNet();
        String uname = configNet.getuname(this, "uname");
        RequestParams params=new RequestParams();
        params.addBodyParameter("deviceName","honghai002");
        params.addBodyParameter("payType",payType);
        params.addBodyParameter("page",String.valueOf(page));
        params.addBodyParameter("limit","10");
        HttpUtils httpUtils=new HttpUtils(15000);
        httpUtils.send(HttpRequest.HttpMethod.POST, ConFigNet.hitory,params, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result=responseInfo.result;
                try {
                    lstv.onLoadComplete();
                    lstv.onRefreshComplete();
                    JSONObject json=new JSONObject(result);
                    int status = json.getInt("status");
                    if (200==status){//获取成功
                        JSONObject total = json.getJSONObject("total");
                        int count = total.getInt("count");
                        if (0==count)return;
                        double alipayMoney = total.getDouble("alipayMoney");
                        double weixinMoney = total.getDouble("weixinMoney");
                        double data=count/10.00;
                        toatolPage = (int) Math.ceil(data);
                        tv_alipay.setText("支付宝:"+alipayMoney+"元");
                        tv_all.setText("总收款:"+(alipayMoney+weixinMoney)+"元");
                        tv_wechat.setText("微信:"+weixinMoney+"元");
                        JSONArray list = json.getJSONArray("list");
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject jsonObject = list.getJSONObject(i);
                            double amount = jsonObject.getDouble("amount");
                            String tradeNo = jsonObject.getString("tradeNo");
                            String orderId = jsonObject.getString("orderId");
                            long notifyTime = jsonObject.getLong("notifyTime");
                            int notifyStatus = jsonObject.getInt("notifyStatus");
                            long updateTime = jsonObject.getLong("updateTime");
                            String merchantOrderId = jsonObject.getString("merchantOrderId");
                            int deviceId = jsonObject.getInt("deviceId");
                            long merchantUid = jsonObject.getLong("merchantUid");
                            int payType = jsonObject.getInt("payType");
                            double money = jsonObject.getDouble("money");
                            long createTime = jsonObject.getLong("createTime");
                            String mark = jsonObject.getString("mark");
                            int status1 = jsonObject.getInt("status");
                            PayItems pay=new PayItems();
                            pay.setAmount(amount);
                            pay.setCreateTime(createTime);
                            pay.setDeviceId(deviceId);
                            pay.setMark(mark);
                            pay.setMoney(money);
                            pay.setMerchantOrderId(merchantOrderId);
                            pay.setPayType(payType);
                            pay.setMerchantUid(merchantUid);
                            pay.setStatus(status1);
                            pay.setUpdateTime(updateTime);
                            pay.setOrderId(orderId);
                            pay.setTradeNo(tradeNo);
                            pay.setNotifyTime(notifyTime);
                            pay.setNotifyStatus(notifyStatus);
                            mlist.add(pay);
                        }
                        adapter.notifyDataSetChanged();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(HttpException e, String s) {
                lstv.onLoadComplete();
                lstv.onRefreshComplete();
                adapter.notifyDataSetChanged();
                Toast.makeText(BillListActivity.this,s,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void findView() {
        rd_group = findViewById(R.id.rd_group);
        adapter = new MyAdapter();
        lstv.setAdapter(adapter);
        lstv.setOnRefreshListener(this);
        lstv.setOnLoadListener(this);
        tv_all = findViewById(R.id.tv_all);
        tv_alipay = findViewById(R.id.tv_alipay);
        tv_wechat = findViewById(R.id.tv_wechat);
    }

    @Override
    public void onRefresh() {
        pages=1;
        mlist.clear();
        sendToServer(1,payType);
        lasttime=System.currentTimeMillis();
    }

    @Override
    public void onLoad() {
        if (System.currentTimeMillis()-lasttime<500)return;
        pages += 1;
        if (pages * 10 <= toatolPage * 10) {
            sendToServer(1,payType);
        } else {
            lstv.onLoadComplete();
            Toast.makeText(BillListActivity.this,"已加载完所有数据！",Toast.LENGTH_LONG).show();
        }

    }
    private void loadData(final int what) {
        // 这里模拟从服务器获取数据
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = handler.obtainMessage();
                msg.what = what;
                msg.obj = getData();
                handler.sendMessage(msg);
            }
        }).start();
    }
    // 测试数据
    public List<String> getData() {
        List<String> result = new ArrayList<String>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            long l = random.nextInt(10000);
            result.add("当前条目的ID：" + l);
        }
        return result;
    }
    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View inflate = getLayoutInflater().inflate(R.layout.bill_item, null);
            TextView tv_time= (TextView) inflate.findViewById(R.id.tv_time);
            TextView tv_money= (TextView) inflate.findViewById(R.id.tv_money);
            TextView tv_mark= (TextView) inflate.findViewById(R.id.tv_mark);
            PayItems payItems = mlist.get(position);
            tv_time.setText(Utils.getsjctotime(payItems.getCreateTime(),"yyyy-MM-dd HH:mm"));
            tv_money.setText(String.valueOf(payItems.getMoney()));
            tv_mark.setText(payItems.getMark());
             mlist.get(position);
            return inflate;
        }
    }
}
