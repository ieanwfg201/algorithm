package com.hbhs.algorithm.classify.neuralnet;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by walter.xu on 2017/2/9.
 *
 */
public class RBFNeuralNetClassifier<T extends RBFNeuralNetClassifier.RBFData> {
    private DecimalFormat df = new DecimalFormat("0.000");
    private boolean debug = false;
    private int SPACE_LENGTH = 10;
    private static int EXPECT_OUTPUT_VALUE_DIVS = 10;
    private static float WIDTH_ADJUST_PARAMETER = 0.1F;
    private static float eta = 0.1f;
    private DataLevel inputLevel;
    private DataLevel hiddenLevel;
    private DataLevel outputLevel;
    public RBFNeuralNetClassifier(boolean debug){
        this.debug = debug;
    }

    public void train(List<RBFData> sampleList, int iterCount){
        initRBFModel(sampleList);
        while(iterCount-->0){
            sampleList.forEach(sample->{
                forward(sample.attributes());
                backwardAndAdjustWeight(sample.type(), sample.attributes());
            });
            log();
        }
    }

    /**
     * 初始化rbf算法模型
     */
    private void initRBFModel(List<RBFData> sampleList){
        // init level information
        int inputLevelNodeCount = sampleList.get(0).attributes().length;
        // 所有输入中的每一个属性的最大最小值
        Map<Integer, float[]> attributeMinMaxMap = new TreeMap<>();
        // 所有结果的列表
        Set<String> resultSet = new HashSet<>();
        sampleList.forEach(sample->{
            resultSet.add(sample.type());
            float[] attributeArray = sample.attributes();
            for(int i=0;i<attributeArray.length;i++){
                float[] maxMinArray = attributeMinMaxMap.get(i);
                if (maxMinArray==null) {maxMinArray=new float[]{attributeArray[i],attributeArray[i]};attributeMinMaxMap.put(i,maxMinArray);}
                if (attributeArray[i] < maxMinArray[0]) maxMinArray[0] =attributeArray[i];
                if (attributeArray[i] > maxMinArray[1]) maxMinArray[1] =attributeArray[i];
            }
        });

        inputLevel = new DataLevel("Input", attributeMinMaxMap);
        hiddenLevel = new DataLevel("Hidden", inputLevelNodeCount*2, inputLevelNodeCount, resultSet.size());
        outputLevel = new DataLevel("Output", new ArrayList<>(resultSet));
        calculateHiddenLevelWeightInfo();
        calculateHiddenLevelCenterParameterInfo();
        calculateHiddenLevelWidthInfo(sampleList.get(0).attributes());
    }

    /**
     * 计算hidden level -> output level的权重参数
     * Weight(i,j)=min(j)+i*(max(j)-min(j))/(totalOutputNodeCount+1)
     * <li>i: hidden level的node序列</li>
     * <li>j: output level的node序列</li>
     * <li>min(j)/max(j): output level的j节点上期望的最大值最小值</li>
     */
    private void calculateHiddenLevelWeightInfo(){
        int totalOuputCount = outputLevel.nodeList.size();
        for(int i=0;i<hiddenLevel.nodeList.size();i++){
            RBFHiddenNode hidden = (RBFHiddenNode)hiddenLevel.nodeList.get(i);
            for(int j=0;j<outputLevel.nodeList.size();j++){
                RBFOutputNode output = (RBFOutputNode)outputLevel.nodeList.get(j);
                hidden.hiddenOutputWeigthVector[j] = output.expectOutputVector[0]+
                        i*(output.expectOutputVector[1]-output.expectOutputVector[0])/(totalOuputCount+1);
            }
        }
    }

    /**
     * 计算hidden level的中心参数信息， 计算公式为:
     * CenterParam(j,i)=min(i)+(max(i)-min(i))/(2*totalHiddenNodeCount)+(j-1)(max(i)-min(i))/totalHiddenNodeCount
     * <li>i: input level的node序列</li>
     * <li>j: output level的node序列</li>
     * <li>max(i)/min(i): input level的j节点上期望的最大值最小值</li>
     */
    private void calculateHiddenLevelCenterParameterInfo(){
        int hiddenNodeCount = hiddenLevel.nodeList.size();
        for(int i=0;i<hiddenLevel.nodeList.size();i++){
            RBFHiddenNode hidden = (RBFHiddenNode)hiddenLevel.nodeList.get(i);
            for(int j=0;j<inputLevel.nodeList.size();j++){
                RBFInputNode input = (RBFInputNode)inputLevel.nodeList.get(j);
                float diff = input.attributeMinMax[1]-input.attributeMinMax[0];
                hidden.centerParameterVector[j] = input.attributeMinMax[0]+diff/(2*hiddenNodeCount)
                        +(i-1)*diff/hiddenNodeCount;
            }
        }
    }

    /**
     * 计算hidden level的宽度信息，计算公式为：
     * Width(i,j)= |CenterParam(i)(j)-input(j)|
     */
    private void calculateHiddenLevelWidthInfo(float[] attributes){
        for(int i=0;i<hiddenLevel.nodeList.size();i++){
            RBFHiddenNode hidden = (RBFHiddenNode)hiddenLevel.nodeList.get(i);
            for(int j=0;j<attributes.length;j++){
                hidden.widthVector[j] -= (hidden.centerParameterVector[j] - attributes[j])*WIDTH_ADJUST_PARAMETER;
            }
        }
    }


    private void forward(float[] attributes){
        // 设置input level的值
        for(int i=0;i<attributes.length;i++){
            inputLevel.nodeList.get(i).outputValue = attributes[i];
        }
        // 计算并设置hidden level的output值, 通过eta函数计算
        for(int i=0;i<hiddenLevel.nodeList.size();i++){
            RBFHiddenNode node = (RBFHiddenNode) hiddenLevel.nodeList.get(i);
            node.outputValue = rbfFunction(node,eta);
        }
        // 计算output level的output值
        for(int i=0;i<outputLevel.nodeList.size();i++){
            float total = 0;
            for(int j=0;j<hiddenLevel.nodeList.size();j++){
                RBFHiddenNode hiddenNode = (RBFHiddenNode)hiddenLevel.nodeList.get(j);
                total += hiddenNode.outputValue*hiddenNode.hiddenOutputWeigthVector[i];
            }
            outputLevel.nodeList.get(i).outputValue = total;  // TODO
        }
    }

    private void backwardAndAdjustWeight(String type, float[] attributes){
        // 设置output level node的误差值
        for(int i=0;i<outputLevel.nodeList.size();i++){
            RBFOutputNode node =(RBFOutputNode) outputLevel.nodeList.get(i);
            if (type.equals(node.type)) node.error = 1 - node.outputValue;
            else node.error = -1 - node.outputValue;
        }
        // 计算hidden level的相关参数
//        calculateHiddenLevelWeightInfo();
//        calculateHiddenLevelCenterParameterInfo();
//        calculateHiddenLevelWidthInfo(attributes);
        for(int i=0;i<hiddenLevel.nodeList.size();i++){
            RBFHiddenNode node = (RBFHiddenNode)hiddenLevel.nodeList.get(i);
            for(int j=0;j<node.centerParameterVector.length;j++){

            }
        }
    }

    /**
     * RBF函数，采用高斯分布
     * @param node hidden level node
     * @param eta 变量
     * @return result
     */
    private float rbfFunction(RBFHiddenNode node, float eta){
        float widthRatio = node.widthRatio(eta);
        float total = 0;
        for(int i=0; i<inputLevel.nodeList.size();i++){
            total += (inputLevel.nodeList.get(i).outputValue-node.centerParameterVector[i])
                    *(inputLevel.nodeList.get(i).outputValue-node.centerParameterVector[i]);
        }
        return (float)Math.exp(-total/(widthRatio*widthRatio));
    }


    private static class DataLevel {
        String levelName;
        List<RBFNode> nodeList = new ArrayList<>();
        public DataLevel(String levelName, int count, int inputNodeCount, int outputNodeCount){
            this.levelName = levelName;
            int index = 0;
            while(index<count) nodeList.add(new RBFHiddenNode(levelName+"-"+(index++ +1),inputNodeCount,outputNodeCount));
        }
        public DataLevel(String levelName, List<String> resultList){
            this.levelName = levelName;
            for(int i=0;i<resultList.size();i++){
                nodeList.add(new RBFOutputNode(levelName+"-"+(i+1),resultList.get(i)));
            }
        }
        public DataLevel(String levelName, Map<Integer, float[]> attributeMap){
            this.levelName = levelName;
            int index =0;
            for (Integer key : attributeMap.keySet()) {
                nodeList.add(new RBFInputNode(levelName+"-"+(index+1),attributeMap.get(key)));
            }
        }
    }
    private abstract static class RBFNode {
        String nodeName;
        float outputValue;
        public RBFNode(String nodeName){this.nodeName=nodeName;}
    }
    private static class RBFOutputNode extends RBFNode{
        String type;                                 // output value
        float[] expectOutputVector = new float[]{-1,1};     // 期望输出的值，1表示匹配，-1表示不匹配
        float error;
        public RBFOutputNode(String nodeName, String type){
            super(nodeName);
            this.type = type;
        }
    }
    private static class RBFInputNode extends RBFNode{
        float[] attributeMinMax = new float[2];
        public RBFInputNode(String nodeName, float[] minMaxArray){
            super(nodeName);
            this.attributeMinMax = minMaxArray;
        }
    }
    private static class RBFHiddenNode extends RBFNode{
        float[] widthVector;                 // 宽度向量
        float[] centerParameterVector;       // 中心参数向量
        float[] hiddenOutputWeigthVector;
        public RBFHiddenNode(String nodeName, int inputNodeCount, int outputNodeCount){
            super(nodeName);
            widthVector = new float[inputNodeCount];
            centerParameterVector=new float[inputNodeCount];
            hiddenOutputWeigthVector = new float[outputNodeCount];
        }
        public float widthRatio(float eta){
            float widthRatio = 0;
            for(float width: widthVector){
                widthRatio = width*width;
            }
            widthRatio /= widthVector.length;
            return (float)(eta*Math.sqrt(widthRatio));
        }
    }

    public interface RBFData{
        float[] attributes();
        String type();
    }

    public void log(){
        if (!debug) return ;
        StringBuilder str = new StringBuilder();
        str.append("RBF neural node informations: \n");
        boolean hasNext = true;
        int inputLength = 0;
        int row = 0;
        while (hasNext){
            if (row<inputLevel.nodeList.size()){
                String line = Arrays.toString(((RBFInputNode)inputLevel.nodeList.get(row)).attributeMinMax);
                if (inputLength==0) inputLength = line.length();
                str.append(line).append(space(SPACE_LENGTH));
                hasNext = true;
            }else{
                str.append(space(inputLength)).append(space(SPACE_LENGTH));
                hasNext = false;
            }
            if (row<hiddenLevel.nodeList.size()){
                RBFHiddenNode hidden = (RBFHiddenNode)hiddenLevel.nodeList.get(row);
                str.append("[").append(df.format(hidden.centerParameterVector[0]));
                for(int i=1;i<hidden.centerParameterVector.length;i++){
                    str.append(",").append(df.format(hidden.centerParameterVector[i]));
                }
                str.append("]").append(space(5));

                str.append("[").append(df.format(hidden.widthVector[0]));
                for(int i=1;i<hidden.widthVector.length;i++){
                    str.append(",").append(df.format(hidden.widthVector[i]));
                }
                str.append("]").append(space(5));

                str.append("[").append(df.format(hidden.hiddenOutputWeigthVector[0]));
                for(int i=1;i<hidden.hiddenOutputWeigthVector.length;i++){
                    str.append(",").append(df.format(hidden.hiddenOutputWeigthVector[i]));
                }
                str.append("]");

                str.append(space(SPACE_LENGTH));
                hasNext = true;
            }else{
                str.append(space(inputLength)).append(space(SPACE_LENGTH));
                hasNext = false;
            }
            if (row<outputLevel.nodeList.size()){
                RBFOutputNode node = (RBFOutputNode)outputLevel.nodeList.get(row);
                str.append(df.format(node.outputValue)).append("|").append(df.format(node.error)).append("|")
                        .append(Arrays.toString(node.expectOutputVector)).append("->").append(node.type).append(space(SPACE_LENGTH));
                hasNext = true;
            }else{
                str.append(space(inputLength)).append(space(SPACE_LENGTH));
                hasNext = false;
            }
            str.append("\n");
            row++;
        }
        System.out.println(str.toString());
    }

    /**
     * 生成指定数量的空白串
     * 如space(1)=" ", space(3)="   ", space(4)="    "
     */
    private String space(int count){
        StringBuilder str = new StringBuilder();
        while(count-- >0) str.append(" ");
        return str.toString();
    }
}
