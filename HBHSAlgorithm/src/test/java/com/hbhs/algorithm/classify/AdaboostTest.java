package com.hbhs.algorithm.classify;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by walter.xu on 2017/1/19.
 */
public class AdaboostTest {
    static List<Adaboost.FeatureSampleEntity> list = new ArrayList<Adaboost.FeatureSampleEntity>();
    static {
        list.add(new Adaboost.FeatureSampleEntity<IntegerFeatureGenerator>(new IntegerFeatureGenerator(0,true)));
        list.add(new Adaboost.FeatureSampleEntity<IntegerFeatureGenerator>(new IntegerFeatureGenerator(1,true)));
        list.add(new Adaboost.FeatureSampleEntity<IntegerFeatureGenerator>(new IntegerFeatureGenerator(2,true)));
        list.add(new Adaboost.FeatureSampleEntity<IntegerFeatureGenerator>(new IntegerFeatureGenerator(3,false)));
        list.add(new Adaboost.FeatureSampleEntity<IntegerFeatureGenerator>(new IntegerFeatureGenerator(4,false)));
        list.add(new Adaboost.FeatureSampleEntity<IntegerFeatureGenerator>(new IntegerFeatureGenerator(5,false)));
//        list.add(new Adaboost.FeatureSampleEntity<IntegerFeatureGenerator>(new IntegerFeatureGenerator(6,true), true));
//        list.add(new Adaboost.FeatureSampleEntity<IntegerFeatureGenerator>(new IntegerFeatureGenerator(7,true), true));
        list.add(new Adaboost.FeatureSampleEntity<IntegerFeatureGenerator>(new IntegerFeatureGenerator(8,true)));
        list.add(new Adaboost.FeatureSampleEntity<IntegerFeatureGenerator>(new IntegerFeatureGenerator(9,false)));
    }

    @Test
    public void testIsCategory(){
        Adaboost test = new Adaboost();
        List<Adaboost.Classifier> classifiers = test.adaboost(list, 3);
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(0,true), classifiers));
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(1,true), classifiers));
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(2,true), classifiers));
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(3,false), classifiers));
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(4,false), classifiers));
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(5,false), classifiers));
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(6,true), classifiers));
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(7,true), classifiers));
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(8,true), classifiers));
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(9,false), classifiers));
        System.out.println(test.isCategoryByClassifiers(new IntegerFeatureGenerator(10,false), classifiers));
    }

    @Test
    public void testAdaboost() throws Exception {
        System.out.println(0.1*Math.exp(-0.4236*1*-1));
        System.out.println(0.1*Math.exp(-0.4236*1*1));
        Adaboost test = new Adaboost();

        List<Adaboost.Classifier> res =  test.adaboost(list, 10);
        System.out.println(res);
    }

    private static class IntegerFeatureGenerator implements Adaboost.FeatureGenerator{
        private int value;
        private boolean match;
        public IntegerFeatureGenerator(int value,boolean match){this.value = value; this.match = match;}
        public double featureValue() {
            return value;
        }
        public int y(){return match?1:-1;}
    }


    @Test
    public void testGnerator() throws Exception{

    }

    private List<IQFeatureGenerator> generateIOSamples(){
        List<IQFeatureGenerator> list = new ArrayList<IQFeatureGenerator>();
        list.add(new IQFeatureGenerator(1, 1, false));
        list.add(new IQFeatureGenerator(1, 9, true));

        return list;
    }
    // 未成年人特征值
    private static class IQFeatureGenerator implements Adaboost.FeatureGenerator{
        private int xueLi = 0;  // 学历，越高说明IO越高 1:小学, 2: 中学, 3: 高中，4：中专，5：高专，6：大学，7：硕士，8：博士
        private int answers = 0;  // 回答正确题目，越高说明IO越高 ,最大为10
        private boolean match;

        public IQFeatureGenerator(int xueLi, int answers, boolean match){
            this.xueLi = xueLi;this.answers=answers;this.match=match;
        }

        public double featureValue() {
            double total = 0;
            if (xueLi<=3) total = 0.3*xueLi;
            else if (xueLi<=5) total = 0.4*xueLi;
            else if (xueLi<=6) total = 0.6*xueLi;
            else if (xueLi == 7) total = 0.8*xueLi;
            else total = 0.9* xueLi;
            return total + answers*0.9;
        }
        public int y(){
            return match?1:-1;
        }
    }
}