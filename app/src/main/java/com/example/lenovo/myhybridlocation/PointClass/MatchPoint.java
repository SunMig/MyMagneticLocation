package com.example.lenovo.myhybridlocation.PointClass;

/**
 * Created by Lenovo on 2018/11/3.
 */

public class MatchPoint {
    public double x;
    public double y;

    public MatchPoint() {
    }

    public MatchPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public MatchPoint(int x, int y){
        this.x=(double)x;
        this.y=(double)y;
    }
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }


}
