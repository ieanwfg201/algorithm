package com.hbhs.algorithm.classify.neuralnet;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by walter.xu on 2017/2/7.
 * BP神经网络归类器, 其本质为，通过对多个训练样本，做训练，生成一个图网络的权重表，通过权重表可以表达在某一层中，通过该节点的权重，
 * 当输入待测试节点时，会根据输入的各个属性和权重表计算得到到下一层所有节点的权值，迭代下去后，可得到最终的结果
 * 详细数学论证过程参考：http://baike.baidu.com/view/1753676.htm
 * 1. 区分三层，inputLevel, hiddenLevel..., outputLevel
 * inputLevel: 输入层，表示为每个样本的属性向量列表，改成节点数应该为样本的属性列表(样本矢量)
 * hiddenLevel: 隐藏层，可为多层，作为中间计算层，每层的节点数可自定义，一般需要比inputLevel高
 * outputLevel: 输出层，为最终可能的结果层，当有几个结果的时候，改层有几个节点
 * 2. 每层中需要包含如下属性：
 * forwardInputValue: 节点的前向输入参数，当为inputLevel节点时，表示某个属性值，否则应该为指向该节点的前一层的所有节点的forwardOutputValue与指向该节点的权重的乘积的总和
 * forwardOuputValue: 节点的前向输出参数，当为inputLevel节点时，表示某个属性值，否则为当前节点的forwardInputValue的增量法则输出，一般为sigmoid函数
 * backwardInputValue: 节点的反向输入参数，该为output节点时，应该改节点的forwardOuputValue以及对应函数(sigmoid)的导数求得，否则是由改节点关联的所有后置level子节点的backwardOutputValue与对应的权值乘积的总和
 * backwardOuputValue: 节点的反向输出参数，为当前节点的backwardInputValue的sigmoid函数值
 *
 */
public class BPNeuralNetClassifier<T extends BPNeuralNetClassifier.BPData> {
    private static DecimalFormat df = new DecimalFormat("0.000");
    private float eta = 0.02f;      // 误差下降分子，用于训练时的误差下降幅度
    private int hiddenLevelSize = 1;      // 默认隐藏层层数
    private boolean debug = false;        // 是否打开debug模式
          // 默认结果集合

    private DataLevel inputLevel;                                  // 输入层
    private List<DataLevel> hiddenLevelList = new ArrayList<>();   // 影藏层列表
    private DataLevel outputLevel;                                 // 输出层
    private List<float[][]> levelWeightList = new ArrayList<>();   // 各层节点间的权重列表

    public BPNeuralNetClassifier(boolean debug, int hiddenLevelSize){
        this.debug = debug;
        if (hiddenLevelSize>1&&hiddenLevelSize<6) this.hiddenLevelSize = hiddenLevelSize;
    }

    /**
     * 测试结果集合
     * @param node 待测试数据节点
     * @return 测试结果
     */
    public String test(BPData node){
        // checking
        if (node==null||node.attributeArrays()==null||node.attributeArrays().length!=inputLevel.nodeList.size()) return null;
        // 通过权重表对该节点做前向测试，
        forward(node.attributeArrays());
        // 校验所有的output level值，返回最大的值即为预测值
        float result = 2;
        String value = null;
        for (BPNode bpNode : outputLevel.nodeList) {
            float diff = 1-bpNode.forwardOutputValue;
            if (diff < result) {
                result = diff;
                value = ((BPOutputNode)bpNode).type;
            }
        }
        return value;
    }

    /**
     * 训练 样本
     * @param sampleList 样本数据列表
     * @param iterCount 迭代次数
     */
    public void train(List<BPData> sampleList, int iterCount){
        int inputLevelNodeSize = sampleList.get(0).attributeArrays().length;

        // 初始化output value 和模型
        Set<String> resultTypeList = new HashSet<>();
        sampleList.forEach(sample -> resultTypeList.add(sample.type()));
        initModel(inputLevelNodeSize, resultTypeList);
//        logWeight();
        // 迭代训练
        while(iterCount-- > 0) {
            sampleList.forEach(sample -> {
                // 对每一个样本做前向/反向传播，同时修改权重列表
                forward(sample.attributeArrays());
                backward(sample.type());
                updateWeight(eta);
            });
//            logWeight();
        }

    }

    /**
     * 初始化 对象
     * @param inputLevelNodeSize 输入的节点数量
     */
    private void initModel(int inputLevelNodeSize, Set<String> resultTypeList){
        // init input level
        inputLevel = new DataLevel("INPUT", inputLevelNodeSize);
        // init hidden level
        int hiddenLevelNodeSize = inputLevelNodeSize+1;
        hiddenLevelList.clear();
        int index = hiddenLevelSize;
        while(index-->0){
            hiddenLevelList.add(new DataLevel("HIDDEN-"+(hiddenLevelSize-index), hiddenLevelNodeSize));
        }
        // init output
        outputLevel = new DataLevel("OUTPUT", new ArrayList<>(resultTypeList));

        levelWeightList.clear();
        // init weight-map, input level -> hidden[0] level
        levelWeightList.add(randomInitTwoLevelWeight(inputLevel, hiddenLevelList.get(0)));
        // init weight-map, hidden[0] level ->hidden[hidden.length()-1] level
        for(int i=0;i<hiddenLevelList.size()-1;i++)
            levelWeightList.add(randomInitTwoLevelWeight(hiddenLevelList.get(i), hiddenLevelList.get(i + 1)));
        // init weight-map, hidden[hidden.length()-1] level -> output level
        levelWeightList.add(randomInitTwoLevelWeight(hiddenLevelList.get(hiddenLevelList.size() - 1), outputLevel));
    }
    // 随机初始化两层之间的权重数组
    private float[][] randomInitTwoLevelWeight(DataLevel previous, DataLevel next){
        float[][] weigthArray = new float[previous.nodeList.size()][next.nodeList.size()];
        for(int i=0;i<previous.nodeList.size();i++){
            for(int j=0;j<next.nodeList.size();j++){
                weigthArray[i][j] = (float)(Math.random()*0.1);
            }
        }
        return weigthArray;
    }

    /**
     * 根据样本节点的所有属性值做前向传播，生成input/hidden/output的前向输入值(forwardInputValue)
     * @param attributes 某一样本的属性值
     */
    private void forward(float[] attributes){
        // input level. 仅设置输入值即可
        for(int i=0;i<attributes.length;i++){
            inputLevel.nodeList.get(i).setForwardInputAndOutputValue(attributes[i], true);
        }
        int weightIndex = 0;
        // 计算 input level -> hidden[0] level
        caculateForwardInputValueBetweenTwoLevel(inputLevel, hiddenLevelList.get(0), levelWeightList.get(weightIndex++));
        // 计算 hidden[0] -> hidden[hidden.length()] level
        for(int i=1;i<hiddenLevelList.size();i++){
            caculateForwardInputValueBetweenTwoLevel(hiddenLevelList.get(i-1), hiddenLevelList.get(i), levelWeightList.get(weightIndex++));
        }
        // 计算 hidden[hidden.length()] level -> output leve
        caculateForwardInputValueBetweenTwoLevel(hiddenLevelList.get(hiddenLevelList.size()-1), outputLevel, levelWeightList.get(weightIndex));
    }

    /**
     * 计算两个level间的forward value, previous level -> nxt level
     * @param previous 从该level开始
     * @param next 计算该level的inputForward值
     * @param weightArray 该映射的权重值
     */
    private void caculateForwardInputValueBetweenTwoLevel(DataLevel previous, DataLevel next, float[][] weightArray){
        for(int i=0;i<next.nodeList.size();i++){
            float total = 0;
            for(int j=0;j<previous.nodeList.size();j++){
                total += previous.nodeList.get(j).forwardOutputValue*weightArray[j][i];
            }
            next.nodeList.get(i).setForwardInputAndOutputValue(total, false);
        }
    }
    /**
     * 根据样本节点的所有属性值做反向传播，生成input/hidden/output的前向输入值(forwardInputValue)
     * @param type 某一样本的属性值
     */
    private void backward(String type){
        //初始化output level
        for(int i=0;i<outputLevel.nodeList.size();i++){
            //输出层计算误差把误差反向传播，这里-1代表不属于，1代表属于
            float deviation = -1;
            if (type.equals(((BPOutputNode)outputLevel.nodeList.get(i)).type))
                deviation = 1;
            outputLevel.nodeList.get(i).setBackwardInputAndOutputValue(outputLevel.nodeList.get(i).forwardOutputValue - deviation, false);
        }
        int levelWeightIndex = levelWeightList.size()-1;
        // 计算ouput level -> hidden[hidden.length()-1] level
        caculateBackwardInputValueBetweenTwoLevel(hiddenLevelList.get(hiddenLevelList.size()-1), outputLevel, levelWeightList.get(levelWeightIndex--));
        // 计算 hidden[hidden.length()] -> hidden[0] level
        for(int i=hiddenLevelList.size()-1;i>0;i--){
            caculateBackwardInputValueBetweenTwoLevel(hiddenLevelList.get(i - 1), hiddenLevelList.get(i), levelWeightList.get(levelWeightIndex--));
        }
        // 计算 hidden[0] level -> input level
        caculateBackwardInputValueBetweenTwoLevel(inputLevel, hiddenLevelList.get(0), levelWeightList.get(0));
    }
    private void caculateBackwardInputValueBetweenTwoLevel(DataLevel previous, DataLevel next, float[][] weightArray){
        for(int i=0;i<previous.nodeList.size();i++){
            float total = 0;
            for(int j=0;j<next.nodeList.size();j++){
                total += next.nodeList.get(j).backwardOutputValue*weightArray[i][j];
            }
            previous.nodeList.get(i).setBackwardInputAndOutputValue(total, false);
        }
    }

    /**
     * 根据误差下降幅度参数更新权重表
     * @param eta 误差下降幅度参数
     */
    private void updateWeight(float eta){
        int weightIndex = 0;
        // 计算 input level -> hidden[0] level的weight
        updateWeigthBetweenTwoLevel(inputLevel, hiddenLevelList.get(0), levelWeightList.get(weightIndex++), eta);
        // 计算 hidden[0] level -> hidden[hidden.length()-1] level的weight
        for(int i=1;i<hiddenLevelList.size();i++)
            updateWeigthBetweenTwoLevel(hiddenLevelList.get(i-1), hiddenLevelList.get(i), levelWeightList.get(weightIndex++), eta);
        // 计算 hidden[hidden.length()-1] level -> output level 的weight
        updateWeigthBetweenTwoLevel(hiddenLevelList.get(hiddenLevelList.size()-1), outputLevel, levelWeightList.get(weightIndex), eta);
    }

    /**
     * 更新两个level间的权重，每个权重的梯度都等于与其相连的前一层节点的输出乘以与其相连的后一层的反向传播的输出
     * @param previous 前一层
     * @param next 后一层
     * @param weightArray 权重数组
     */
    private void updateWeigthBetweenTwoLevel(DataLevel previous, DataLevel next, float[][] weightArray, float eta){
        for(int i=0;i<previous.nodeList.size();i++){
            for(int j=0;j<next.nodeList.size();j++){
                weightArray[i][j] -= eta*previous.nodeList.get(i).forwardOutputValue
                        *next.nodeList.get(j).backwardOutputValue;
            }
        }
    }

    /**
     * BP神经网络算法的数据节点: 样本数据和测试数据
     */
    public interface BPData{
        /**
         * 属性列表值数组
         * @return 所有属性数组
         */
        float[] attributeArrays();

        /**
         * 该节点的类型
         * @return 类型
         */
        String type();
    }

    /**
     * 数据level，可为input level, hidden level, output level
     */
    private static class DataLevel {
        String levelName;                             // level name
        List<BPNode> nodeList = new ArrayList<>();    // node list current level contains

        /**
         * 初始化inputLevel和hiddenLevel
         */
        public DataLevel(String levelName, int nodeSize){
            this.levelName = levelName;
            int index = 0;
            while(index <nodeSize){
                nodeList.add(new BPNode(levelName+"-NODE_"+ ++index));
            }
        }

        /**
         * 初始化outputLevel
         */
        public DataLevel(String levelName, List<String> typeList){
            this.levelName = levelName;
            int index = 0;
            while(index <typeList.size()){
                nodeList.add(new BPOutputNode(levelName + "-NODE_" + ++index, typeList.get(index-1)));
            }

        }
    }
    // bp节点类
    private static class BPNode{
        // 节点名称
        String nodeName;
        float forwardInputValue;      // 节点的前向输入值
        float forwardOutputValue;     // 节点的前向输出值
        float backwardInputValue;     // 节点的反向输入值
        float backwardOutputValue;    // 节点的反向输出值
        BPNode(String nodeName){this.nodeName=nodeName;}
        // 前向sigmoid函数
        public float forwardSigmoid(float value){
            return tanhS(value);
//            return logS(value);
        }
        /**
         * log-sigmoid函数
         */
        private float logS(float in) {
            return (float) (1 / (1 + Math.exp(-in)));
        }
        /**
         * log-sigmoid函数的导数
         */
        private float logSDerivative(float in) {
            return forwardOutputValue * (1 - forwardOutputValue) * in;
        }
        /**
         * tan-sigmoid函数
         */
        private float tanhS(double in) {
            return (float) ((Math.exp(in) - Math.exp(-in)) / (Math.exp(in) + Math
                    .exp(-in)));
        }
        // 反向导数
        public float backwardPropagate(float value){
            return tanhSDerivative(value);
//            return logSDerivative(value);
        }
        /**
         * tan-sigmoid函数的导数
         */
        private float tanhSDerivative(float in) {
            return (float) ((1 - Math.pow(forwardOutputValue, 2)) * in);
        }

        /**
         * 设置前向输入和输出值
         * @param forwardInputValue 输入值
         * @param isInputNode 是否为inputNode
         */
        public void setForwardInputAndOutputValue(float forwardInputValue, boolean isInputNode){
            this.forwardInputValue = forwardInputValue;
            this.forwardOutputValue = forwardInputValue;
            // 当不是inputLevel node时，对应的forwardOuputValue应该为当前节点的forwardInputValue的sigmoid函数求得
            if (!isInputNode) this.forwardOutputValue = forwardSigmoid(forwardInputValue);
        }
        /**
         * 设置反向输入和输出值
         * @param backwardInputValue 输入值
         * @param isInputNode 是否为inputNode
         */
        public void setBackwardInputAndOutputValue(float backwardInputValue, boolean isInputNode){
            this.backwardInputValue = backwardInputValue;
            this.backwardOutputValue = backwardInputValue;
            // 当不是inputLevel node时，对应的backwardOutputValue应该为当前节点的backwardInputValue的导数函数求得
            if (!isInputNode) this.backwardOutputValue = backwardPropagate(backwardInputValue);
        }
        public String buildValue(){
            StringBuilder str = new StringBuilder();
            str.append("[");
            str.append("{").append(df.format(forwardInputValue)).append(",").append(df.format(forwardOutputValue)).append("},");
            str.append("{").append(df.format(backwardInputValue)).append(",").append(df.format(backwardOutputValue)).append("}");
            str.append("]");
            return str.toString();
        }
    }
    // 输出node节点
    private static class BPOutputNode extends BPNode{
        String type;
        BPOutputNode(String nodeName, String type){super(nodeName); this.type = type;}
        public String buildValue(){
            return super.buildValue()+" -> "+type;
        }
    }

    /**
     * 打印权重数组信息
     */
    public void logWeight(){
        if (!debug) return ;
        StringBuilder arrayStr = new StringBuilder();
        boolean hasNext = true;
        int spaceLength = 4;
        int row = 0;
        List<Integer> lengthSize = new ArrayList<>();
        while(hasNext){
            arrayStr.append(space(spaceLength));
            for(int i=0;i<levelWeightList.size();i++){
                float[][] floats = levelWeightList.get(i);
                if (row< floats.length){
                    String arrays = logArrays(floats[row]);
                    if (lengthSize.size()<levelWeightList.size()) lengthSize.add(arrays.length());
                    arrayStr.append(arrays).append(space(spaceLength));
                    hasNext = true;
                }else{
                    arrayStr.append(space(lengthSize.get(i))).append(space(spaceLength));
                    hasNext = false;
                }

            }
            arrayStr.append("\n");
            row++;
        }

        StringBuilder str = new StringBuilder();
        str.append("Weight information: only support pattern as: ").append(df.toPattern()).append("\n");
        String header = inputLevel.levelName+"->"+hiddenLevelList.get(0).levelName;
        str.append(space(spaceLength)).append(header).append(space(lengthSize.get(0)-header.length())).append(space(spaceLength));
        for(int i=0;i<hiddenLevelList.size()-1;i++){
            header = hiddenLevelList.get(i).levelName+"->"+hiddenLevelList.get(i+1).levelName;
            str.append(header).append(space(lengthSize.get(0)-header.length())).append(space(spaceLength));
        }
        header = hiddenLevelList.get(hiddenLevelList.size()-1).levelName+"->"+outputLevel.levelName;
        str.append(header).append(space(lengthSize.get(0)-header.length())).append(space(spaceLength));
        str.append("\n");
        str.append(arrayStr);
        System.out.println(str.toString());
        try{Thread.sleep(1);}catch(Exception e){e.printStackTrace();}
    }
    private String logArrays(float[] arrays){
        StringBuilder str = new StringBuilder();
        for (float array : arrays) {
            str.append(",").append(df.format(array));
        }
        return "["+str.substring(1)+"]";
    }

    /**
     * 打印节点信息
     */
    public void logNode(){
        StringBuilder arrayStr = new StringBuilder();
        boolean hasNext = true;
        int spaceLength = 4;
        int row = 0;
        int totalSize = hiddenLevelList.size()+2;
        List<Integer> lengthSize = new ArrayList<>();
        int index = 0;
        while(index++ < hiddenLevelList.size()+2){
            lengthSize.add(40);
        }
        while(hasNext){
            arrayStr.append(space(spaceLength));
            // input level
            if (row<inputLevel.nodeList.size()){
                String value = row + inputLevel.nodeList.get(row).buildValue();
//                if (lengthSize.size()<totalSize) lengthSize.add(value.length());
                arrayStr.append(value).append(space(spaceLength));
                hasNext = true;
            }else {
                arrayStr.append(space(lengthSize.get(0))).append(space(spaceLength));
                hasNext = false;
            }
            // hidden level
            for(int i=0;i<hiddenLevelList.size();i++){
                if (row<hiddenLevelList.get(i).nodeList.size()){
                    String value = row + hiddenLevelList.get(i).nodeList.get(row).buildValue();
//                    if (lengthSize.size()<totalSize) lengthSize.add(value.length());
                    arrayStr.append(value).append(space(spaceLength));
                    hasNext = true;
                }else{
                    arrayStr.append(space(lengthSize.get(i+1))).append(space(spaceLength));
                    hasNext = false;
                }
            }
            // output level
            if (row<outputLevel.nodeList.size()){
                String value = row + outputLevel.nodeList.get(row).buildValue();
//                if (lengthSize.size()<totalSize) lengthSize.add(value.length());
                arrayStr.append(value).append(space(spaceLength));
                hasNext = true;
            }else {
                arrayStr.append(space(lengthSize.get(0))).append(space(spaceLength));
                hasNext = false;
            }

            arrayStr.append("\n");
            row++;
        }

        StringBuilder str = new StringBuilder();
        str.append("Node information: only support pattern as: ").append(df.toPattern()).append("\n");
        // input level
        String header = inputLevel.levelName;
        str.append(space(spaceLength)).append(header).append(space(lengthSize.get(0)-header.length())).append(space(spaceLength));
        // hidden level
        for(int i=0;i<hiddenLevelList.size();i++){
            header = hiddenLevelList.get(i).levelName;
            str.append(header).append(space(lengthSize.get(i+1)-header.length())).append(space(spaceLength));
        }
        // ouput level
        header = outputLevel.levelName;
        str.append(header).append(space(lengthSize.get(lengthSize.size()-1)-header.length())).append(space(spaceLength));

        str.append("\n");
        str.append(arrayStr);
        System.out.println(str.toString());
        try{Thread.sleep(1);}catch(Exception e){e.printStackTrace();}
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
