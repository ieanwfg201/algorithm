package com.hbhs.algorithm.classify;

import java.util.*;

/**
 * Created by walter.xu on 2017/1/24.
 * k-means algorithm
 * @author walter.xu
 */
public class KMeans {
    public static boolean DEBUG = false;

    public static <T extends KMeans.KMeansDimensionGenerator> Map<double[], List<T>> kmeans(List<T> sampleList, int k){
        if (sampleList==null||sampleList.size()==0||sampleList.get(0).dimensions()==null||sampleList.get(0).dimensions().length==0) return null;
        List<Node> randomKNode = initKNodes(sampleList, k);
        Map<Integer, List<Integer>> nodeIndexMap = initSamplesToNode(sampleList, randomKNode);

        while(ifNodePositionChanged(sampleList, randomKNode, nodeIndexMap)){
            nodeIndexMap = initSamplesToNode(sampleList, randomKNode);
        }
        return trans(nodeIndexMap, sampleList, randomKNode);
    }

    private static <T extends KMeans.KMeansDimensionGenerator> Map<double[], List<T>> trans(Map<Integer, List<Integer>> sampleIndexAndNodeIndexMap,
                                                                                            List<T> sampleList, List<Node> nodeList){
        Map<double[], List<T>> resultMap = new HashMap<double[], List<T>>();
        for (Integer nodeIndex : sampleIndexAndNodeIndexMap.keySet()) {
            List<T> subList = new ArrayList<T>();
            for (Integer sampleIndex : sampleIndexAndNodeIndexMap.get(nodeIndex)) {
                subList.add(sampleList.get(sampleIndex));
            }
            resultMap.put(nodeList.get(nodeIndex).nodeValue, subList);
        }
        if (DEBUG){
            StringBuilder str = new StringBuilder();
            str.append("Generate result is: \n");
            for (double[] node : resultMap.keySet()) {
                str.append("  |- ").append(Arrays.toString(node)).append(" = ");
                str.append(Arrays.toString(resultMap.get(node).toArray())).append("\n");
            }
            System.out.println(str.toString());
        }
        return resultMap;
    }



    private static <T extends KMeans.KMeansDimensionGenerator> boolean ifNodePositionChanged(List<T> sampleList, List<Node> kNodeList, Map<Integer, List<Integer>> sampleIndexAndNodeIndexMap){
        boolean ifPositionChanged = false;
        for (Integer nodeIndex : sampleIndexAndNodeIndexMap.keySet()) {
            List<Integer> sampleIndexList = sampleIndexAndNodeIndexMap.get(nodeIndex);
            double[] middleNode = findCenterPosition(sampleList, sampleIndexList);
            ifPositionChanged = Arrays.toString(middleNode).equals(kNodeList.get(nodeIndex).nodeValue);
            kNodeList.get(nodeIndex).nodeValue = middleNode;
        }
        return ifPositionChanged;
    }

    private static <T extends KMeans.KMeansDimensionGenerator> double[] findCenterPosition(List<T> sampleList, List<Integer> sampleIndexList){
        Node[] maxAndMinNodeArray = generateMaxAndMinNode(sampleList, sampleIndexList);
        double[] middleArray = new double[maxAndMinNodeArray[0].nodeValue.length];
        for(int i=0; i<maxAndMinNodeArray[0].nodeValue.length;i++){
            middleArray[i] = (maxAndMinNodeArray[0].nodeValue[i]+maxAndMinNodeArray[1].nodeValue[i])/2;
        }
        return middleArray;
    }

    private static <T extends KMeans.KMeansDimensionGenerator> Map<Integer, List<Integer>> initSamplesToNode(List<T> sampleList, List<Node> kNodeList){
        Map<Integer, List<Integer>> nodeIndexMap = new TreeMap<Integer, List<Integer>>();
        int sampleIndex = 0;
        for (T sample : sampleList) {
            int minNodeIndex = 0;
            double minNodexDistance = Double.MAX_VALUE;
            for(int i=0;i<kNodeList.size();i++){
                double distance = caculateDistance(sample.dimensions(), kNodeList.get(i).nodeValue);
                if (distance < minNodexDistance){
                    minNodexDistance = distance;
                    minNodeIndex = i;
                }
            }
            List<Integer> sampleIndexList = nodeIndexMap.get(minNodeIndex);
            if (sampleIndexList == null) sampleIndexList = new ArrayList<Integer>();
            sampleIndexList.add(sampleIndex);
            nodeIndexMap.put(minNodeIndex, sampleIndexList);
            sampleIndex++;
        }
        return nodeIndexMap;
    }

    private static double caculateDistance(double[] node1, double[] node2){
        double total = 0;
        int index = 0;
        while(index < node1.length){
            total += (node1[index]-node2[index])*(node1[index]-node2[index]);
            index++;
        }
        return Math.sqrt(total);
    }

    private static <T extends KMeans.KMeansDimensionGenerator> List<Node> initKNodes(List<T> sampleList, int k){
        Node[] maxAndMinNodeArray = generateMaxAndMinNode(sampleList, null);
        return initKNodesByMaxAndMinNodes(maxAndMinNodeArray[0], maxAndMinNodeArray[1], k);
    }

    private static List<Node> initKNodesByMaxAndMinNodes(Node maxNode, Node minNode, int k){
        List<Node> nodeList = new ArrayList<Node>();
        while(k-->0){
            Node randomNode = new Node(maxNode.nodeValue.length);
            for(int i=0; i< maxNode.nodeValue.length; i++){
                randomNode.nodeValue[i] = minNode.nodeValue[i]+(maxNode.nodeValue[i]-minNode.nodeValue[i])*Math.random();
            }
            nodeList.add(randomNode);
        }
        if (DEBUG) System.out.println(Arrays.toString(nodeList.toArray()));
        return nodeList;
    }

    private static <T extends KMeans.KMeansDimensionGenerator> Node[] generateMaxAndMinNode(List<T> sampleList, List<Integer> availableIndexList){
        if (availableIndexList==null) availableIndexList = new ArrayList<Integer>();
        if (availableIndexList.size() == 0)
            for(int i=0; i<sampleList.size();i++) availableIndexList.add(i);
        Node maxNode = new Node(sampleList.get(0).dimensions().length);
        Node minNode = new Node(sampleList.get(0).dimensions().length);
        // init
        for (int i =0; i< sampleList.get(availableIndexList.get(0)).dimensions().length; i++) {
            maxNode.nodeValue[i] = sampleList.get(availableIndexList.get(0)).dimensions()[i];
            minNode.nodeValue[i] = sampleList.get(availableIndexList.get(0)).dimensions()[i];
        }
        for (Integer sampleIndex : availableIndexList) {
            for (int i =0; i< sampleList.get(sampleIndex).dimensions().length; i++) {
                if (sampleList.get(sampleIndex).dimensions()[i] < minNode.nodeValue[i]) minNode.nodeValue[i] = sampleList.get(sampleIndex).dimensions()[i];
                if (sampleList.get(sampleIndex).dimensions()[i] > maxNode.nodeValue[i]) maxNode.nodeValue[i] = sampleList.get(sampleIndex).dimensions()[i];
            }
        }
        return new Node[]{maxNode, minNode};
    }

    public interface KMeansDimensionGenerator{
        double[] dimensions();
    }
    private static class Node{
        double[] nodeValue;
        Node(int length){nodeValue = new double[length];}
        public String toString(){
            return Arrays.toString(nodeValue);
        }
    }
}
