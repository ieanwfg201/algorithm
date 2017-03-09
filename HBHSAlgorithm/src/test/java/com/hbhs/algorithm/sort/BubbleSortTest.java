package com.hbhs.algorithm.sort;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by walter.xu on 2017/3/1.
 */
public class BubbleSortTest {

    public static void main(String[] args){
        Integer[] array = new Integer[]{1,0,9,2,3,8,7,4,5,6};
        System.out.println("原始数据："+Arrays.toString(array));
        BubbleSort.sort(array);
        System.out.println("排序后数据："+Arrays.toString(array));
    }

}