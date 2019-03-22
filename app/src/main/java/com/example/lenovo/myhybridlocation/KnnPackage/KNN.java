package com.example.lenovo.myhybridlocation.KnnPackage;

import android.util.Log;

import com.example.lenovo.myhybridlocation.PointClass.FingerPoint;
import com.example.lenovo.myhybridlocation.PointClass.MatchPoint;
import com.example.lenovo.myhybridlocation.jama.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Lenovo on 2018/11/4.
 */

public class KNN {
    private int NearPointNumber;
    public KNN(){
        //默认K是3
        this.NearPointNumber=3;
    }
    //指定K的个数
    public KNN(int number){
        this.NearPointNumber=number;
    }

    //计算方法
    public MatchPoint getMatchResults(List<FingerPoint> fingerPointList,FingerPoint testPoint){
        MatchPoint matchPoint=new MatchPoint();
        double Match_result_x=0.0;
        double Match_result_y=0.0;
        //计算欧式距离
        List<DistRank> distRankList=DistanceCalculte(fingerPointList,testPoint);
        //依据欧式距离提取临近点号
        List<Integer> Number_list=sortDistance(distRankList,NearPointNumber);
        //找到临近点的点号之后，计算各点的权重，反距离加权
        List<Double> weight_list=getWeight(distRankList,NearPointNumber);
        List<MatchPoint> nearPointCoorList = getNeighborPoint(fingerPointList, Number_list);
        //计算匹配结果的坐标
        for(int i=0;i<nearPointCoorList.size();i++){
            Match_result_x=nearPointCoorList.get(i).x*weight_list.get(i);
            Match_result_y=nearPointCoorList.get(i).y*weight_list.get(i);
        }
        //加权计算的结果赋值
        matchPoint.x=Match_result_x;
        matchPoint.y=Match_result_y;
        //返回计算结果
        return matchPoint;
    }
    //根据点号提取对应点
    private List<MatchPoint> getNeighborPoint(List<FingerPoint> fingerPointList, List<Integer> number_list) {
        List<MatchPoint> NeighPointList=new ArrayList<>();
        FingerPoint fp;
        for(int i=0;i<number_list.size();i++){
            fp=new FingerPoint();
            int Number_ID=number_list.get(i);
            for(int j=0;j<fingerPointList.size();j++){
                if(Number_ID==fingerPointList.get(j).getId()){
                    fp=fingerPointList.get(j);
                    MatchPoint mp=fp.getMPoint();
                    NeighPointList.add(mp);
                }
            }
        }
        return NeighPointList;
    }

    //计算权重的方法
    private List<Double> getWeight(List<DistRank> distRankList, int nearPointNumber) {
        List<Double> weightlist=new ArrayList<>();
        //生成一个距离数组
        DistRank[] distrank_1=new DistRank[distRankList.size()];
        distRankList.toArray(distrank_1);
        Arrays.sort(distrank_1);//升序排序
        double sum_weight=0;
        double weight;
        for(int i=0;i<nearPointNumber;i++){
            double distance=distrank_1[i].getDistance();
            if (Math.abs(distance - 0) <= 0.001) {
                weight = 1000d;
            } else {
                weight = 1 / distance;
            }
            sum_weight+=weight;
            //重写distanceRank
            distrank_1[i].setDistance(weight);
        }
        //计算对应ID的权重
        for(int i=0;i<nearPointNumber;i++){
            weightlist.add(distrank_1[i].getDistance()/sum_weight);
        }

        return weightlist;
    }

    //提取最临近点的点号
    private List<Integer> sortDistance(List<DistRank> distRankList, int nearPointNumber) {
        List<Integer> list_number=new ArrayList<>();//存放临近点的点号
        DistRank[] dist_rank=new DistRank[distRankList.size()];
        distRankList.toArray(dist_rank);//集合转数组
        Arrays.sort(dist_rank);//升序拍排列
        Log.d("点号索引是: ",dist_rank[0].getId()+" "+dist_rank[1].getId()+" "+dist_rank[2].getId());
        //循环得到最临近点的点号
        for(int i=0;i<nearPointNumber;i++){
            list_number.add(dist_rank[i].getId());
        }
        return list_number;
    }

    //计算指纹点与参考点的距离，存入list
    private List<DistRank> DistanceCalculte(List<FingerPoint> fingerPointList, FingerPoint testPoint) {
        //先判断下条件，可有可无~
        if(fingerPointList==null||testPoint==null){
            return null;
        }
        List<DistRank> DR_list=new ArrayList<>();//用于存储距离的list
        Matrix  test_matrix=testPoint.getPointSingalAttrMat();
        //循环求出指纹点与参考点的欧式距离
        for(int i=0;i<fingerPointList.size();i++){
            FingerPoint fc=fingerPointList.get(i);
            Matrix figer_matrix=fc.getPointSingalAttrMat();
            double distance=0d;
            //求信号强度的差值
            Matrix Mins_matrix=figer_matrix.minus(test_matrix);
            //求信号差值的平方和
            int rows,colums;
            double sum_distance=0;
            Matrix matrix=(Mins_matrix.transpose()).arrayTimes(Mins_matrix.transpose());
            rows=matrix.getRowDimension();
            colums=matrix.getColumnDimension();
            //求平方和
            for(int j=0;j<rows;j++){
                for(int k=0;k<colums;k++){
                    double c=matrix.get(j,k);
                    sum_distance+=c;
                }
            }
            double sqrt_of_sum=Math.sqrt(sum_distance);
            DistRank distrank=new DistRank();
            distrank.setDistance(sqrt_of_sum);
            distrank.setId((int) fc.getId());
            DR_list.add(distrank);
        }
        //返回计算结果
        return DR_list;
    }
}
