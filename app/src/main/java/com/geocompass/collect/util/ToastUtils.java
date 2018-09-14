package com.geocompass.collect.util;

import android.content.Context;
import android.widget.Toast;

import com.geocompass.collect.coordinate.MyApplication;

/**
 * Created by admin on 2018/8/14.
 */

public class ToastUtils {

    private static Toast toast;

    /**
     * 吐丝的方法，可以避免重复吐丝。当你点击多次按钮的时候，吐丝只出现一次。
     *
     * @param context 上下文对象
     * @param string  吐丝的内容
     */
    public static void showToast(Context context, String string) {
        // TODO Auto-generated method stub
        if (toast == null) {
            /**
             * 如果Toast对象为空了，那么需要创建一个新的Toast对象
             *为了处理小米手机弹Toast默认带应用名
             */
            toast = Toast.makeText(context, null, Toast.LENGTH_SHORT);
        }
        // 如果toast对象还存在，那么就不再创建新的Toast对象
        toast.setText(string);
        // 最后调用show方法吐丝
        toast.show();
    }
}
