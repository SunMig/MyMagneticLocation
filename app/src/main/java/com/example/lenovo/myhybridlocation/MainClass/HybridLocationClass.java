package com.example.lenovo.myhybridlocation.MainClass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;


import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.lenovo.myhybridlocation.WifiThread.WifiLocationListener;
import com.example.lenovo.myhybridlocation.WifiThread.WifiLocationThread;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Lenovo on 2018/11/3.
 */

public class HybridLocationClass {
    private static String TAG="HybridLocation";
    private Context mcontext;
    private WifiManager mwifimanager;
    private String string,wifi_mac,wifi_RM,mag_FP;
    private SensorManager mSensorManager;
    private Sensor msensor;
    private float[] maVal=new float[3];
    private HybridLocationListener hybridLocationListener;
    private double wifiMatchResults_x,wifiMatchResults_y;
    private double magMatchResults_x,magMatchResults_y;
    private List<Float> geoList=new ArrayList<Float>();
    private float B=0f;
    private String z="1";
    private boolean isRunMag=false;
    private MagneticThread magneticThread;
    public HybridLocationClass(Context context, WifiManager mwifimanager,SensorManager sensorManager,String string){
        this.mcontext=context;
        this.mwifimanager=mwifimanager;
        this.mSensorManager=sensorManager;
        this.string=string;
        init();
    }

    private void init() {
        getFilePath();//获取文件路径
        initSensor();
        magneticThread=new MagneticThread(mag_FP);
    }
    public SensorEventListener msensorEventListener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
                maVal=sensorEvent.values.clone();
                B= (float) Math.sqrt(maVal[0]*maVal[0]+maVal[1]*maVal[1]+maVal[2]*maVal[2]);
                if(geoList.size()<100){
                    geoList.add(B);
                }else{
                    geoList.remove(0);
                    geoList.add(99,B);
                    magneticThread.ListReceive(new CopyOnWriteArrayList(geoList));
                    //Log.d(TAG," "+geoList.size());
                }
                //Log.d(TAG," "+geoList.size());
                hybridLocationListener.onLocation(magMatchResults_x,magMatchResults_y,z);//监听
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    //初始化传感器
    private void initSensor(){
        msensor=mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(msensorEventListener,msensor,CollectTime.COLLECT_NORMAL);
        Log.d(TAG, "initSensor: ");
    }

    //混合定位类启动的方法
    public void start(){

//        //WiFi线程开启
//        WifiLocationThread wifiLocationThread=new WifiLocationThread(mwifimanager,wifi_mac,wifi_RM);
//        //开启定位线程
//        wifiLocationThread.start();
//        wifiLocationThread.setWifiLocationListener(new WifiListener());
        //注册传感器

        isRunMag=true;//地磁线程开启
        magneticThread.start();
        magneticThread.setMagneticLocListener(new MagneticListener());

    }

    //混合定位的监听接口
    public void setHybridLocationListener(HybridLocationListener hybridLocationListener){
        this.hybridLocationListener=hybridLocationListener;
    }
    private class WifiListener implements WifiLocationListener{

        @Override
        public void onLocation(double x, double y, double weight) {
            //添加匹配结果的x,y坐标
            wifiMatchResults_x=x;
            wifiMatchResults_y=y;
            Log.i(TAG,"WIFI的输出结果是："+x+"  "+y);
        }
    }
    //地磁监听器绑定
    private class MagneticListener implements MagneticLocListener{

        @Override
        public void onLocation(double x, double y) {
            magMatchResults_x=x;
            magMatchResults_y=y;
            Log.d(TAG,"magMatchResults_x is "+x);
        }
    }
    //获取文件路径
    private void getFilePath() {
        wifi_mac=string+ File.separator+"wifi_mac.txt";
        wifi_RM=string+File.separator+"wifi_RM.txt";
        mag_FP=string+File.separator+"magnetic_data.txt";//地磁数据
    }
}
