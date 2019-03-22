package com.example.lenovo.myhybridlocation.KnnPackage;

/**
 * Created by cumt_bjx on 2018/3/16.
 */

public class DistRank implements Comparable<DistRank>{
    private int Distance_ID=0;
    private double Distance=0d;
    //依据距离进行排序
    @Override
    public int compareTo(DistRank another){
        return getDistance().compareTo(another.getDistance());
    }

    @Override
    public Object clone() throws CloneNotSupportedException{
        return super.clone();
    }

    public int getId(){
        return Distance_ID;
    }
    public void setId(int id){
        Distance_ID=id;
    }

    public Double getDistance(){
        return Distance;
    }

    public void setDistance(double distance){
        Distance=distance;
    }
}
