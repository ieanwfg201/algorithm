package com.hbhs.algorithm.classify;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by walter.xu on 2017/1/22.
 */
public class DecisionTreeC45Test {

    List<String[]> quatoValuesList = new ArrayList<String[]>();
    List<Boolean> resultList = new ArrayList<Boolean>();

    private void init(){
        addLine("Sunny,Hot,High,Weak,No");
        addLine("Sunny,Hot,High,Strong,No");
        addLine("Overcast,Hot,High,Weak,Yes");
        addLine("Rain,Mild,High,Weak,Yes");
        addLine("Rain,Cool,Normal,Weak,Yes");
        addLine("Rain,Cool,Normal,Strong,No");
        addLine("Overcast,Cool,Normal,Strong,Yes");
        addLine("Sunny,Mild,High,Weak,No");
        addLine("Sunny,Cool,Normal,Weak,Yes");
        addLine("Rain,Mild,Normal,Weak,Yes");
        addLine("Sunny,Mild,Normal,Strong,Yes");
        addLine("Overcast,Mild,High,Strong,Yes");
        addLine("Overcast,Hot,Normal,Weak,Yes");
        addLine("Rain,Mild,High,Strong,No");
    }
    private void addLine(String line){
        String[] arrays = line.substring(0,line.lastIndexOf(",")).split(",");
        boolean ifTrue = line.toLowerCase().endsWith("yes");
        quatoValuesList.add(arrays); resultList.add(ifTrue);
//        System.out.println(Arrays.toString(arrays)+": "+ifTrue);
    }

    @Test
    public void testGenerateDecisionTree() throws Exception{
        init();
        DecisionTreeC45 test = new DecisionTreeC45();
        test.DEBUG = true;

        DecisionTreeC45.DecisionTreeNode tree = test.generateDecisionTree(quatoValuesList, resultList);
        System.out.println(tree);
    }

    @Test
    public void testGenerateDecitionTree() throws Exception {
        init();
        DecisionTreeC45 test = new DecisionTreeC45();
        test.DEBUG = true;
        List<Integer> availableQuatoIndexList = new ArrayList<Integer>();
        availableQuatoIndexList.add(0);
        availableQuatoIndexList.add(1);
        availableQuatoIndexList.add(2);
        availableQuatoIndexList.add(3);
        List<Integer> availableValueIndexList = new ArrayList<Integer>();
        availableValueIndexList.add(0);
        availableValueIndexList.add(1);
        availableValueIndexList.add(2);
        availableValueIndexList.add(3);
        availableValueIndexList.add(4);
        availableValueIndexList.add(5);
        availableValueIndexList.add(6);
        availableValueIndexList.add(7);
        availableValueIndexList.add(8);
        availableValueIndexList.add(9);
        availableValueIndexList.add(10);
        availableValueIndexList.add(11);
        availableValueIndexList.add(12);
        availableValueIndexList.add(13);
        double topEntropy = test.generateTopEntropy(resultList, availableValueIndexList);
        test.calculateAndSelectQuotaByMinEntropy(quatoValuesList, resultList, availableQuatoIndexList, availableValueIndexList,topEntropy);
    }

    @Test
    public void testGenerateDecisionTree1() throws Exception{
        init();
        DecisionTreeC45<Student> test = new DecisionTreeC45<Student>();
        List<Student> students = new ArrayList<Student>();
        for (int i = 0; i<10;i++){
            students.add(new Student(i));
        }
        test.generateDecisionTree(students);
        test.DEBUG = true;
    }
    private static class Student{
        @DecisionTreeC45QuotaField
        private String line1;
        @DecisionTreeC45QuotaField
        private String line2;
        @DecisionTreeC45QuotaField
        private String line3;
        @DecisionTreeC45QuotaField
        private String line4;
        private String line5;
        @DecisionTreeC45QuotaResult
        private String result1;
        private Boolean result2;

        public Student(int index){
            this.line1 = "line1_"+index;
            this.line2 = "line2_"+index;
            this.line3 = "line3_"+index;
            this.line4 = "line4_"+index;
            this.line5 = "line5_"+index;
            this.result1 = "result1_"+index;
            this.result2 = index%2==0;



        }

        public String getLine1() {
            return line1;
        }

        public void setLine1(String line1) {
            this.line1 = line1;
        }

        public String getLine2() {
            return line2;
        }

        public void setLine2(String line2) {
            this.line2 = line2;
        }

        public String getLine3() {
            return line3;
        }

        public void setLine3(String line3) {
            this.line3 = line3;
        }

        public String getLine4() {
            return line4;
        }

        public void setLine4(String line4) {
            this.line4 = line4;
        }

        public String getLine5() {
            return line5;
        }

        public void setLine5(String line5) {
            this.line5 = line5;
        }

        public String getResult1() {
            return result1;
        }

        public void setResult1(String result1) {
            this.result1 = result1;
        }

        public Boolean getResult2() {
            return result2;
        }

        public void setResult2(Boolean result2) {
            this.result2 = result2;
        }
    }
}