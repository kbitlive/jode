package com.tools.payhelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.tools.payhelper.eventbus.AlipayReciveMoney;
import com.tools.payhelper.eventbus.Logging;
import com.tools.payhelper.mina.MinaClient;
import com.tools.payhelper.utils.Check;
import com.tools.payhelper.utils.PreferencesUtils;
import com.tools.payhelper.utils.URLRequest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LogingActivity extends Activity implements View.OnClickListener {

    private EditText edt_user;
    private EditText edt_password;
    private String uname;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_loging);
        regist();
        if (Check.is_login(this)&&MinaClient.isConnected()){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
        ConFigNet configNet = new ConFigNet();
        String uname = configNet.getuname(this, "uname");
        String password = configNet.getuname(this, "pasword");
        findViewById(R.id.btn_loging).setOnClickListener(this);
        edt_user = (EditText) findViewById(R.id.edt_user);
        edt_password = (EditText) findViewById(R.id.edt_password);
        if (!TextUtils.isEmpty(uname)){
            edt_user.setText(uname);
        }
        if (!TextUtils.isEmpty(password)){
            edt_password.setText(password);
        }
        findViewById(R.id.btn_disconnect).setOnClickListener(this);
    }

    private void regist() {
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_loging:
                logging();
                break;
            case R.id.btn_disconnect:
                MinaClient.getinstance().reLease();
                break;
        }

    }

    private void logging() {
        uname = edt_user.getText().toString().trim();
        password = edt_password.getText().toString().trim();
        if (TextUtils.isEmpty(uname)||TextUtils.isEmpty(password)){
            Toast.makeText(LogingActivity.this,"用户名或密码不能为空",Toast.LENGTH_LONG).show();
        }else {
            URLRequest.getInstance().send100(ConFigNet.socketip, LogingActivity.this, uname, password);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void LogingBack(Logging logging){
        PreferencesUtils.putBooleanToSPMap(LogingActivity.this, PreferencesUtils.Keys.IS_LOGIN, true);
        System.out.println("登陆成功");
        new ConFigNet().savedData(this,uname,password);
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }






}
