package com.geocompass.collect.coordinate.activity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.geocompass.collect.coordinate.Constants;
import com.geocompass.collect.coordinate.R;
import com.geocompass.collect.util.SharedPreferencesUtils;



public class SettingActivity extends BaseActivity implements View.OnClickListener {
    private EditText mEtLink;
    public static final String PREFERENCE_LINK = "perence_link";
    private String mInputLik;
    private static boolean isFirst=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        SharedPreferencesUtils.init(SettingActivity.this);
        mEtLink = (EditText) findViewById(R.id.et_link);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.iv_setting_back).setOnClickListener(this);
        String link=null;
        if(isFirst){
             link = SharedPreferencesUtils.getString(Constants.PREFERENCE_LINK, Constants.SERVICE);
        }else {
             link = SharedPreferencesUtils.getString(Constants.PREFERENCE_LINK, "_");
        }
        //Log.e("SettingActivity",link);

        if (!link.trim().equals("_") ){
            mEtLink.setText(link);
            mInputLik = mEtLink.getText().toString().trim();
        }else {
            mEtLink.setText("没有服务器地址！");
        }
        SharedPreferencesUtils.putString(Constants.PREFERENCE_LINK, mInputLik);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                mInputLik = mEtLink.getText().toString().trim();
                if (mInputLik.isEmpty()) {
                    Toast.makeText(SettingActivity.this, "链接地址为空，请输入", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferencesUtils.putString(Constants.PREFERENCE_LINK, mInputLik);
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
