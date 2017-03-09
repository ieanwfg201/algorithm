package com.hbhs.algorithm.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by walter.xu on 2017/3/3.
 */
public class BucketSortTest {

    public static void main(String[] args){
        // 随机初始化20个数
        List<Integer> array = new ArrayList<>();
        for(int i=0;i<20;i++){
            int data = (int)(Math.random()*100);
//            while(data<10){
//                data = (int)(Math.random()*100);
//            }
            array.add(data);
        }
        System.out.println(Arrays.toString(array.toArray()));
        BucketSort.sort(array);
        System.out.println(Arrays.toString(array.toArray()));
    }
}