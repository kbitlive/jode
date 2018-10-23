package com.tools.payhelper.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tools.payhelper.CustomApplcation;
import com.tools.payhelper.R;
import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.QrCodeBean;

import java.util.ArrayList;

public class QrcodeListActivity extends Activity {

    private ArrayList<QrCodeBean> qrCodeBeans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);
//        ListView mlist= (ListView) findViewById(R.id.mlist);
//        DBManager dbManager=new DBManager(CustomApplcation.getInstance().getApplicationContext());
//        qrCodeBeans = dbManager.FindQrcodeAll();
//        mlist.setAdapter(new MyAdapter());


    }

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return qrCodeBeans.size();
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
            QrCodeBean qrCodeBean = qrCodeBeans.get(position);
            TextView tv_money= (TextView) inflate.findViewById(R.id.tv_money);
            TextView tv_mark= (TextView) inflate.findViewById(R.id.tv_mark);

            tv_money.setText(qrCodeBean.getMoney());
            tv_mark.setText(qrCodeBean.getMark());
            return inflate;
        }
    }

}
