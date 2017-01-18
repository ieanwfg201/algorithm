package com.hbhs.algorithm.string;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by walter.xu on 2016/8/30.
 */
public class LongestCommonSubsquenceTest {

    @Test
    public void testLcs() throws Exception {
        String source1 = createRadomStr(10);
        String source2 = createRadomStr(8);

        String result = LongestCommonSubsquence.lcs(source1, source2);

        System.out.println("arg0:"+source1+", arg1:"+source2+", longest common sub sequence:"+result);

//        System.out.println(LongestCommonSubsquence.formatLength("1"));
//        System.out.println(LongestCommonSubsquence.formatLength("1dfdf"));
//        System.out.println(LongestCommonSubsquence.formatLength("1ss"));
    }

    private String createRadomStr(int length){
        String seed = "abcdefghijklmn";
        StringBuilder str = new StringBuilder();
        while (length-- >0){
            str.append(seed.charAt((int)(Math.random()*seed.length())));
        }
        return str.toString();
    }
}