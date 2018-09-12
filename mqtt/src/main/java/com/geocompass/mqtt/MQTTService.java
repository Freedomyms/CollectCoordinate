package com.geocompass.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by gxsn on 2018/6/8.
 */

public class MQTTService extends Service {
    public static final String TAG = MQTTService.class.getSimpleName();
    
    private static MqttAndroidClient mqttClient;
    private MqttConnectOptions connectionOptions;
    
    private static final String LAST_WILL_TOPIC = "/topic/will";
    private String host = "ws://219.234.147.220:61623"; // 模拟器中的ip地址
    private String userName = "admin";
    private String passWord = "password";
    private String clientId = "test1";
    private Handler mWorkHandler;
    private MQTTBinder mBinder = new MQTTBinder();
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       return START_STICKY;
    }
    
    
    @Override
    public void onDestroy() {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
    
    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        if (!mqttClient.isConnected() && isConnectIsNormal()) {
            try {
                mqttClient.connect(connectionOptions, null, iMqttActionListener);
                Message message = new Message();
                message.what = MQTTConnectionConstants.STATE_CHANGE;
                message.arg1 = MQTTConnectionConstants.STATE_CONNECTING;
                mWorkHandler.sendMessage(message);
            } catch (MqttException e) {
                e.printStackTrace();
                Message message = new Message();
                message.what = MQTTConnectionConstants.STATE_CHANGE;
                message.arg1 = MQTTConnectionConstants.STATE_CONNECTION_FAILED;
                mWorkHandler.sendMessage(message);
            }
        }
    }
    
    // MQTT是否连接成功
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken arg0) {
            Message message = new Message();
            message.what = MQTTConnectionConstants.STATE_CHANGE;
            message.arg1 = MQTTConnectionConstants.STATE_CONNECTED;
            mWorkHandler.sendMessage(message);
        }
        
        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Message message = new Message();
            message.what = MQTTConnectionConstants.STATE_CHANGE;
            message.arg1 = MQTTConnectionConstants.STATE_CONNECTION_FAILED;
            mWorkHandler.sendMessage(message);
        }
    };
    
    
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
            Message message = new Message();
            message.what = MQTTConnectionConstants.MQTT_RAW_PUBLISH;
            MQTTMessage publish = new MQTTMessage();
            publish.topic = topic;
            publish.message = mqttMessage.getPayload();
            message.obj = publish;
            mWorkHandler.sendMessage(message);
        }
        
        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
        
        }
        
        @Override
        public void connectionLost(Throwable arg0) {
            Message message = new Message();
            message.what = MQTTConnectionConstants.STATE_CHANGE;
            message.arg1 = MQTTConnectionConstants.STATE_CONNECTION_FAILED;
            mWorkHandler.sendMessage(message);
        }
    };
    
    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }
    
    
    /**
     * 连接服务
     */
    public void connect() {
        mqttClient = new MqttAndroidClient(this, host, clientId);
        mqttClient.setCallback(mqttCallback);
        connectionOptions = new MqttConnectOptions();
        connectionOptions.setCleanSession(true);
        connectionOptions.setConnectionTimeout(10);
        connectionOptions.setKeepAliveInterval(20);
        connectionOptions.setUserName(userName);
        connectionOptions.setPassword(passWord.toCharArray());
        String message = "{terminal_uid:" + clientId + "}";
        connectionOptions.setWill(LAST_WILL_TOPIC, message.getBytes(), 0, false);
        doClientConnection();
    }
    
    public void disconnect(){
        if(mqttClient.isConnected()){
            try {
                mqttClient.disconnect();
                mqttClient = null;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void unSubscribe(String topic) {
        try {
            mqttClient.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    
  
    public void subscribe(String topic) {
        String[] topics = {topic};
        subscribe(topics);
    }
    
    
    
   
    public void subscribe(String[] topics) {
        int[] qoss = new int[topics.length];
        for (int i = 0; i < qoss.length; i++)
            qoss[i] = 1;
        subscribe(topics, qoss);
    }
    
  
    public void subscribe(String topic, int qos) {
        String[] topics = {topic};
        int[] qoss = {qos};
        subscribe(topics, qoss);
    }
    
    
    public void subscribe(String[] topics, int[] qoss) {
        try {
            mqttClient.subscribe(topics,qoss);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * 发送消息
     *
     * @param topic
     * @param msg
     */
    public static void publish(String topic, String msg) {
        Integer qos = 1;
        Boolean retained = false;
        try {
            mqttClient.publish(topic, msg.getBytes(), qos, retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    
    public class MQTTBinder extends Binder {
        public MQTTService getService() {
            return MQTTService.this;
        }
    }
    
    public String getHost() {
        return host;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPassWord() {
        return passWord;
    }
    
    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
    
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public Handler getWorkHandler() {
        return mWorkHandler;
    }
    
    public void setWorkHandler(Handler mWorkHandler) {
        this.mWorkHandler = mWorkHandler;
    }
}
