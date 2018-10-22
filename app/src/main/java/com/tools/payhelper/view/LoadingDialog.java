package com.tools.payhelper.view;

/*
 * LoadingDialog.java
 * @author Andrew Lee
 * 2014-10-20 下午4:05:04
 */


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tools.payhelper.R;
import com.tools.payhelper.utils.LoadingCancelListener;


/**
 * LoadingDialog.java description:
 *
 * @author Andrew Lee version 2014-10-20 下午4:05:04
 */
public class LoadingDialog {

    private Dialog mDialog;
    private Activity currentActivity;
    LoadingCancelListener loadcancel;
    private boolean isCancelble = true;
    private final ImageView img_loding;
    private String tishi = "";
    private final TextView tv_tishi;
    private DialogInterface.OnDismissListener onDismissListener;

    public LoadingDialog(Context context) {
        currentActivity = (Activity) context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.loading, null);
        img_loding = (ImageView) view.findViewById(R.id.img_loding);
        mDialog = new Dialog(context, R.style.Dialog_Transparent);
        mDialog.setContentView(view);
        tv_tishi = (TextView) mDialog.findViewById(R.id.tv_tishi);
        mDialog.setCanceledOnTouchOutside(false);
        AnimationDrawable drawable = (AnimationDrawable) img_loding.getDrawable();
        drawable.start();
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (null != loadcancel) loadcancel.cancel();
                    if (isCancelble) {
                        dismiss();
                    }
                    return true;
                }
                return false;
            }
        });

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (onDismissListener != null) {
                    onDismissListener.onDismiss(mDialog);
                }
            }
        });

    }

    public void setCancelble(boolean cancel) {
        this.isCancelble = cancel;
        mDialog.setCancelable(cancel);
    }

    public void setCancelListener(LoadingCancelListener loadcancel) {
        this.loadcancel = loadcancel;
    }

    public void show() {
        tv_tishi.setVisibility(View.GONE);
        if (null != currentActivity && !currentActivity.isFinishing()) mDialog.show();
    }

    public void show(String text) {
        if (!"".equals(text) && null != text) {
            tv_tishi.setText(text);
            tv_tishi.setVisibility(View.VISIBLE);
        }
        if (null != currentActivity && !currentActivity.isFinishing()) mDialog.show();
    }

    public void dismiss() {
        if (currentActivity != null && !currentActivity.isFinishing()) {
            mDialog.dismiss();
        }
    }

    public boolean isshow() {
        if (mDialog.isShowing()) {
            return true;
        }
        return false;
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

}
