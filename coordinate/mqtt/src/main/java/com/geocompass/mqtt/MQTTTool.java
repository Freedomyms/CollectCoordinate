package com.geocompass.mqtt;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.geocompass.mqtt.MQTTConnectionConstants.MQTT_RAW_PUBLISH;
import static com.geocompass.mqtt.MQTTConnectionConstants.STATE_CHANGE;
import static com.geocompass.mqtt.MQTTConnectionConstants.STATE_CONNECTED;
import static com.geocompass.mqtt.MQTTConnectionConstants.STATE_CONNECTING;
import static com.geocompass.mqtt.MQTTConnectionConstants.STATE_CONNECTION_FAILED;
import static com.geocompass.mqtt.MQTTConnectionConstants.STATE_NONE;


/**
 * Created by gxsn on 2018/6/4.
 */

public class MQTTTool {


    private static final String TAG = "MQTTTool";
    private ServiceConnection serviceConnection;

    private static final String MQTT_USER_NAME = "admin";
    private static final String MQTT_PASSWORD = "password";
    private static  String MQTT_HOST = "ws://219.234.147.220:61623";
    private static final int MQTT_PORT = 61613;

    private Context mContext;
    private MQTTToolCallback mCallBack;
    private MQTTHandler mHandler;
    private MQTTService mService;
    List<String> mTopicList;

    public interface MQTTToolCallback {
        /**
         * 正在连接
         */
        void onConnecting();

        /**
         * 连接成功
         */
        void onConnectSuccess();

        /**
         * 连接失败
         */
        void onConnectFailed();

        /**
         * 收到消息
         */
        void onPublish(MQTTMessage message);

    }


    /**
     * MQTT 消息服务 辅助工具
     *
     * @param context
     * @param callback
     */
    public MQTTTool(Context context, MQTTToolCallback callback,String MQTT_host) {
        mContext = context;
        mCallBack = callback;
        mHandler = new MQTTHandler(context.getMainLooper(), this);
        MQTT_HOST=MQTT_host;
    }

    private void subscribe() {
        if (mTopicList == null) return;
        String[] topicArray = new String[mTopicList.size()];
        mTopicList.toArray(topicArray);
        mService.subscribe(topicArray);
    }
    
    

    /**
     * 注册话题
     *
     * @param topic
     */
    public void subscribeTopic(String topic) {
        if (mTopicList == null) mTopicList = new ArrayList<>();
        mTopicList.add(topic);

        if(mService!=null)mService.subscribe(topic);
    }
    
    public void unSubscribeTopic(String topic){
        if(topic==null)return;
        if(mTopicList != null){
            Iterator<String> iterator  = mTopicList.iterator();
            while (iterator.hasNext()){
                String t = iterator.next();
                if(topic.equals(t)){
                    iterator.remove();
                }
            }
        }
        if(mService!=null)mService.unSubscribe(topic);
    }

    /**
     * 发送消息
     * @param topic
     * @param message
     */
    public void sendMessage(String topic,String message){
        mService.publish(topic,message);
    }

    /**
     * 绑定服务
     */
    public void bindMqttService() {
        Intent intent = new Intent(mContext, MQTTService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                    MQTTService.MQTTBinder binder = (MQTTService.MQTTBinder) service;
                    mService = binder.getService();
                mService.setClientId(DeviceUtil.getUniqueId(mContext));
                mService.setHost(MQTT_HOST);
                mService.setUserName(MQTT_USER_NAME);
                mService.setPassWord(MQTT_PASSWORD);
                mService.setWorkHandler(mHandler);
                mService.connect();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG,"服务连接失败");
            }
        };
        mContext.bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    /**
     * 解绑服务
     */
    public void unbindMqttService() {
        mContext.unbindService(serviceConnection);
    }

    private static class MQTTHandler extends Handler {
        private WeakReference<MQTTTool> mTool;

        public MQTTHandler(Looper looper, MQTTTool tool) {
            super(looper);
            mTool = new WeakReference<>(tool);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STATE_CHANGE:
                    switch (msg.arg1) {
                        case STATE_NONE:
                            return;
                        case STATE_CONNECTING:
                            mTool.get().mCallBack.onConnecting();
                            return;
                        case STATE_CONNECTED:
                            mTool.get().subscribe();
                            mTool.get().mCallBack.onConnectSuccess();
                            return;
                        case STATE_CONNECTION_FAILED:
                            mTool.get().mCallBack.onConnectFailed();
                            return;
                    }
                    return;
                case MQTT_RAW_PUBLISH:
//                    MQTTPublish publish = (MQTTPublish) msg.obj;
                    MQTTMessage message = (MQTTMessage) msg.obj;
                    mTool.get().mCallBack.onPublish(message);
                    return;
                default:
                    return;
            }
        }
    }

}

//package com.geocompass.mqtt;
//
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.Handler;
//import android.os.IBinder;
//import android.os.Looper;
//import android.os.Message;
//
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.List;
//
//import static android.content.Context.BIND_AUTO_CREATE;
//
//import static com.geocompass.mqtt.MQTTConnectionConstants.STATE_CHANGE;
//import static com.geocompass.mqtt.MQTTConnectionConstants.STATE_CONNECTED;
//import static com.geocompass.mqtt.MQTTConnectionConstants.STATE_CONNECTING;
//import static com.geocompass.mqtt.MQTTConnectionConstants.STATE_CONNECTION_FAILED;
//import static com.geocompass.mqtt.MQTTConnectionConstants.STATE_NONE;
//import static se.wetcat.qatja.MQTTConstants.PUBLISH;
//
///**
// * Created by gxsn on 2018/6/4.
// */
//
//public class MQTTTool {
//
//
//    private ServiceConnection serviceConnection;
//
//    private static final String MQTT_USER_NAME = "admin";
//    private static final String MQTT_PASSWORD = "password";
//    private static final String MQTT_HOST = "219.234.147.220";
//    private static final int MQTT_PORT = 61613;
//
//    private Context mContext;
//    private MQTTToolCallback mCallBack;
//    private MQTTHandler mHandler;
//    private QatjaService mMqttService;
//    List<String> mTopicList;
//
//    public interface MQTTToolCallback {
//        /**
//         * 正在连接
//         */
//        void onConnecting();
//
//        /**
//         * 连接成功
//         */
//        void onConnectSuccess();
//
//        /**
//         * 连接失败
//         */
//        void onConnectFailed();
//
//        /**
//         * 收到消息
//         */
//        void onPublish(MQTTMessage publish);
//
//    }
//
//    public void sendMessage(String topic,String message){
//        mMqttService.publish(topic,message);
//    }
//
//    /**
//     * MQTT 消息服务 辅助工具
//     *
//     * @param context
//     * @param callback
//     */
//    public MQTTTool(Context context, MQTTToolCallback callback) {
//        mContext = context;
//        mCallBack = callback;
//        mHandler = new MQTTHandler(context.getMainLooper(), this);
//    }
//
//    private void subscribe() {
//        if (mTopicList == null) return;
//        String[] topicArray = new String[mTopicList.size()];
//        mTopicList.toArray(topicArray);
//        mMqttService.subscribe(topicArray);
//    }
//
//    /**
//     * 注册话题
//     *
//     * @param topic
//     */
//    public void subscribeTopic(String topic) {
//        if (mTopicList == null) mTopicList = new ArrayList<>();
//        if (mMqttService != null && mMqttService.getState() == STATE_CONNECTED) {
//            mMqttService.subscribe(topic);
//        }
//        mTopicList.add(topic);
//    }
//
//    /**
//     * 绑定服务
//     */
//    public void bindMqttService() {
//        Intent intent = new Intent(mContext, QatjaService.class);
//        serviceConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                QatjaService.QatjaBinder binder = (QatjaService.QatjaBinder) service;
//                mMqttService = binder.getService();
//                mMqttService.setIdentifier(DeviceUtil.getUniqueId(mContext));
//                mMqttService.setHost(MQTT_HOST);
//                mMqttService.setPort(MQTT_PORT);
//                mMqttService.setUserName(MQTT_USER_NAME);
//                mMqttService.setPawword(MQTT_PASSWORD);
//                mMqttService.setHandler(mHandler);
//                mMqttService.setCleanSession(true);
//                mMqttService.connect();
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//
//            }
//        };
//        mContext.bindService(intent, serviceConnection, BIND_AUTO_CREATE);
//    }
//
//    /**
//     * 解绑服务
//     */
//    public void unbindMqttService() {
//        mContext.unbindService(serviceConnection);
//    }
//
//    private static class MQTTHandler extends Handler {
//        private WeakReference<MQTTTool> mTool;
//
//        public MQTTHandler(Looper looper, MQTTTool tool) {
//            super(looper);
//            mTool = new WeakReference<>(tool);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case STATE_CHANGE:
//                    switch (msg.arg1) {
//                        case STATE_NONE:
//                            return;
//                        case STATE_CONNECTING:
//                            mTool.get().mCallBack.onConnecting();
//                            return;
//                        case STATE_CONNECTED:
//                            mTool.get().subscribe();
//                            mTool.get().mCallBack.onConnectSuccess();
//                            return;
//                        case STATE_CONNECTION_FAILED:
//                            mTool.get().mCallBack.onConnectFailed();
//                            return;
//                    }
//                    return;
//                case PUBLISH:
//                    se.wetcat.qatja.messages.MQTTPublish publish = (se.wetcat.qatja.messages.MQTTPublish) msg.obj;
////                    publish.get
//                    MQTTMessage publish1 = new MQTTMessage();
//                    publish1.topic = publish.getTopicName();
//                    publish1.message = publish.getPayload();
//                    mTool.get().mCallBack.onPublish(publish1);
//                    return;
//                default:
//                    return;
//            }
//        }
//    }
//
//}
