package com.tools.payhelper;

import java.util.List;

import com.tools.payhelper.utils.DBManager;
import com.tools.payhelper.utils.OrderBean;
import com.tools.payhelper.utils.PayHelperUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
//		DBManager dbManager=new DBManager(context);
//		List<OrderBean> orderBeans=dbManager.FindAllOrders();
//		for (OrderBean orderBean : orderBeans) {
//			PayHelperUtils.notify(context, orderBean.getType(), orderBean.getNo(), orderBean.getMoney(), orderBean.getMark(), orderBean.getDt());
//		}
	}

}
