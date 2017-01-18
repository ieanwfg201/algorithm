package com.hbhs.algorithm.leecode;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by walter.xu on 2016/9/9.
 */
public class ContainsDuplicate {
    public static void main(String[] args){
        ContainsDuplicate test = new ContainsDuplicate();
        System.out.println(test.containsDuplicate(new int[]{1}));
        System.out.println(test.containsDuplicate(new int[]{1,2,3,4,5,6,7,8,9,1}));
    }
    public boolean containsDuplicate(int[] nums) {
        if (nums==null||nums.length<1) return true;
        Set<Integer> set = new HashSet<Integer>();
        for(int num: nums){
            if (!set.add(num)) return false;
        }
        return true;
    }
}
