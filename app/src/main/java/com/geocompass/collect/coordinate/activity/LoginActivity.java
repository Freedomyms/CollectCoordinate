package com.geocompass.collect.coordinate.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.geocompass.collect.coordinate.Constants;
import com.geocompass.collect.coordinate.R;
import com.geocompass.collect.util.AppUtils;
import com.geocompass.collect.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText mEtUsername, mEtPass, mEtId;
    private String mInputUser, mInputPass, mInputId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkPermission();
        SharedPreferencesUtils.init(LoginActivity.this);

        initView();
        readFromSharedPreference();
        judgeAppRun();
        SharedPreferencesUtils.getString(Constants.PREFERENCE_LINK, Constants.SERVICE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.iv_setting:
                Intent intent = new Intent(LoginActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void initView() {
        mEtUsername = (EditText) findViewById(R.id.et_username);
        mEtPass = (EditText) findViewById(R.id.et_pass);
        mEtId = (EditText) findViewById(R.id.et_id);

        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.iv_setting).setOnClickListener(this);
    }

    private void login() {
        mInputUser = mEtUsername.getText().toString().trim();
        mInputPass = mEtPass.getText().toString().trim();
        mInputId = mEtId.getText().toString().trim();
        if (checkAccountInput()) {
            saveToSharedPreference();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            intent.putExtra(Constants.INPUT_ID, mInputId);
            startActivity(intent);
            readFromSharedPreference();
        }
    }

    /**
     * 判断MQTTService是否运行，若运行直接跳到MainActivity
     */
    private void judgeAppRun() {
        boolean isRun = AppUtils.isServiceRunning(LoginActivity.this, this.getResources().getString(R.string.MQTTService_name));
        if (isRun) {
            login();
        }
    }

    private void saveToSharedPreference() {
        SharedPreferencesUtils.putString(Constants.PREFERENCE_USERNAME, mInputUser);
        SharedPreferencesUtils.putString(Constants.PREFERENCE_PASS, mInputPass);
        SharedPreferencesUtils.putString(Constants.PREFERENCE_ID, mInputId);
    }

    private void readFromSharedPreference() {
        String user = SharedPreferencesUtils.getString(Constants.PREFERENCE_USERNAME, "");
        if (!user.trim().isEmpty()) {
            mEtUsername.setText(user);
        }
        String pass = SharedPreferencesUtils.getString(Constants.PREFERENCE_PASS, "");
        if (!pass.trim().isEmpty()) {
            mEtPass.setText(pass);
        }
        String id = SharedPreferencesUtils.getString(Constants.PREFERENCE_ID, "");
        if (!id.trim().isEmpty()) {
            mEtId.setText(id);
        }

    }

    //验证用户名
    private boolean checkAccountInput() {
      /*  if (TextUtils.isEmpty(mInputUser)) {
            mEtUsername.requestFocus();
            mEtUsername.setError("用户名不能为空");
            return false;
        }
        if (TextUtils.isEmpty(mInputPass)) {
            mEtPass.requestFocus();
            mEtPass.setError("密码不能为空");
            return false;
        }*/
        if (TextUtils.isEmpty(mInputId)) {
            mEtId.requestFocus();
            mEtId.setError("ID不能为空");
            return false;
        }
        return true;
    }


    private boolean checkPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(LoginActivity.this, permissions, Constants.PERMISSION_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERMISSION_REQUEST_CODE) {
            boolean result = true;
            for (int granted : grantResults) {
                if (granted != PackageManager.PERMISSION_GRANTED) {
                    result = false;
                    break;
                }
            }
            if (!result) {
                alert();
            }
        }
    }

    private void alert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告")
                .setMessage("缺少应用的必要运行权限，应用无法使用")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LoginActivity.this.finish();
                    }
                });
        builder.create().show();
    }
}
