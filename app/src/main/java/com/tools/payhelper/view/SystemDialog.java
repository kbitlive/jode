package com.tools.payhelper.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.tools.payhelper.R;

public class SystemDialog extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        String title = getIntent().getStringExtra("title");
        String message = getIntent().getStringExtra("message");
        ((TextView)findViewById(R.id.tv_title)).setText(title);
        ((TextView)findViewById(R.id.tv_content)).setText(message);
        findViewById(R.id.tv_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
