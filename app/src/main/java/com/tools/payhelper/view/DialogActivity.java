package com.tools.payhelper.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.tools.payhelper.CustomApplcation;
import com.tools.payhelper.LogingActivity;
import com.tools.payhelper.R;
import com.tools.payhelper.mina.MinaClient;
import com.tools.payhelper.utils.PreferencesUtils;

public class DialogActivity extends Activity {
    private LoadingDialog waittingDialog;
    Handler mhand=new Handler();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        waittingDialog = new LoadingDialog(this);
        setFinishOnTouchOutside(false);
        ((TextView)findViewById(R.id.tv_title)).setText("下线通知");
        ((TextView)findViewById(R.id.tv_content)).setText("您的账号已在其它设备登陆,请确保账号密码安全");
        findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                PreferencesUtils.clearSPMap(DialogActivity.this);
                startActivity(new Intent(DialogActivity.this,LogingActivity.class));
//                showDialog();
//                mhand.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        dismissDialog();
//                        PreferencesUtils.clearSPMap(DialogActivity.this);
//                        startActivity(new Intent(DialogActivity.this,LogingActivity.class));
//                    }
//                },3000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mhand.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * 显示等候对话框
     */
    public void showDialog() {
        if (!waittingDialog.isshow()) {
            waittingDialog.show();
            mhand.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (waittingDialog.isshow()) waittingDialog.dismiss();
                }
            }, 8000);
        }
    }

    /**
     * 隐藏等候对话框
     */
    public void dismissDialog() {
        if (waittingDialog != null) {
            if (waittingDialog.isshow()) {
                waittingDialog.dismiss();
            }
        }
    }

}
