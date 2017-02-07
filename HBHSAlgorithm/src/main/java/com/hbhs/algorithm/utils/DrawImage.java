package com.hbhs.algorithm.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

/**
 * Created by walter.xu on 2017/2/6.
 */
public final class DrawImage {
    private static final String IMAGE_TYPE = "jpg";
    private static final int FONT_SIZE = 30;
    private static final int X_precision = 3000;
    private static final int Y_precision = 3000;
    private static final int X_MAX_POINT_COUNT = 30;
    private static final int Y_MAX_POINT_COUNT = 30;
    private static DrawImage image = new DrawImage();
    private DrawImage(){

    }
    public static DrawImage instance(){return image;}

    public void drawImage(DrawImageFunction function){drawImage(function, 100, 100);}
    public void drawImage(DrawImageFunction function, int xMax, int yMax){
        xMax = Math.abs(xMax); yMax = Math.abs(yMax);
        drawImage(function, -xMax, xMax, -yMax, yMax);
    }

    public void drawImage(DrawImageFunction function, int xMin, int xMax, int yMin, int yMax){
        BufferedImage image = new BufferedImage(X_precision, Y_precision, BufferedImage.TYPE_INT_RGB);
//        image.
        Graphics2D graphi = (Graphics2D)image.getGraphics();
        graphi.setBackground(Color.WHITE);
        graphi.clearRect(0, 0, X_precision, Y_precision);//通过使用当前绘图表面的背景色进行填充来清除指定的矩形。
        graphi.setPaint(Color.BLACK);//设置画笔,设置Paint属性
        graphi.setFont(new Font("宋体", Font.BOLD, FONT_SIZE));
        this.drawZeroOrMiddlePosition(graphi, xMin, xMax, yMin, yMax);
        //TODO
        drawFunction(graphi, function, xMin,xMax,yMax);
        showImage(image);
        graphi.dispose();
    }

    private void drawFunction(Graphics2D graphi, DrawImageFunction function, int xMin, int xMax, int yMax){
        int x = 0;
        while(x++<X_precision){
            double orginalX = getOrignalPosition(x,X_precision, xMax - xMin) + xMin;
            double orignalY = yMax - function.function(orginalX);
            double imageY = getImagePosition(orignalY, X_precision, xMax - xMin);
            graphi.drawOval(x, (int)imageY, 1, 1);
        }

    }

    private void showImage(BufferedImage image){
        String path = System.getProperty("java.io.tmpdir");
        path += File.separator+new Date().getTime()+"-"+(int)(Math.random()*1000)+"."+IMAGE_TYPE;
        try {
            ImageIO.write(image, IMAGE_TYPE, new File(path));
            OpenFile.openFile(path);
        }catch (Exception e){}

    }

    private void drawZeroOrMiddlePosition(Graphics graphi, int xMin, int xMax, int yMin, int yMax){
        // draw x line
        int zeroOrMiddleY = getZeroOrMiddlePositionByPrecision(yMin, yMax, Y_precision);
        graphi.drawLine(0, zeroOrMiddleY, X_precision, zeroOrMiddleY);
        // draw x point
        /*int xDiff = X_precision/X_MAX_POINT_COUNT;
        int start = xDiff;
        while(start<X_precision){
            graphi.drawString((int)getOrignalPosition(start,X_precision,xMax-xMin)+"", start, zeroOrMiddleY);
            start += xDiff;
        }*/
        // draw y line
        int zeroOrMiddleX = getZeroOrMiddlePositionByPrecision(yMin, yMax, X_precision);
        graphi.drawLine(zeroOrMiddleX, 0, zeroOrMiddleX, Y_precision);
//        graphi.drawString("("+(xMin<0&&xMax>0?0:(xMin+xMax)/2)+", "+
//                (yMin<0&&yMax>0?0:(yMin+yMax)/2)+")", zeroOrMiddleX, zeroOrMiddleY);



    }

    private int getZeroOrMiddlePositionByPrecision(int min, int max, int precision){
        int orginalPostion = min<0&&max>0?min:(min+max)/2;
        return Math.abs(orginalPostion) * X_precision / (max-min);
    }
    public double getOrignalPosition(int imageX, int precision, int orignalLength){
        return 1.0*imageX*orignalLength/precision;
    }
    public double getImagePosition(double orignalX, int precision, int orignalLength){
        return orignalX*precision/orignalLength;
    }
    public interface DrawImageFunction{
        double function(double x);
    }
}
