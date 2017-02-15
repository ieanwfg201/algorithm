package com.hbhs.algorithm.classify.neuralnet;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by walter.xu on 2017/2/10.
 */
public class RBFNeuralNetClassifierTest {

    @Test
    public void testAll(){
        RBFNeuralNetClassifier test = new RBFNeuralNetClassifier(true);
        List<Iris> sampleList = loadData("train");
        test.train(sampleList, 10);
    }

    private static class Iris implements RBFNeuralNetClassifier.RBFData{
        private float[] attributes;
        private String type;
        public Iris(String type, float[] attributes){
            this.type =type;this.attributes = attributes;
        }
        @Override
        public float[] attributes() {
            return attributes;
        }

        @Override
        public String type() {
            return type;
        }
    }

    private List<Iris> loadData(String type){
        List<Iris> list = new ArrayList<>();
        String path = "iris/"+type+".txt";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(BPNeuralNetClassifierTest.class.getClassLoader().getResourceAsStream(path)));
            String line = reader.readLine();
            while(line!=null){
                String[] args = line.trim().split(",");
                if (args.length==5){
                    try {
                        float[] attributes = new float[4];
                        attributes[0] = Float.valueOf(args[0]);
                        attributes[1] = Float.valueOf(args[1]);
                        attributes[2] = Float.valueOf(args[2]);
                        attributes[3] = Float.valueOf(args[3]);
                        list.add(new Iris(args[4], attributes));
                    }catch (Exception e){

                    }

                }
                line = reader.readLine();
            }
        }catch (Exception e){
            if (reader!=null)try{reader.close();}catch (Exception e1){}
        }
        return list;
    }
}