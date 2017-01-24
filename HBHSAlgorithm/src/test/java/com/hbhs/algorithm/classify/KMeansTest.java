package com.hbhs.algorithm.classify;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by walter.xu on 2017/1/24.
 */
public class KMeansTest {


    @Test
    public void testKmeans() throws Exception {
        KMeans.DEBUG = true;
//        List<Point> pointList = randomPoints(10, 0, 10, 0, 10);
        List<Point> pointList = randomPoints();
        KMeans.kmeans(pointList, 3);
    }
    private static class Point implements KMeans.KMeansDimensionGenerator{
        int x;
        int y;
        public Point(int x, int y){this.x = x; this.y = y;}
        public double[] dimensions() {
            return new double[]{x, y};
        }
        public String toString(){ return "["+x+", "+y+"]";}
    }
    private List<Point> randomPoints(int count, int startX, int maxX, int startY, int maxY){
        List<Point> list = new ArrayList<Point>();
        while(count-->0){
            list.add(new Point(startX+(int)(Math.random()*maxX), startY+(int)(Math.random()*maxY)));
        }
        return list;
    }
    private List<Point> randomPoints(){
        List<Point> list = new ArrayList<Point>();
        list.add(new Point(0,0));
        list.add(new Point(2,4));
        list.add(new Point(1,3));
        list.add(new Point(5,4));
        list.add(new Point(8,1));
        list.add(new Point(3,1));
        list.add(new Point(6,6));
        list.add(new Point(8,6));
        list.add(new Point(7,8));
        list.add(new Point(6,5));

        return list;
    }

}