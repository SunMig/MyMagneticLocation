package com.example.lenovo.myhybridlocation.MainClass;

import android.print.PrinterId;
import android.util.Log;

import com.example.lenovo.myhybridlocation.PointClass.FingerPoint;
import com.example.lenovo.myhybridlocation.PointClass.MatchPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MagneticThread extends Thread {
    private final String TAG="MagneticThread";
    private List<Float> testList=new ArrayList<Float>();
    private List<Float> testlist=new ArrayList<Float>();
    private List<FingerPoint> fingerList=new ArrayList<FingerPoint>();
    private double[] FingerArray;
    private float[] TestArray;
    private String fingertext;
    private MagneticLocListener magneticLocListener;
    private double result_x,result_y;
    private boolean MagIsRun=false,IsRun=true;
    private float Azimuth=0f,sum_deta_azimuth=0f,Azimuth_for=0f,Azimuth_current=0f;
    private List<Float> Azimuth_list=new ArrayList<Float>();
    //构造函数
    public MagneticThread(String string) {
        this.fingertext=string;
        init();
    }
    //初始化操作
    private void init() {
        //读取文件
        ReadFinger(fingertext);
        //集合转数组
        FingerArray=listToArray_1(fingerList);
        Log.d(TAG,""+FingerArray.length);
        MagIsRun=true;
    }
    //测试数据转数组
    private float[] listToArray(List list){
        int length=list.size();
        float[] testarray=new float[length];
        for(int i=0;i<length;i++){
            testarray[i]=(float) list.get(i);
        }
        return testarray;
    }
    //指纹数据转数组
    private double[] listToArray_1(List<FingerPoint> list){
        int length=list.size();
        double[] testarray_1=new double[length];
        for(int i=0;i<length;i++){
            FingerPoint fpt=list.get(i);
            testarray_1[i]=fpt.getB();
        }
        return testarray_1;
    }
    private void ReadFinger(String string){
        File file=new File(string);
        String line="";
        try {
            FileReader fr=new FileReader(file);
            BufferedReader br=new BufferedReader(fr);
            while((line=br.readLine())!=null){
                FingerPoint fp=new FingerPoint();
                MatchPoint mp=new MatchPoint();
                line=line.trim();
                String[] arrayString=line.split("\t");
                fp.setB(Float.parseFloat(arrayString[0]));
                mp.x=Double.parseDouble(arrayString[1]);
                mp.y=Double.parseDouble(arrayString[2]);
                fp.setPointCoor(mp.x,mp.y);
                fingerList.add(fp);
            }
            fr.close();
            br.close();
            Log.d(TAG,"地磁数据读取完毕...");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG,"没找到文件...");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"读取文件错误...");
        }
    }

    //主方法，后续需要验证算法的java版与matlab版
    @Override
    public void run() {
        super.run();
        Log.d(TAG,"is running...");
        while(MagIsRun){
// 通过方位角变化增加量判断是否发生了转向，由于变化的过程短暂，判断也比较难
// 判断是否发生了转向

            //为啥提交不了
            float sum_deta=sum_deta_azimuth;
            if (sum_deta > 175 && sum_deta < 200 &&IsRun) {
                Log.d(TAG, "sum_deta_azimuth is: " + sum_deta_azimuth);
                Collections.reverse(fingerList);
                FingerArray = listToArray_1(fingerList);
                IsRun=false;
            }
            if (testlist.size() == 100) {
                TestArray = listToArray(testlist);
                //计算各段的dtw距离
                Log.d(TAG, "TestArray " + TestArray.length);
                List dtwlist = new ArrayList();//用于存放一次计算的DTW距离
                List dtwlist_1 = new ArrayList();//用于复制
                //判断方向
                for (int i = 0; i + 100 < FingerArray.length; i += 10) {
                    double[] seg_finger = Arrays.copyOfRange(FingerArray, i, i + 100);
                    double d = dtw(seg_finger, TestArray);
                    dtwlist.add(d);
                }
                dtwlist_1.addAll(dtwlist);
                Collections.sort(dtwlist_1);
                int count = 0;//这里的count与实际的索引有10倍的关系，因为上述步长是10
                for (int i = 0; i < dtwlist.size(); i++) {
                    if (dtwlist_1.get(0).equals(dtwlist.get(i))) {
                        count = i;
                    }
                }
                //最小距离索引值
                count = count * 10;
                int iter;
                //加入坐标索引太小，取前后一秒的数据就会出错，因为iter-20会小于零
                if (count < 20 || count == 0) {
                    iter = 0;
                } else {
                    //count>0时，找到下标之后，取坐标，取最小值点的前后1秒
                    iter = count - 20;
                }
                //取前后一秒的坐标值平均值
                double sum_x = 0;
                double sum_y = 0;
                for (int i = iter; i < count + 20; i++) {
                    FingerPoint fps = fingerList.get(i);
                    sum_x = sum_x + fps.getMPoint().x;
                    sum_y = sum_y + fps.getMPoint().y;
                }
                //坐标数据放在接口上
                result_x = sum_x / 40;
                result_y = sum_y / 40;
                magneticLocListener.onLocation(result_x, result_y);
                Log.d(TAG, "X IS " + result_x);
                //线程休眠一秒
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //接收数据传感器的数据
    public void ListReceive(List list){
        this.testList=new CopyOnWriteArrayList<>(list);
        testlist=new CopyOnWriteArrayList<>(testList);
        //Log.d(TAG,""+testlist.size());
    }
    //接收方位角数据
    public void Azimuth_receive(float azimuth){
        this.Azimuth=azimuth;
        Azimuth_list.add(0,Azimuth);
        if(Azimuth_list.size()>=5){
            Azimuth_list.remove(0);
            Azimuth_list.add(4,Azimuth);
            Log.d(TAG,"Azimuth is "+Azimuth);
            Azimuth_for= (float) Azimuth_list.get(0);
            Azimuth_current=Azimuth_list.get(4);
            sum_deta_azimuth=Azimuth_current-Azimuth_for;
            //Log.d(TAG,"sum_deta_azimuth is: "+sum_deta_azimuth);

        }
    }
    //dtw算法过程
    public double dtw(double a[],float b[]){
        int num1=a.length;
        int num2=b.length;
        double[][] SumDistance=new double[num1][num2];
        double[][] dtw=new double[num1][num2];
        //先初始化累积矩阵SumDistance
        for(int i=0;i<num1;i++){
            for(int j=0;j<num2;j++){
                SumDistance[i][j]=Math.sqrt((b[j]-a[i])*(b[j]-a[i]));
            }
        }
        //初始化dtw矩阵，即损耗矩阵
        dtw[0][0]=SumDistance[0][0];
        for(int i=1;i<num1;i++){
            for(int j=0;j<num2;j++){
                //临界条件判断
                if(i>0&&j>0){
                    dtw[i][j]=minDist(dtw[i][j-1]+SumDistance[i][j],dtw[i-1][j]+SumDistance[i][j],dtw[i-1][j-1]+2*SumDistance[i][j]);
                }
                else if(i==0&&j>0){
                    dtw[i][j] = dtw[i][j-1]+SumDistance[i][j];
                }
                else if(i>0&&j==0){
                    dtw[i][j]= dtw[i-1][j]+SumDistance[i][j];
                }else{
                    dtw[i][j]=0;
                }
            }
        }
        //返回dtw矩阵的最后一行最后一列的值
        return dtw[num1-1][num2-1];
    }
    //找最小距离
    private double minDist(double dist1,double dist2,double dist3){
        return(dist1<dist2?(dist2<dist3?dist3:(dist1>dist3?dist3:dist1)):(dist2>dist3?dist3:dist2));
    }
    //监听器
    public void setMagneticLocListener(MagneticLocListener locListener){
        this.magneticLocListener=locListener;
    }

}