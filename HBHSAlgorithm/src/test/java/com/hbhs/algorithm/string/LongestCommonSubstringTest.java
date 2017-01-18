package com.hbhs.algorithm.string;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by walter.xu on 2016/8/30.
 */
public class LongestCommonSubstringTest {

    @Test
    public void testLcs() throws Exception {
        String source1 = createRadomStr(15);
        String source2 = createRadomStr(14);

        List<String> list = LongestCommonSubstring.lcs(source1, source2);
        System.out.println(Arrays.toString(list.toArray()));
    }

    private String createRadomStr(int length){
        String seed = "abcdefgh";
        StringBuilder str = new StringBuilder();
        while (length-- >0){
            str.append(seed.charAt((int)(Math.random()*seed.length())));
        }
        return str.toString();
    }
}