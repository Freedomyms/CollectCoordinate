package com.geocompass.collect.coordinate.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.geocompass.collect.util.CalcUtils;
import com.geocompass.collect.util.GDLocationUtils;
import com.geocompass.collect.util.PositionUtil;
import com.geocompass.collect.coordinate.R;
import com.geocompass.collect.coordinate.bean.SendGpsBean;
import com.geocompass.collect.util.SharedPreferencesUtils;
import com.geocompass.mqtt.MQTTMessage;
import com.geocompass.mqtt.MQTTTool;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.geocompass.collect.coordinate.activity.SettingActivity.PREFERENCE_LINK;


public class MainActivity extends AppCompatActivity {
    private static final String TOPIC_GPS = "dtid_gps";

    private TextView mTvLat,mTvLon, mTvSendCount, mTvService, mTvId;
    private LatLng mCurLnglat;
    private LatLng mPreLnglat;
    private MQTTTool mMqttTool;
    private static final String WEB_FLY_TOPIC = "collect_coordinate";
    private SendGpsBean mGpsBean;
    private Gson mGson;
    private String MQTT_host = "ws://219.234.147.220:61623";

    private MapView mMapView;
    private AMap aMap;
    private UiSettings mUiSettings;//定义一个UiSettings对象
    CameraUpdate cameraUpdate;
    //定位蓝点
    MyLocationStyle myLocationStyle;

    List<LatLng> latLngs = new ArrayList<LatLng>();
    Polyline mPolyline;
    private long mCount = 1;
    private String mID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView(savedInstanceState);
        dealWithData();
        getCurrentLocation();
        mUiSettings = aMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);//隐藏放大缩小按钮
        mUiSettings.setRotateGesturesEnabled(false);//旋转手势
        //蓝点初始化
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        //myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。

    }

    private void updateCurrentLocation() {
        if (mCurLnglat != null) {
            //改变可视区域为指定位置
            //CameraPosition4个参数分别为位置，缩放级别，目标可视区域倾斜度，可视区域指向方向（正北逆时针算起，0-360）
            cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(mCurLnglat, aMap.getCameraPosition().zoom, 0, 0));
            aMap.moveCamera(cameraUpdate);//地图移向指定区域
            Log.e("moveCamera", mCurLnglat + "");
            // LatLng latLng = new LatLng(33.906901,99.397972);
            //final Marker marker = aMap.addMarker(new MarkerOptions().position(mCurLnglat).title("北京").snippet("DefaultMarker"));

        }
    }

    private void recordTrackLine(double lat, double lon) {
        latLngs.add(new LatLng(lat, lon));
        Log.e("recordTrackLine", latLngs.size() + "");
        if (latLngs.size() >= 2) {
            mPolyline = aMap.addPolyline(new PolylineOptions().
                    addAll(latLngs).width(10).color(Color.argb(255, 1, 1, 1)));
            if (latLngs.size() > 500) {
                latLngs.remove(0);
            }

        }

    }

    private void getCurrentLocation() {
        GDLocationUtils gdLocationUtils = GDLocationUtils.getInstance(getApplication());
        gdLocationUtils.setLocationUpdateListener(new GDLocationUtils.LocationUpdateListener() {

            @Override
            public void onLocationUpdate(double lat, double lon) {
                recordTrackLine(lat, lon);
                // 保存轨迹
                PositionUtil.PositionModel p = PositionUtil.gcj_To_Gps84(lat, lon);

                // 保留八位小数
                double mLat = CalcUtils.decimalControl(p.getWgLat(), 8);
                double mLon = CalcUtils.decimalControl(p.getWgLon(), 8);
                mPreLnglat = mCurLnglat;
                mCurLnglat = new LatLng(mLat, mLon);
                double distance = AMapUtils.calculateLineDistance(mPreLnglat, mCurLnglat);
                Log.e("距离", distance + "");
                if (mPreLnglat.latitude != 0 && mPreLnglat.longitude != 0 && distance > 2) {
                    double y = mCurLnglat.latitude;
                    double x = mCurLnglat.longitude;
                    String sendGps = x + "," + y;
                    DecimalFormat format = new DecimalFormat("#.000000");
                    mTvLat.setText(Html.fromHtml("纬度 : <b>" + format.format(lat)));
                    mTvLon.setText(Html.fromHtml( "</b>  经度 : <b>" + (format.format(lon)) + "</b> "));
                    mTvSendCount.setText(Html.fromHtml("发送 : <b>" + mCount + "</b> 次"));
                    //mTvSendCount.setText(Html.fromHtml("Your big island <b>ADVENTURE!</b>"));
                    mCount++;
                    mGpsBean.setCoor(sendGps);
                    String s = mGson.toJson(mGpsBean);
                    //Log.e("main",s);

                    mMqttTool.sendMessage(TOPIC_GPS, s);
                }
            }

            @Override
            public boolean forceToUpload() {
                return false;
            }

            /**
             * 每隔几秒执行一次
             **/
            @Override
            public void onLocationRecord(double lat, double lon) {
               /* recordTrackLine(lat, lon);
                mCurLnglat = new LatLng(lat, lon);
                double y = lat;
                double x = lon;
                DecimalFormat format = new DecimalFormat("#.000000");

                String sendGps = x + "," + y;
                mTvLat.setText(Html.fromHtml("纬度 : <b>" + format.format(lat)));
                mTvLon.setText(Html.fromHtml( "</b>  经度 : <b>" + (format.format(lon)) + "</b> "));
                mTvSendCount.setText("发送 : " + mCount+" 次");
                mCount++;
                mGpsBean.setCoor(sendGps);
                String s = mGson.toJson(mGpsBean);
                Log.e("main", s);
                mTvSendCount.setText(Html.fromHtml("发送 : <b>" + mCount + "</b> 次"));
                mMqttTool.sendMessage(TOPIC_GPS, s);*/
            }
        });
        gdLocationUtils.startLocation();
    }

    private void initView(Bundle savedInstanceState) {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map_view);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        mTvLat = findViewById(R.id.tv_lat);
        mTvLon=findViewById(R.id.tv_lon);
        mTvSendCount = findViewById(R.id.tv_send_count);
        mTvService = findViewById(R.id.tv_service);
        mTvId = findViewById(R.id.tv_id);
        String link = SharedPreferencesUtils.getString(PREFERENCE_LINK, "");
        if (!link.trim().isEmpty()) {
            mTvService.setText("服务器 : " + link);
        }
    }

    private void dealWithData() {
        mGson = new Gson();
        mGpsBean = new SendGpsBean();
        Intent intent = getIntent();
        mID = intent.getStringExtra(LoginActivity.INPUT_ID);
        mGpsBean.setId(mID);
        String link = SharedPreferencesUtils.getString(PREFERENCE_LINK, "");
        if (!link.trim().isEmpty()) {
            MQTT_host = link;
        }
        mMqttTool = new MQTTTool(this, mMqttCallBack, MQTT_host);
        mMqttTool.subscribeTopic(TOPIC_GPS);

        mTvId.setText("ID: " + mID);
    }

    /**
     * 推送服务连接回调
     */
    private MQTTTool.MQTTToolCallback mMqttCallBack = new MQTTTool.MQTTToolCallback() {
        @Override
        public void onConnecting() {
            Toast.makeText(MainActivity.this, "正在连接", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnectSuccess() {
            Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onConnectFailed() {
            Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onPublish(MQTTMessage publish) {
            String message = new String(publish.message);
            updateCurrentLocation();
             Toast.makeText(MainActivity.this, "收到消息:" + publish.topic + ":" + message, Toast.LENGTH_SHORT).show();


            try {

            } catch (NumberFormatException | ArrayIndexOutOfBoundsException exception) {
                //Timber.e("消息格式错误:%s", message);
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mMqttTool.bindMqttService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMqttTool.unbindMqttService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

}
