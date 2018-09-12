package com.geocompass.collect.coordinate;

import android.app.Application;
import android.content.Context;

/**
 * Created by admin on 2018/7/20.
 */

public class MyApplication extends Application {

    public static MyApplication MyInstance;
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        MyInstance = this;
        context = getApplicationContext();
    }
    public static Context getContextObject(){
        return context;
    }
    public static MyApplication getMyInstance() {
        return MyInstance;
    }
}