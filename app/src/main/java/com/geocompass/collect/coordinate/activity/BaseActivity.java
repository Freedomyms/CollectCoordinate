package com.geocompass.collect.coordinate.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2018/9/13.
 */

public class BaseActivity extends AppCompatActivity {

    private static final List<Activity> mActivies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mActivies.add(this);
        transparentStatusBar();
    }

    @Override
    protected void onDestroy() {
        mActivies.remove(this);
        super.onDestroy();
    }

    /**
     * 隐藏状态栏
     */
    protected void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 透明状态栏
     */
    protected void transparentStatusBar() {
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 不显示标题栏
     */
    protected void hideTitleBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }


    @Override
    public void finish() {
        super.finish();
    }

    /**
     * 推出应用，关闭所有Activity
     */
    public static void finishAll() {
        List<Activity> list = new ArrayList<>();
        for (Activity activity : mActivies) {
            list.add(activity);
        }
        for (Activity activity : list) {
            activity.finish();
        }
        System.exit(0);
    }

}