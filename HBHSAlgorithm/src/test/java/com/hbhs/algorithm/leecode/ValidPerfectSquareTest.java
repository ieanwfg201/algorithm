package com.hbhs.algorithm.leecode;


import org.junit.Test;

/**
 * Created by walter.xu on 2016/7/5.
 */
public class ValidPerfectSquareTest {

    @Test
    public void testIsPerfectSquare() throws Exception {
        ValidPerfectSquare test = new ValidPerfectSquare();
        int target = 16;
        boolean isSuccess = false;

        isSuccess = test.isPerfectSquare(target);
        System.out.println("number:"+target+", isPerfectSquare: "+isSuccess);

        target = 2147483647;
        isSuccess = test.isPerfectSquare(target);
        System.out.println("number:"+target+", isPerfectSquare: "+isSuccess);
    }
}