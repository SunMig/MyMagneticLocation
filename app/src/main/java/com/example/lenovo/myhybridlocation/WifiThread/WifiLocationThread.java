package com.example.lenovo.myhybridlocation.WifiThread;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.lenovo.myhybridlocation.KnnPackage.KNN;
import com.example.lenovo.myhybridlocation.PointClass.FingerPoint;
import com.example.lenovo.myhybridlocation.PointClass.MatchPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lenovo on 2018/11/3.
 */

public class WifiLocationThread extends Thread{
    private static final String TAG="WifiLocationThread";
    private ArrayList<String> wifiMAC=new ArrayList<String>();
    private WifiManager wifiManager;
    private String wifi_mac,wifi_RM;
    private double wifi_weight=0d;
    private List<FingerPoint> wifiFingerList=new ArrayList<>();
    private LinkedHashMap<String,Double> scan_wifi_mac=new LinkedHashMap<>();
    private LinkedHashMap<String,Double> wifi_RM_map=new LinkedHashMap<>();
    private String wifi_scan_string="";
    private boolean WifiThreadFlag=false;
    private KNN knn;
    WifiLocationListener wifiLocationListener;
    public WifiLocationThread(WifiManager wifiManager,String wifi_mac,String wifi_RM){
        this.wifiManager=wifiManager;
        this.wifi_mac=wifi_mac;
        this.wifi_RM=wifi_RM;
        WifiThreadFlag=true;
        knn=new KNN();
        init();
    }

    private void init() {
        //完成数据读取的操作
        wifiFingerList=readFingerMacFile(wifi_RM);
        wifi_RM_map=readWifiMacFile(wifi_mac);
        scan_wifi_mac= (LinkedHashMap<String, Double>) wifi_RM_map.clone();
    }
    //读取WiFi的MAC
    private LinkedHashMap<String,Double> readWifiMacFile(String string){
        LinkedHashMap<String,Double> wifi_mac=new LinkedHashMap<>();
        File file=new File(string);
        try {
            FileReader fr=new FileReader(file);
            BufferedReader br=new BufferedReader(fr);
            String macstring;
            while ((macstring=br.readLine())!=null){
                macstring=macstring.trim();
                wifi_mac.put(macstring,-95.0);
                //Log.i(TAG,"读取的wifi字符串是："+macstring);
            }
            fr.close();
            br.close();
            //Log.i(TAG,"wifi的Mac信息写入到LinkedHashMap中");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG,"wifi的mac文件不存在！");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wifi_mac;
    }
    //读取指纹库数据
    private List<FingerPoint> readFingerMacFile(String filepath){
        List<FingerPoint> list=new ArrayList<>();
        File file=new File(filepath);
        try {
            FileReader fr=new FileReader(file);
            BufferedReader br=new BufferedReader(fr);
            String readstring;
            while((readstring=br.readLine())!=null){
                FingerPoint fingerPoint=null;
                MatchPoint macpoint=new MatchPoint();
                readstring=readstring.trim();
                String[] arrString=readstring.split(",");
                int size=arrString.length;
                fingerPoint=new FingerPoint(Long.parseLong(arrString[0]));
                macpoint.x=Double.parseDouble(arrString[1].trim());
                macpoint.y=Double.parseDouble(arrString[2].trim());
                fingerPoint.setPointCoor(macpoint.x,macpoint.y);
                int RMsize=size-3;
                double[][] signal_mat=new double[RMsize][1];
                for(int i=3;i<arrString.length;i++){
                    if(arrString[i]=="0.00"){
                        signal_mat[i-3][0]=-105d;
                    }else{
                        signal_mat[i-3][0]=Double.parseDouble(arrString[i]);
                    }
                }
                fingerPoint.addSignalAttr(signal_mat);
                list.add(fingerPoint);
                Log.i(TAG,"读取的wifi字符串是："+readstring);
            }
            fr.close();
            br.close();
            Log.i(TAG,"wifi的指纹信息读取");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG,"wifi的RM文件不存在！");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    //wifi扫描函数
    private void wifiScanning(){
        wifiManager.startScan();
        //获得扫描结果
        List<ScanResult> list = wifiManager.getScanResults();
        for (int j = 0; j < list.size(); j++) {
            String ap_name = list.get(j).SSID;
            String ap_mac = list.get(j).BSSID;
            wifiMAC.add(ap_mac);
            int ap_level = list.get(j).level;
            //Log.i(TAG,apmac+aplevel);
            //筛选属于指纹库的mac地址的rssi值
            if (scan_wifi_mac.containsKey(ap_mac)) {
                scan_wifi_mac.put(ap_mac, (double) ap_level);
            }
            //Log.i(TAG,scan_wifi_mac.toString());
        }
    }

    @Override
    public void run() {
        super.run();
        while(wifiManager.isWifiEnabled()&&WifiThreadFlag){
            //开始扫描
            wifiScanning();
            FingerPoint testPoint=new FingerPoint();
            //拿到扫描wifi的rssi值
            for(Map.Entry entry:scan_wifi_mac.entrySet()){
                String s1=Double.toString((Double) entry.getValue());
                wifi_scan_string=wifi_scan_string+","+s1;
            }
            wifi_scan_string=wifi_scan_string.substring(1);
            wifi_scan_string=wifi_scan_string.trim();
            String[] wifi_scan_arrays=wifi_scan_string.split(",");
            int size_scan=wifi_scan_arrays.length;
            double[][] wifiscanarrays=new double[size_scan][1];//一定要声明成[size_scan][1],不能是[size_scan][0]，要不然会报空指针错误
            //转化成double数组，size_scan*1的数组
            for(int i=0;i<size_scan;i++){
                wifiscanarrays[i][0]=Double.parseDouble(wifi_scan_arrays[i]);
            }
            //给testpoint添加扫描的数组数据
            testPoint.addSignalAttr(wifiscanarrays);
            //调用KNN方法
            MatchPoint matchPointResults=knn.getMatchResults(wifiFingerList,testPoint);
            //每次都要置空wifi扫描字符串和扫描map
            scan_wifi_mac= (LinkedHashMap<String, Double>) wifi_RM_map.clone();
            wifi_scan_string="";
            //设置匹配结果的x,y坐标
            wifiLocationListener.onLocation(matchPointResults.x,matchPointResults.y,wifi_weight);
        }

    }
    //设置监听器
    public void setWifiLocationListener(WifiLocationListener wifiLocationListener){
        this.wifiLocationListener=wifiLocationListener;
    }
}
