package com.geocompass.collect.coordinate.activity;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.geocompass.collect.coordinate.R;
import com.geocompass.collect.util.SharedPreferencesUtils;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mEtLink;
    public static final String PREFERENCE_LINK = "perence_link";
    private String mInputLik;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        SharedPreferencesUtils.init(SettingActivity.this);
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        mEtLink = (EditText) findViewById(R.id.et_link);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.iv_setting_back).setOnClickListener(this);
        String link = SharedPreferencesUtils.getString(PREFERENCE_LINK, "");
        if (link.trim()!=null) {
            mEtLink.setText(link);
            mInputLik = mEtLink.getText().toString().trim();
        }else {
            mEtLink.setText("没有服务器地址！");
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                if (mInputLik.isEmpty()) {
                    Toast.makeText(SettingActivity.this, "链接地址为空，请输入", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferencesUtils.putString(PREFERENCE_LINK, mInputLik);
                Toast.makeText(SettingActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                break;
            case R.id.iv_setting_back:
                finish();
                break;
            default:
                break;
        }
    }
}
