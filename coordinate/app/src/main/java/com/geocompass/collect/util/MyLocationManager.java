package com.geocompass.collect.util;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.geocompass.collect.coordinate.MyApplication;

/**
 * Created by admin on 2018/7/20.
 */

public class MyLocationManager {
    private AMapLocationClient mLocationClient;

    private static MyLocationManager mMyLocationManager;
    private LatLng mCurrentLatlng = new LatLng(0, 0);
    private double mCurrentAltitude;

    public double getCurrentAltitude() {
        return mCurrentAltitude;
    }

    private MyLocationManager() {
    }

    /**
     * 获取LocationManager的单一实例
     *
     * @return
     */
    public static MyLocationManager getInstance() {
        if (mMyLocationManager == null) {
            synchronized (MyLocationManager.class) {
                if (mMyLocationManager == null) {
                    mMyLocationManager = new MyLocationManager();
                }
            }
        }
        return mMyLocationManager;
    }

    private MyLocationChangeListener mMyLocationListener;

    /**
     * 设置我的当前位置发生变化的监听
     *
     * @param myLocationListener
     */
    public void setOnMyLocationListener(MyLocationChangeListener myLocationListener) {
        mMyLocationListener = myLocationListener;
    }

    /**
     * 检测经纬度是否处于范围内
     *
     * @param locationLat
     * @param locationLng
     * @return
     */
    public static boolean checkGpsCoordinates(double locationLat, double locationLng) {
        return (locationLat > -90 && locationLat < 90 && locationLng > -180
                && locationLng < 180) && (locationLat != 0f && locationLng != 0f);
    }

    /**
     * 获取当前的定位点坐标,这里获取的是高德坐标
     * isGPSCoordinate,如果为true，说明要转换为GPS坐标
     *
     * @return
     */
    public LatLng getCurrentLatlng(boolean isGPSCoordinate) {
        if (isGPSCoordinate) {
            PositionUtil.PositionModel positionModel = PositionUtil.gcj_To_Gps84(mCurrentLatlng.latitude, mCurrentLatlng.longitude);
            return new LatLng(positionModel.getWgLat(), positionModel.getWgLon());
        }
        return mCurrentLatlng;
    }

    /**
     * 初始化高德地图定位
     */
    public void initAMapLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(MyApplication.getContextObject());

        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        locationOption.setInterval(1500);
        //设置是否返回地址信息（默认返回地址信息）
        locationOption.setNeedAddress(true);
        //关闭缓存机制
        locationOption.setLocationCacheEnable(false);
        //启动定位时SDK会返回最近3s内精度最高的一次定位结果
        locationOption.setOnceLocationLatest(true);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(locationOption);
        //启动定位
        mLocationClient.startLocation();



    }

    public void destroy() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
            mLocationClient = null;
        }
        if (mMyLocationListener != null) {
            mMyLocationListener = null;
        }
    }

    /**
     * 高德地图定位的监听回调
     */
    private AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            mCurrentAltitude = aMapLocation.getAltitude();
            mCurrentLatlng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            if (mMyLocationListener != null) {
                mMyLocationListener.onLocationChanged(mCurrentLatlng, aMapLocation.getAccuracy());
            }
        }
    };

    public interface MyLocationChangeListener {
        //经纬度和精度
        void onLocationChanged(LatLng latLng, float accuracy);
    }
}
