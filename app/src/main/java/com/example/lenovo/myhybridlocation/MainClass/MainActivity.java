package com.example.lenovo.myhybridlocation.MainClass;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.lenovo.myhybridlocation.LocalPath.LocalPath;
import com.example.lenovo.myhybridlocation.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";
    private String[] need_permission;
    private TextView tv1;
    private SensorManager sensorManager;
    private WifiManager wifiManager;
    private Sensor msensor;
    private double mx,my;
    private String flr="";
    private String RP= LocalPath.SecondLocalPath;
    HybridLocationClass hybridLocationClass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //申请权限
        requestApplicationPermission();
        initView();
        wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //开启wifi
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
            Log.i(TAG,"WIFI 打开 ");
        }
        hybridLocationClass=new HybridLocationClass(MainActivity.this,wifiManager,sensorManager,RP);
        hybridLocationClass.setHybridLocationListener(new MyHybridLocationListener());
        //11.4，写完接口监听...，还需添加knn匹配方法，以及把匹配结果添加到接口上
        hybridLocationClass.start();
    }

    //获取传感器操作
    protected void onResume() {
        super.onResume();
    }

    private void initView(){
        tv1=(TextView)findViewById(R.id.textview);
    }
    //权限申请方法
    private void requestApplicationPermission() {
        need_permission = new String[]{
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.READ_LOGS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        boolean permission_ok = true;
        for (String permission : need_permission) {
            if (ContextCompat.checkSelfPermission(this,
                    permission) != PackageManager.PERMISSION_GRANTED) {
                permission_ok = false;
//                mTextView.append(String.valueOf(permission_ok)+"\n");
            }
        }
        if (!permission_ok) {
            ActivityCompat.requestPermissions(this, need_permission, 1);
        }
    }


    /**
     * 实现混合定位的位置接口
     */
    private class MyHybridLocationListener implements HybridLocationListener{
        //添加监听结果
        @Override
        public void onLocation(double B, double L, String floor) {
            mx=B;my=L;flr=floor;
            tv1.setText("x坐标："+mx+" -y坐标："+my+" -楼层："+flr);
        }
    }
}
