package com.geocompass.collect.coordinate.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.geocompass.collect.coordinate.R;
import com.geocompass.collect.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import static com.geocompass.collect.coordinate.activity.SettingActivity.PREFERENCE_LINK;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEtUsername, mEtPass, mEtId;
    private String mInputUser, mInputPass, mInputId;
    public static final String INPUT_ID = "id";
    public static final String PREFERENCE_USERNAME="preference_username";
    public static final String PREFERENCE_PASS="preference_pass";
    public static final String PREFERENCE_ID="preference_id";
    private static final int PERMISSION_REQUEST_CODE = 0x111;
    public static final String SERVICE="ws://219.234.147.220:61623";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkPermission();
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        SharedPreferencesUtils.init(LoginActivity.this);

        initView();
        readFromSharedPreference();
        SharedPreferencesUtils.putString(PREFERENCE_LINK,SERVICE);
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
            intent.putExtra(INPUT_ID, mInputId);
            startActivity(intent);
            readFromSharedPreference();
        }
    }

    private void saveToSharedPreference() {
        SharedPreferencesUtils.putString(PREFERENCE_USERNAME,mInputUser);
        SharedPreferencesUtils.putString(PREFERENCE_PASS,mInputPass);
        SharedPreferencesUtils.putString(PREFERENCE_ID,mInputId);
    }
    private void readFromSharedPreference() {
        String user = SharedPreferencesUtils.getString(PREFERENCE_USERNAME,"");
        if(!user.trim().isEmpty()){
            mEtUsername.setText(user);
        }
        String pass = SharedPreferencesUtils.getString(PREFERENCE_PASS,"");
        if(!pass.trim().isEmpty()){
            mEtPass.setText(pass);
        }
        String id = SharedPreferencesUtils.getString(PREFERENCE_ID,"");
        if(!id.trim().isEmpty()){
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
    private boolean checkPermission(){
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
            ActivityCompat.requestPermissions(LoginActivity.this, permissions, PERMISSION_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
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
