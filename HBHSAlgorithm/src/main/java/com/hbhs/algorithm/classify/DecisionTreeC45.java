package com.hbhs.algorithm.classify;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by walter.xu on 2017/1/22.
 */
public class DecisionTreeC45<T> {
    public Boolean DEBUG = false;
    private DecisionTreeNode root = null;

    public DecisionTreeNode generateDecisionTree(List<T> list) throws Exception{
        if (list == null||list.size()==0) return null;
        List<String[]> quotaValuesList = new ArrayList<String[]>();
        List<Boolean> resultList = new ArrayList<Boolean>();
        this.generateQuotaValuesAndResultByAnnotation(list, quotaValuesList, resultList);
        if (quotaValuesList.size()==0||resultList.size()==0) return null;

        this.generateDecisionTree(quotaValuesList, resultList);
        return root;
    }

    private void generateQuotaValuesAndResultByAnnotation(List<T> list,  List<String[]> quotaValuesList, List<Boolean> resultList) throws Exception{
        if (list==null||list.size()==0) return ;
        List<Field> quotaFiledList = new ArrayList<Field>();
        Field resultField = null;
        Field[] allFields = list.get(0).getClass().getDeclaredFields();
        for(Field field: allFields){
            if (field.getAnnotation(DecisionTreeC45QuotaField.class)!=null){field.setAccessible(true); quotaFiledList.add(field);}
            if (resultField==null&&field.getAnnotation(DecisionTreeC45QuotaResult.class)!=null) {field.setAccessible(true); resultField = field;}
        }
        if (quotaFiledList.size()==0||resultField==null) {
            if (DEBUG) System.out.println("No annotation @DecisionTreeC45QuotaField or @DecisionTreeC45QuotaResult found at class: "+list.get(0).getClass());
            return ;
        }

        for (T t : list) {
            String[] quotaValueArray = new String[quotaFiledList.size()];
            int index = 0;
            for (Field field : quotaFiledList) {
                Object value = field.get(t);
                quotaValueArray[index++] = value==null?"":value.toString();
            }
            quotaValuesList.add(quotaValueArray);

            Object resultValue = resultField.get(t);
            Boolean result = false;
            if (resultValue!=null&&!"".equals(resultValue.toString().trim())){
                if (resultValue instanceof Boolean) result = (Boolean) resultValue;
                else if (resultValue instanceof Integer) result = ((Integer) resultValue)!=0;
                else if (resultValue instanceof Double) result = ((Double) resultValue)!=0;
                else if (resultValue instanceof Long) result = ((Long) resultValue)!=0;
                else if (resultValue instanceof Float) result = ((Float) resultValue)!=0;
                else result = true;   // 其他情况下 null
            }
            resultList.add(result);
        }
    }

    /**
     * 依据样本参数以及结果集合生成决策树-ID3
     * @param quotaValuesList 各个指标值列表。list: 样本列表， string[]：指标列表
     * @param resultList 各个样本对应的值
     * @return 决策树信息
     */
    public DecisionTreeNode generateDecisionTree(List<String[]> quotaValuesList, List<Boolean> resultList){
        if (quotaValuesList==null||quotaValuesList.size()==0||resultList==null||resultList.size()==0) return null;
        // 初始化决策树结构
        root = new DecisionTreeNode("", "ROOT");
        // 初始化可使用的指标列表和样本列表
        List<Integer> availableQuotaIndexList = new ArrayList<Integer>();
        List<Integer> availableValueIndexList = new ArrayList<Integer>();
        int index = 0;
        while(index<quotaValuesList.get(0).length) availableQuotaIndexList.add(index++);
        index = 0;
        while(index<quotaValuesList.size()) availableValueIndexList.add(index++);
        // 生成决策树
        this.generateDecisionTree(root, quotaValuesList, resultList, availableQuotaIndexList, availableValueIndexList);
        return root;
    }

    /**
     * 依据样本参数以及结果集合生成决策树-ID3
     * @param node 当前决策树的生成节点
     * @param quotaValuesList 各个指标值列表。list: 样本列表， string[]：指标列表
     * @param resultList 各个样本对应的值
     * @param availableQuotaIndexList 指标信息列表
     * @param availableValueIndexList 样本信息列表
     */
    private void generateDecisionTree(DecisionTreeNode node, List<String[]> quotaValuesList, List<Boolean> resultList,
                                      List<Integer> availableQuotaIndexList, List<Integer> availableValueIndexList){
        double totalChooseEntropy = this.generateTopEntropy(resultList, availableValueIndexList);
        // 1. 计算出当前可用指标，可用样本下的最优指标(根据熵降低最快-即熵最小)
        QuotaEntropyEntity selectQuota = calculateAndSelectQuotaByMinEntropy(quotaValuesList, resultList, availableQuotaIndexList, availableValueIndexList, totalChooseEntropy);
        if (selectQuota == null) return ;
        // 2. 将该指标作为当前决策树下的子节点，构造决策树
        for (String valueKey : selectQuota.valueMap.keySet()) {
            DecisionTreeNode subNode = new DecisionTreeNode(node.prefixName+"    ", selectQuota.quotaIndex+" - "+valueKey);
            QuotaEntropyEntity.QuotaValueEntity entity = selectQuota.valueMap.get(valueKey);
            // 如果当前指标当前值下，无论其他指标为何，但是结果均一致，则表示无论其他指标情况下，最终结果均已经确定，所以无需对其他的指标做拆分
            if (entity.valueCountArray[0] == 0||entity.valueCountArray[1]==0){
                subNode.trueOrFalse = entity.valueCountArray[0] != 0;
            }else{
                // 需要对其他指标再做拆分，重新再当前子节点下构造决策树
                // 构造可用的指标列表，需排序当前指标
                List<Integer> availableQuotaIndexListCopy = this.copyList(availableQuotaIndexList);
                availableQuotaIndexListCopy.remove(selectQuota.quotaIndex);

                // 如果剩余指标不为空，则重新构造
                if (availableQuotaIndexListCopy.size()>0)
                    generateDecisionTree(subNode, quotaValuesList, resultList, availableQuotaIndexList, entity.avaialbeValueIndexList);
            }
            node.subNodeMap.put(valueKey, subNode);
        }
    }

    /**
     * COPY list
     * @param list list to copy
     * @return copy list, should not equal with original one
     */
    private List<Integer> copyList(List<Integer> list){
        List<Integer> resultList = new ArrayList<Integer>();
        for (Integer key : list) {
            resultList.add(key);
        }
        return resultList;
    }

    /**
     * 根据熵计算最优化指标，并获取到该指标信息
     * @param quotaValuesList  各个指标值列表。list: 样本列表， string[]：指标列表
     * @param resultList 各个样本对应的值
     * @param availableQuotaIndexList 指标信息列表
     * @param availableValueIndexList 样本信息列表
     * @return 最优化的指标信息
     */
    public QuotaEntropyEntity calculateAndSelectQuotaByMinEntropy(List<String[]> quotaValuesList, List<Boolean> resultList,
                                                                  List<Integer> availableQuotaIndexList, List<Integer> availableValueIndexList, double parentEntropy){
        if (availableValueIndexList.size()==0||availableQuotaIndexList.size()==0) return null;

        Map<Integer, QuotaEntropyEntity> quotaEntropyMap = this.generateQuotaMap(quotaValuesList, resultList, availableQuotaIndexList, availableValueIndexList);
        // 计算各指标的独立熵
        this.calculateQuotaEntropyAndInfoGain(quotaEntropyMap, availableValueIndexList.size(), parentEntropy);
        // 打印信息
        this.printQuotaEntropyMap(quotaEntropyMap);
        // 返回最小熵的指标
        return this.findMaxGainInfoRatioOfQuotaEntity(quotaEntropyMap);
    }

    /**
     * 生成当前指标，当前样本下对应的指标熵信息列表， 当前结果下并未计算熵信息
     * @param quotaValuesList  各个指标值列表。list: 样本列表， string[]：指标列表
     * @param resultList 各个样本对应的值
     * @param availableQuotaIndexList 指标信息列表
     * @param availableValueIndexList 样本信息列表
     * @return key: 指标index，value:指标对应熵信息
     */
    private Map<Integer, QuotaEntropyEntity> generateQuotaMap(List<String[]> quotaValuesList, List<Boolean> resultList,
                                                              List<Integer> availableQuotaIndexList, List<Integer> availableValueIndexList){
        Map<Integer, QuotaEntropyEntity> quotaEntropyMap = new TreeMap<Integer, QuotaEntropyEntity>();
//        Map<Integer, Map<String, int[]>> quotaMap = new TreeMap<Integer, Map<String, int[]>>();
        for (Integer valueIndex : availableValueIndexList) {
            String[] quotaArray = quotaValuesList.get(valueIndex);
            for (Integer quotaIndex : availableQuotaIndexList) {

                QuotaEntropyEntity quotaEntropy = quotaEntropyMap.get(quotaIndex);
                // 如果不存在，则初始化QuotaEntropyEntity对象
                if (quotaEntropy==null){ quotaEntropy = new QuotaEntropyEntity(); quotaEntropy.quotaIndex = quotaIndex; quotaEntropyMap.put(quotaIndex,quotaEntropy); }

                QuotaEntropyEntity.QuotaValueEntity valueEntity = quotaEntropy.valueMap.get(quotaArray[quotaIndex]);
                // 如果ValueCount不存在，则初始化valueCount map
                if (valueEntity==null){
                    valueEntity = new QuotaEntropyEntity.QuotaValueEntity();
                    valueEntity.valueName = quotaArray[quotaIndex];
                    quotaEntropy.valueMap.put(quotaArray[quotaIndex], valueEntity);
                }
                valueEntity.avaialbeValueIndexList.add(valueIndex);
                // 做count操作，计算各种样本情况下，选择该指标的时候决策的最终值
                if (resultList.get(valueIndex)) valueEntity.valueCountArray[0] = valueEntity.valueCountArray[0]+1;
                else valueEntity.valueCountArray[1] = valueEntity.valueCountArray[1]+1;
            }
        }
        return quotaEntropyMap;
    }

    /**
     * 计算各个指标的entropy 熵值以及gainInfo
     * @param quotaEntropyMap 指标map对象
     * @param totalSize 样本总大小
     */
    private void calculateQuotaEntropyAndInfoGain(Map<Integer, QuotaEntropyEntity> quotaEntropyMap, int totalSize, double parentEntropy){
        for (Integer quotaIndex : quotaEntropyMap.keySet()) {
            Map<String, QuotaEntropyEntity.QuotaValueEntity> valueCountMap = quotaEntropyMap.get(quotaIndex).valueMap;
            // 总的entrpy = 占比*子entropy+占比*子entropy+...
            for (String valueKey : valueCountMap.keySet()) {
                // 计算value属性的entropy
                int[] count = valueCountMap.get(valueKey).valueCountArray;
                valueCountMap.get(valueKey).entropy = entropy(count[0], count[1])*(count[0]+count[1])/totalSize;
                // 计算当前总的entropy
                quotaEntropyMap.get(quotaIndex).entropy += valueCountMap.get(valueKey).entropy;
                // 计算split information
                quotaEntropyMap.get(quotaIndex).splitInfo += splitInformation(count[0]+count[1], totalSize);
            }
            // 信息增益 = 父选择节点的entropy-当前选择的entropy
            quotaEntropyMap.get(quotaIndex).gainInfo = parentEntropy-quotaEntropyMap.get(quotaIndex).entropy;
            // 信息增益率 = 信息增益/分离信息
            quotaEntropyMap.get(quotaIndex).gainInfoRatio = quotaEntropyMap.get(quotaIndex).gainInfo/quotaEntropyMap.get(quotaIndex).splitInfo;
        }
    }

    /**
     * 查询最小熵值得一个指标, 需要在{@link #calculateQuotaEntropyAndInfoGain(Map, int, double)} 方法执行后有效
     * @param quotaEntropyMap 指标map对象
     * @return 最小熵值得指标对象
     */
    private QuotaEntropyEntity findMaxGainInfoRatioOfQuotaEntity(Map<Integer, QuotaEntropyEntity> quotaEntropyMap){
        int minQuotaIndex = 0;
        double maxGainInfoRatio = 0;
        for (Integer quotaIndex : quotaEntropyMap.keySet()) {
            if (quotaEntropyMap.get(quotaIndex).gainInfoRatio>maxGainInfoRatio){
                minQuotaIndex = quotaIndex; maxGainInfoRatio = quotaEntropyMap.get(quotaIndex).gainInfoRatio;
            }
        }
        return quotaEntropyMap.get(minQuotaIndex);
    }

    private void printQuotaEntropyMap(Map<Integer, QuotaEntropyEntity> quotaEntropyMap){
        if (!DEBUG) return ;
        StringBuilder str = new StringBuilder();
        str.append("Entropy: informations for \n");
        for (Integer quotaIndex : quotaEntropyMap.keySet()) {
            str.append("      |-").append(quotaEntropyMap.get(quotaIndex)).append("\n");
        }
        System.out.println(str);
    }

    public double generateTopEntropy(List resultList, List<Integer> availableValueIndexList){
        Map<String, Integer> valueCountMap = new HashMap<String, Integer>();
        for(int index: availableValueIndexList){
            String value = resultList.get(index)==null?"":resultList.get(index).toString();
            if (valueCountMap.get(value)==null) valueCountMap.put(value, 0);
            valueCountMap.put(value, valueCountMap.get(value) + 1);
        }
        return generateEntropy(valueCountMap.values().toArray(new Integer[]{}));
    }

    private double generateEntropy(Integer... countArray){
        int totalCount = 0;
        for(int count: countArray) totalCount += count;
        double entropy = 0;
        for(int count: countArray) entropy += (-1.0*count/totalCount*Math.log(1.0*count/totalCount)/Math.log(2));
        return entropy;
    }

    /**
     * 计算熵值
     * @param yesCount 为true的count数
     * @param noCount 为false的count数
     * @return 熵值
     */
    private double entropy(int yesCount, int noCount){
        if (yesCount==0||noCount==0) return 0;
        int totalCount = yesCount+noCount;
        return -1.0*yesCount/totalCount*Math.log(1.0*yesCount/totalCount)/Math.log(2)-
                1.0*noCount/totalCount*Math.log(1.0*noCount/totalCount)/Math.log(2);
    }
    private double splitInformation(int chooseCount, int totalCount){
        if (chooseCount==0||totalCount==0) return 0;
        return -1.0*chooseCount/totalCount*(Math.log(1.0*chooseCount/totalCount)/Math.log(2));
    }
    // 指标对象，包含熵，值对象
    private static class QuotaEntropyEntity  {
        int quotaIndex;        //
        Map<String, QuotaValueEntity> valueMap = new TreeMap<String, QuotaValueEntity>();
        double entropy = 0;
        double gainInfo = 0;                   // 信息增益
        double gainInfoRatio ;                 // 信息增益率
        double splitInfo ;                     // 分离信息
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            str.append("").append(quotaIndex).append(" = ").append(entropy)
                    .append("|").append(gainInfo).append("|").append(splitInfo)
                    .append("|").append(gainInfoRatio)
                    .append("  Value data: ");
            for (String valueKey : valueMap.keySet()) {
                str.append("{").append(valueMap.get(valueKey)).append("}, ");
            }
            return str.toString();
        }
        // 指标值对象，包含当前指标下各个值得结果count数
        private static class QuotaValueEntity{
            String valueName;
            int[] valueCountArray = new int[]{0,0};
            List<Integer> avaialbeValueIndexList = new ArrayList<Integer>();
            double entropy ;         // 当前值时候的熵
            @Override
            public String toString(){
                StringBuilder str = new StringBuilder();
                str.append(valueName).append(":").append(Arrays.toString(valueCountArray)).append("|")
                        .append(entropy).append("|")
                        .append(Arrays.toString(avaialbeValueIndexList.toArray()));
                return str.toString();
            }
        }
    }
    // 决策树节点
    public static class DecisionTreeNode{
        public String prefixName;  // no used here
        public String nodeName;
        public Boolean trueOrFalse;
        public Map<String, DecisionTreeNode> subNodeMap = new HashMap<String, DecisionTreeNode>();
        public DecisionTreeNode(String prefixName, String nodeName){this.prefixName = prefixName; this.nodeName = "|-"+nodeName;}
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder();
            str.append(prefixName).append(nodeName);
            if (trueOrFalse != null) str.append(" = ").append(trueOrFalse);
            else{
                for (String key : subNodeMap.keySet()) {
                    str.append("\n").append(subNodeMap.get(key));
                }
            }
            return str.toString();
        }
    }
}
