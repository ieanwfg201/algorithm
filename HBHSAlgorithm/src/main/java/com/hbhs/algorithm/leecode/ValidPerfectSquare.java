package com.hbhs.algorithm.leecode;

/**
 * <B>Valid Perfect Square</B>
 * <BR>Given a positive integer num, write a function which returns True if num is a perfect square else False.
 * <BR>Note: Do not use any built-in library function such as sqrt.
 * <BR>Example 1: Input: 16   Returns: True
 * <BR>Example 2: Input: 14   Returns: False
 */
public class ValidPerfectSquare {

    /**
     * 使用二分法来查询是否某一个范围，并校验
     * @param num
     * @return
     */
    public boolean isPerfectSquare(int num){
        if (num<=0) return false;
        if (num==1) return true;
        int start = 1, end = num;
        int mid = ((end-start)>>1)+start;

        int total = 0;
        while(true){
            if (mid==start||mid==end) return false;
            total =  mid*mid;
            if (total <0)
                end = mid;
            else if (total<num)
                start = mid;
            else if(total > num)
                end = mid;
            else
                return true;

            mid = ((end-start)>>1)+start;
        }
    }
}
