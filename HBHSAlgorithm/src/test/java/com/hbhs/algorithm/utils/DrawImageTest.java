package com.hbhs.algorithm.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by walter.xu on 2017/2/6.
 */
public class DrawImageTest {

    @Test
    public void testDrawImage() throws Exception {
//        DrawImage.instance().drawImage(new Line());

//        testA();


        DrawImage.instance().drawImage(new GaussFunction(1), 10, 10);
    }

    private void testA(){
        System.out.println(DrawImage.instance().getImagePosition(1,100,50));
        System.out.println(DrawImage.instance().getImagePosition(2,100,50));
        System.out.println(DrawImage.instance().getImagePosition(3,100,50));
        System.out.println(DrawImage.instance().getImagePosition(4,100,50));
        System.out.println(DrawImage.instance().getImagePosition(5,100,50));

        System.out.println(DrawImage.instance().getOrignalPosition(1, 100, 50));
        System.out.println(DrawImage.instance().getOrignalPosition(2, 100, 50));
        System.out.println(DrawImage.instance().getOrignalPosition(3, 100, 50));
        System.out.println(DrawImage.instance().getOrignalPosition(4, 100, 50));
        System.out.println(DrawImage.instance().getOrignalPosition(5, 100,50));
    }
    public static class LOG2 implements DrawImage.DrawImageFunction{
        public double function(double x) {
            return Math.log(x)/Math.log(2);
        }
    }

    public static class Line implements DrawImage.DrawImageFunction{
        public double function(double x) {
            return 0.5*x+1;
        }
    }
    public static class Derivative implements DrawImage.DrawImageFunction{
        public double function(double x) {
            return 0;
        }
    }

    public static class GaussFunction implements DrawImage.DrawImageFunction{
        private double exp=1;
        public GaussFunction(double exp){if(exp!=0)this.exp=exp;}
        public double function(double x) {
            return Math.exp(-x*x/(2*exp*exp));
        }
    }

}