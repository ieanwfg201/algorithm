package com.hbhs.algorithm.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class OpenFile {
    private static Map<String, String> fileSuffixCommandMap = new HashMap<String, String>();

    /**
     * Only support image and txt files
     * @param file file
     * @throws Exception
     */
    public static void openFile(File file) throws Exception{
        init();
        if (file==null||!file.exists()||file.isDirectory()) return ;
        String path = file.getAbsolutePath();
        String suffix = "txt";
        int index = path.lastIndexOf(".");
        if (index>0) suffix = path.substring(index+1);
        String command = fileSuffixCommandMap.get(suffix)+" "+file.getAbsolutePath();
        System.out.println(command);
        Runtime.getRuntime().exec(command);
    }

    /**
     * Only support image and txt files
     * @param filePath file path
     * @throws Exception
     */
    public static void openFile(String filePath) throws Exception{
        openFile(new File(filePath));
    }
    private static void init(){
        if (fileSuffixCommandMap.size()==0){
            fileSuffixCommandMap.put("jpg","rundll32.exe shimgvw.dll,ImageView_Fullscreen");
            fileSuffixCommandMap.put("jpeg","rundll32.exe shimgvw.dll,ImageView_Fullscreen");
            fileSuffixCommandMap.put("gif","rundll32.exe shimgvw.dll,ImageView_Fullscreen");
            fileSuffixCommandMap.put("png","rundll32.exe shimgvw.dll,ImageView_Fullscreen");
            fileSuffixCommandMap.put("bmp","rundll32.exe shimgvw.dll,ImageView_Fullscreen");
            fileSuffixCommandMap.put("txt","notepad");
        }
    }
}
