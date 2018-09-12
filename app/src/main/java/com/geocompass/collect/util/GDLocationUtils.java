package com.geocompass.collect.util;

import android.app.Application;
import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.MyLocationStyle;

/**
 * Created by yxj on 2016/6/7.
 */
public class GDLocationUtils {
    private static GDLocationUtils gdLocation;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明mLocationOption对象
    public AMapLocationClientOption mLocationOption = null;
    private double lon = 0.0;
    private double lat = 0.0;

    private LocationUpdateListener mListener;
    //定位蓝点
    MyLocationStyle myLocationStyle;

    public interface LocationUpdateListener {
        void onLocationUpdate(double lat, double lon);

        boolean forceToUpload();

        void onLocationRecord(double lat, double lon);
    }

    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {

            if (mListener != null)
                mListener.onLocationRecord(aMapLocation.getLatitude(), aMapLocation.getLongitude());

            if (aMapLocation != null) {
                if (aMapLocation.getErrorCode() == 0 &&
                        lat != aMapLocation.getLatitude()) {
//                        0.0 != aMapLocation.getLatitude()) {

                    lat = aMapLocation.getLatitude();//获取纬度
                    lon = aMapLocation.getLongitude();//获取经度


                    if (mListener != null) {
                        mListener.onLocationUpdate(lat, lon);
                    }

//                    if (getDistance(lat, lon,
//                            aMapLocation.getLatitude(), aMapLocation.getLongitude()) < 5)
//                        return;

//                    if (aMapLocation.getAccuracy() > 200) return;

                    //超出鼓浪屿的点，经纬度不记录
//                    if (lat < 24.438 && lat > 24.46 && lon < 118.052 && lon > 118.074)
//                        return;

//                    lat = aMapLocation.getLatitude();//获取纬度
//                    lon = aMapLocation.getLongitude();//获取经度
//                    if (mListener != null) mListener.onLocationUpdate(lat, lon);
                }
            }
        }
    };


    private GDLocationUtils(Context context) {
        mLocationClient = new AMapLocationClient(context);
        mLocationClient.setLocationListener(mLocationListener);
        initLocation();
    }

    public static GDLocationUtils getInstance(Application application) {
        if (gdLocation == null) gdLocation = new GDLocationUtils(application);
        return gdLocation;
    }

    private void initLocation() {
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(false);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }


    /**
     * @return 当前经度
     */
    public double getLongitude() {
        return lon;
    }

    /**
     * @return 当前纬度
     */
    public double getLatitude() {
        return lat;
    }

    /**
     * 开始定位
     */
    public void startLocation() {
        mLocationClient.startLocation();
    }

    /**
     * 停止定位
     */
    public void stopLocation() {
        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
        gdLocation = null;
    }

    /**
     * 实现接口
     *
     * @param locationUpdateListener
     */
    public void setLocationUpdateListener(LocationUpdateListener locationUpdateListener) {
        mListener = locationUpdateListener;
    }

    /**
     * 计算两点距离
     *
     * @return 米
     */
    public static double getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        final double EARTH_RADIUS = 6378137.0;
        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }


}
