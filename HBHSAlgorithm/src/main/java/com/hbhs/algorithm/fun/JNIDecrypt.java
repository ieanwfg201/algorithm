package com.hbhs.algorithm.fun;

public class JNIDecrypt {

    static {
        System.loadLibrary("test");
    }
    public native String decode(String price, String key1, String key2);
}
