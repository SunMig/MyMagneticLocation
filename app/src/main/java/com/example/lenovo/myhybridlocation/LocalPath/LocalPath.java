package com.example.lenovo.myhybridlocation.LocalPath;

import android.os.Environment;

import java.io.File;
import java.io.FileReader;

/**
 * Created by Lenovo on 2018/11/3.
 */

public class LocalPath {
    public static String SystemDirPath= Environment.getExternalStorageDirectory().toString();
    public static String FirstLocalPath=SystemDirPath+File.separator+"CetcTest";
    public static String SecondLocalPath=FirstLocalPath+File.separator+"RadioMap";
    public static String wifi_mac_txt=SecondLocalPath+File.separator+"wifi_mac.txt";
    public static String wifi_RM=SecondLocalPath+File.separator+"wifi_RM.txt";
    public static String magnetic_data=SecondLocalPath+File.separator+"magnetic_data.txt";
}
