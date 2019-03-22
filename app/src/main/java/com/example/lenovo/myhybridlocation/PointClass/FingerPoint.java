package com.example.lenovo.myhybridlocation.PointClass;

import com.example.lenovo.myhybridlocation.jama.Matrix;

/**
 * Created by Lenovo on 2018/11/3.
 */

public class FingerPoint {
    private long PointId=0l;
    private MatchPoint matchPoint;
    private Matrix matrix;
    private float B=0f;
    public FingerPoint(long id){
        this.PointId=id;
        this.matchPoint=new MatchPoint();
//        this.B=b;
    }
    public void setB(float b){
        this.B=b;
    }
    public float getB() {
        return B;
    }

    public FingerPoint(){
        this.matchPoint=new MatchPoint();
    }
    public long getId(){
        return PointId;
    }
    public void setId(long id){
        PointId=id;
    }
    public void setPointCoor(double x,double y){
        matchPoint.x=x;
        matchPoint.y=y;
    }
    public MatchPoint getMPoint(){
        return matchPoint;
    }
    public Matrix getPointSingalAttrMat(){
        return matrix;
    }
    public void addSignalAttr(double[][] vals){

       matrix=new Matrix(vals);
//        Log.i(TAG,"KNN里组合的信号指纹是："+pointSingalAttrMat.toString());
    }
}
