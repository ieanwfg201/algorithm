package com.hbhs.algorithm.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by walter.xu on 2017/2/6.
 */
public class OpenFileTest {
    String path = "D:/log/test.jpg";
    @Test
    public void testOpenFile() throws Exception {
        OpenFile.openFile(path);
    }
}