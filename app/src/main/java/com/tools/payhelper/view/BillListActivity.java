package com.tools.payhelper.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.tools.payhelper.CustomApplcation;
import com.tools.payhelper.R;
import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.OrderBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BillListActivity extends Activity implements AutoListView.OnRefreshListener,
        AutoListView.OnLoadListener {
    ArrayList<OrderBean> orderBeans;
    private List<String> list = new ArrayList<String>();
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            List<String> result = (List<String>) msg.obj;
            switch (msg.what) {
                case AutoListView.REFRESH:
                    lstv.onLoadComplete();
                    lstv.onRefreshComplete();
                    list.clear();
                    list.addAll(result);
                    break;
                case AutoListView.LOAD:
                    lstv.onLoadComplete();
                    list.addAll(result);
                    break;
            }
            lstv.setResultSize(result.size());
            adapter.notifyDataSetChanged();
        };
    };
    private AutoListView lstv;
    private MyAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);
        findViewById(R.id.tv_reson).setVisibility(View.VISIBLE);
        DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());
        orderBeans= dbManager.FindFailOrders();
        lstv = (AutoListView) findViewById(R.id.mlist);
        adapter = new MyAdapter();
        lstv.setAdapter(adapter);
        lstv.setOnRefreshListener(this);
        lstv.setOnLoadListener(this);
        loadData(AutoListView.REFRESH);

    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lstv.onRefreshComplete();
            }
        },3000);
    }

    @Override
    public void onLoad() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                lstv.onLoadComplete();
               loadData(AutoListView.LOAD);
            }
        },3000);

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
            return list.size();
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
            TextView tv_type= (TextView) inflate.findViewById(R.id.tv_type);
            TextView tv_money= (TextView) inflate.findViewById(R.id.tv_money);
            TextView tv_mark= (TextView) inflate.findViewById(R.id.tv_mark);
            TextView tv_status= (TextView) inflate.findViewById(R.id.tv_status);
//            OrderBean orderBean = orderBeans.get(position);
//            tv_type.setText(orderBean.getType());
//            tv_money.setText(orderBean.getMoney());
//            tv_mark.setText(orderBean.getMark());
//            tv_status.setText(orderBean.getResult());
            String s = list.get(position);
            tv_type.setText(s);
            return inflate;
        }
    }
}
