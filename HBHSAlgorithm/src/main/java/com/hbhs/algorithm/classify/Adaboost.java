package com.hbhs.algorithm.classify;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Created by walter.xu on 2017/1/18.
 * {@link }
 */
public class Adaboost {
    /**
     * 当前指定entity是否属于样本库
     * @param entity 待匹配对象
     * @param sampleList 样本对象
     * @return 1：属于样本库， -1：不属于样本库
     */
    public int isCategory(FeatureGenerator entity, List<FeatureSampleEntity> sampleList){
        return isCategory(entity, sampleList, 10);
    }
    /**
     * 当前指定entity是否属于样本库
     * @param entity 待匹配对象
     * @param sampleList 样本对象
     * @param maxCount 最大迭代次数
     * @return 1：属于样本库， -1：不属于样本库
     */
    public int isCategory(FeatureGenerator entity, List<FeatureSampleEntity> sampleList, Integer maxCount){
        List<Classifier> classifiers = adaboost(sampleList, maxCount);
        return isCategoryByClassifiers(entity, classifiers);
    }
    /**
     * 当前指定entity是否属于样本库
     * @param entity 待匹配对象
     * @param classifierList 弱分类器列表
     * @return 1：属于样本库， -1：不属于样本库
     */
    public int isCategoryByClassifiers(FeatureGenerator entity, List<Classifier> classifierList){
        double value = 0;
        for (Classifier classifier : classifierList) {
            value += classifier.coefficient*classifier.G(entity.featureValue());
        }
        return value>=0?1:-1;
    }

    /**
     * 根据样本库生成弱分类器
     * @param entityList 样本对象
     * @param maxCount 最大迭代次数
     * @return 分类器列表
     */
    public List<Classifier> adaboost(List<FeatureSampleEntity> entityList, int maxCount){
        if (entityList==null||entityList.size()==0) return null;
        List<Classifier> list = new ArrayList<Classifier>();
        // 1. sort by feature value
        Collections.sort(entityList, new Comparator<FeatureSampleEntity>() {
            public int compare(FeatureSampleEntity o1, FeatureSampleEntity o2) {
                return o1.x() > o2.x() ? 1 : -1;
            }
        });
        // step 1. 初始化训练数据的权值分布。每一个训练样本最开始时都被赋予相同的权值：1/N. 按照match/unmatch的值做均值分配
        initWeigth(entityList);
        // step 2. 进行多轮迭代，用m = 1,2, ..., M表示迭代的第多少轮
        int count= 0;
        while(count++<maxCount){
//            print(entityList);
            // 2.1 根据最小误差来计算获得分类器
            Classifier classifier = findClassifierWithMinDeviation(entityList);
            // 2.2 根据获取的分类器重新设置样本权值
            setSamplesWeightByClassifier(classifier, entityList);
            list.add(classifier);
        }
        return list;
    }
    // print data
    protected void print(List<FeatureSampleEntity> entityList){
        DecimalFormat df  = new DecimalFormat("0.00000");
        final StringBuilder weight = new StringBuilder();
        for (FeatureSampleEntity entity : entityList) {
            weight.append(df.format(entity.weight())).append(", ");
        }
        System.out.println(weight);
    }

    /**
     * 依据最小误差率获取对应的分类器
     * @param entityList 样本库
     * @return 最小误差率的分类器
     */
    protected Classifier findClassifierWithMinDeviation(List<FeatureSampleEntity> entityList){
        int minThresholdIndex = 0;
        double minThresholdWeightValue = Double.MAX_VALUE;
        int yLessThanThresholdX = 0;
        int yMoreThanThresholdX = 0;
        int index = 0;
        // 对每一次分类做计算，并获取到分类中误差率最小的
        while(index < entityList.size()){
            double[] weightValues = calcDeviation(entityList, index);
            if (weightValues[0]<=weightValues[1]&&weightValues[0]<minThresholdWeightValue){
                minThresholdIndex = index;
                minThresholdWeightValue = weightValues[0];
                yLessThanThresholdX = 1;
                yMoreThanThresholdX = -1;
            }else if (weightValues[1]<minThresholdWeightValue){
                minThresholdIndex = index;
                minThresholdWeightValue = weightValues[1];
                yLessThanThresholdX = -1;
                yMoreThanThresholdX = 1;
            }
            index++;
        }
        // 通过最小误差率计算该分类器的系数
        double coefficient = 0.5*Math.log((1-minThresholdWeightValue)/minThresholdWeightValue);
        return new Classifier(minThresholdIndex,entityList.get(minThresholdIndex).x(),yLessThanThresholdX,yMoreThanThresholdX,coefficient);
    }

    /**
     * 计算误差率，分为两部分：
     * 根据index将样本库定义为两部分。定义S1为entityList[0, index+1], 不包含index+1，S2为entityList[index+1, maxLength], 不包含maxLength
     * 1. 当S1块定义为true(1)时，S2块定义为false(-1)时，计算误差率(计算该次分类中所有分类错误的)
     * 1. 当S1块定义为false(-1)时，S2块定义为true(1)时，计算误差率
     * @param entityList 样本库
     * @param index 分类序号，[0,index+1]为一类，[index+1,maxLength]为另外一类
     * @return 数组array[0]为
     */
    private double[] calcDeviation(List<FeatureSampleEntity> entityList, int index){
        double deviation1 = 0;         // 当小于等于index为1,大于index为-1时的误差deviation
        double deviation2 = 0;         // 当小于等于index为-1,大于index为1时的误差deviation
        // 迭代index[0,index+1]，不包含index+1
        for (int i=0;i<index+1;i++){
            if (entityList.get(i).y()!=1) deviation1 += entityList.get(i).weight();
            if (entityList.get(i).y()!=-1) deviation2 += entityList.get(i).weight();
        }
        // 迭代index[index+1,entityList.size()]，不包含entityList.size()
        for (int i=index+1;i<entityList.size();i++){
            if (entityList.get(i).y()!=-1) deviation1 += entityList.get(i).weight();
            if (entityList.get(i).y()!=1) deviation2 += entityList.get(i).weight();
        }
        return new double[]{deviation1,deviation2};
    }

    /**
     * 依据新生成的分类器为entity对象重新赋予权值
     * @param classifier 指定分类器
     * @param entityList 对象列表
     */
    protected void setSamplesWeightByClassifier(Classifier classifier, List<FeatureSampleEntity> entityList){
        double totalWeight = 0;
        int index = 0;
        for(FeatureSampleEntity entity: entityList){
            entity.setWeigth(entity.weight()*Math.exp(-classifier.coefficient*entity.y()*classifier.G(index))); //TODO
            totalWeight += entity.weight();
            index++;
        }
        for (int i=0;i<entityList.size();i++){
            entityList.get(i).setWeigth(entityList.get(i).weight()/totalWeight);
        }
    }

    /**
     * Init weight of feature sample entity
     * @param entityList sample entity
     */
    private void initWeigth(List<FeatureSampleEntity> entityList){
        double weigth = 1.0/entityList.size();
        for(FeatureSampleEntity entity: entityList){
            entity.setWeigth(weigth);
        }
    }

    public interface FeatureGenerator{ double featureValue(); int y();}
    public static class FeatureSampleEntity<T extends FeatureGenerator>{
        private T entity;
        private double weigth;
        public FeatureSampleEntity(T t){
            this.entity = t;
        }
        public double x(){return entity.featureValue();}
        public int y(){return entity.y();}
        public double weight(){return weigth;}
        public void setWeigth(double newWeigth){this.weigth = newWeigth;}
    }
    // 分类器
    public static class Classifier {
        private int thresholdIndex = 0;                // 阈值-样本序号
        private double thresholdX = 0;                    // 阈值-特征值
        private int yLessThanThresholdX = 0;              // 小于阈值的value，可为-1/1
        private int yMoreThanThresholdX = 0;              // 小于阈值的value，可为-1/1
        private double coefficient;
        public Classifier(int thresholdIndex, double thresholdX,int yLessThanThresholdX,int yMoreThanThresholdX, double coefficient){
            this.thresholdIndex = thresholdIndex;this.thresholdX = thresholdX;
            this.yLessThanThresholdX = yLessThanThresholdX; this.yMoreThanThresholdX = yMoreThanThresholdX;
            this.coefficient =coefficient;
        }

        /**
         * Gx(x)函数，用于计算某个sample是否满足该函数
         * @param sampleIndex 样本index
         * @return 该样本在该分类器函数下是否执行准确。1：准确，-1：不准确
         */
        public int G(int sampleIndex){
            // 如果该函数为小于阈值时匹配(y为1).
            if (yLessThanThresholdX==1){
                return sampleIndex<=thresholdIndex?1:-1;
            }else{
                // 如果该函数为小于阈值时不匹配(y为-1).
                return sampleIndex<=thresholdIndex?-1:1;
            }
        }
        /**
         * Gx(x)函数，用于计算某个sample是否满足该函数
         * @param featureValue 目标feature value
         * @return 该样本在该分类器函数下是否执行准确。1：准确，-1：不准确
         */
        public int G(double featureValue){
            // 如果该函数为小于阈值时匹配(y为1).
            if (yLessThanThresholdX==1){
                return featureValue<=thresholdX?1:-1;
            }else{
                // 如果该函数为小于阈值时不匹配(y为-1).
                return featureValue<=thresholdX?-1:1;
            }
        }

        public double getThresholdX() {
            return thresholdX;
        }

        public void setThresholdX(double thresholdX) {
            this.thresholdX = thresholdX;
        }

        public int getyLessThanThresholdX() {
            return yLessThanThresholdX;
        }

        public void setyLessThanThresholdX(int yLessThanThresholdX) {
            this.yLessThanThresholdX = yLessThanThresholdX;
        }

        public int getyMoreThanThresholdX() {
            return yMoreThanThresholdX;
        }

        public void setyMoreThanThresholdX(int yMoreThanThresholdX) {
            this.yMoreThanThresholdX = yMoreThanThresholdX;
        }

        public int getThresholdIndex() {
            return thresholdIndex;
        }

        public void setThresholdIndex(int thresholdIndex) {
            this.thresholdIndex = thresholdIndex;
        }
        public String toString(){
            return coefficient+"G(x): <=["+thresholdIndex+","+thresholdX+"]"+ yLessThanThresholdX+".  >["+thresholdIndex+","+thresholdX+"]"+ yLessThanThresholdX;
        }
    }

}
