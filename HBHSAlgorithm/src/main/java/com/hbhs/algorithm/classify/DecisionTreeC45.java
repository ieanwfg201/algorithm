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
                else result = true;   // ��������� null
            }
            resultList.add(result);
        }
    }

    /**
     * �������������Լ�����������ɾ�����-ID3
     * @param quotaValuesList ����ָ��ֵ�б�list: �����б� string[]��ָ���б�
     * @param resultList ����������Ӧ��ֵ
     * @return ��������Ϣ
     */
    public DecisionTreeNode generateDecisionTree(List<String[]> quotaValuesList, List<Boolean> resultList){
        if (quotaValuesList==null||quotaValuesList.size()==0||resultList==null||resultList.size()==0) return null;
        // ��ʼ���������ṹ
        root = new DecisionTreeNode("", "ROOT");
        // ��ʼ����ʹ�õ�ָ���б�������б�
        List<Integer> availableQuotaIndexList = new ArrayList<Integer>();
        List<Integer> availableValueIndexList = new ArrayList<Integer>();
        int index = 0;
        while(index<quotaValuesList.get(0).length) availableQuotaIndexList.add(index++);
        index = 0;
        while(index<quotaValuesList.size()) availableValueIndexList.add(index++);
        // ���ɾ�����
        this.generateDecisionTree(root, quotaValuesList, resultList, availableQuotaIndexList, availableValueIndexList);
        return root;
    }

    /**
     * �������������Լ�����������ɾ�����-ID3
     * @param node ��ǰ�����������ɽڵ�
     * @param quotaValuesList ����ָ��ֵ�б�list: �����б� string[]��ָ���б�
     * @param resultList ����������Ӧ��ֵ
     * @param availableQuotaIndexList ָ����Ϣ�б�
     * @param availableValueIndexList ������Ϣ�б�
     */
    private void generateDecisionTree(DecisionTreeNode node, List<String[]> quotaValuesList, List<Boolean> resultList,
                                      List<Integer> availableQuotaIndexList, List<Integer> availableValueIndexList){
        double totalChooseEntropy = this.generateTopEntropy(resultList, availableValueIndexList);
        // 1. �������ǰ����ָ�꣬���������µ�����ָ��(�����ؽ������-������С)
        QuotaEntropyEntity selectQuota = calculateAndSelectQuotaByMinEntropy(quotaValuesList, resultList, availableQuotaIndexList, availableValueIndexList, totalChooseEntropy);
        if (selectQuota == null) return ;
        // 2. ����ָ����Ϊ��ǰ�������µ��ӽڵ㣬���������
        for (String valueKey : selectQuota.valueMap.keySet()) {
            DecisionTreeNode subNode = new DecisionTreeNode(node.prefixName+"    ", selectQuota.quotaIndex+" - "+valueKey);
            QuotaEntropyEntity.QuotaValueEntity entity = selectQuota.valueMap.get(valueKey);
            // �����ǰָ�굱ǰֵ�£���������ָ��Ϊ�Σ����ǽ����һ�£����ʾ��������ָ������£����ս�����Ѿ�ȷ�������������������ָ�������
            if (entity.valueCountArray[0] == 0||entity.valueCountArray[1]==0){
                subNode.trueOrFalse = entity.valueCountArray[0] != 0;
            }else{
                // ��Ҫ������ָ��������֣������ٵ�ǰ�ӽڵ��¹��������
                // ������õ�ָ���б�������ǰָ��
                List<Integer> availableQuotaIndexListCopy = this.copyList(availableQuotaIndexList);
                availableQuotaIndexListCopy.remove(selectQuota.quotaIndex);

                // ���ʣ��ָ�겻Ϊ�գ������¹���
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
     * �����ؼ������Ż�ָ�꣬����ȡ����ָ����Ϣ
     * @param quotaValuesList  ����ָ��ֵ�б�list: �����б� string[]��ָ���б�
     * @param resultList ����������Ӧ��ֵ
     * @param availableQuotaIndexList ָ����Ϣ�б�
     * @param availableValueIndexList ������Ϣ�б�
     * @return ���Ż���ָ����Ϣ
     */
    public QuotaEntropyEntity calculateAndSelectQuotaByMinEntropy(List<String[]> quotaValuesList, List<Boolean> resultList,
                                                                  List<Integer> availableQuotaIndexList, List<Integer> availableValueIndexList, double parentEntropy){
        if (availableValueIndexList.size()==0||availableQuotaIndexList.size()==0) return null;

        Map<Integer, QuotaEntropyEntity> quotaEntropyMap = this.generateQuotaMap(quotaValuesList, resultList, availableQuotaIndexList, availableValueIndexList);
        // �����ָ��Ķ�����
        this.calculateQuotaEntropyAndInfoGain(quotaEntropyMap, availableValueIndexList.size(), parentEntropy);
        // ��ӡ��Ϣ
        this.printQuotaEntropyMap(quotaEntropyMap);
        // ������С�ص�ָ��
        return this.findMaxGainInfoRatioOfQuotaEntity(quotaEntropyMap);
    }

    /**
     * ���ɵ�ǰָ�꣬��ǰ�����¶�Ӧ��ָ������Ϣ�б� ��ǰ����²�δ��������Ϣ
     * @param quotaValuesList  ����ָ��ֵ�б�list: �����б� string[]��ָ���б�
     * @param resultList ����������Ӧ��ֵ
     * @param availableQuotaIndexList ָ����Ϣ�б�
     * @param availableValueIndexList ������Ϣ�б�
     * @return key: ָ��index��value:ָ���Ӧ����Ϣ
     */
    private Map<Integer, QuotaEntropyEntity> generateQuotaMap(List<String[]> quotaValuesList, List<Boolean> resultList,
                                                              List<Integer> availableQuotaIndexList, List<Integer> availableValueIndexList){
        Map<Integer, QuotaEntropyEntity> quotaEntropyMap = new TreeMap<Integer, QuotaEntropyEntity>();
//        Map<Integer, Map<String, int[]>> quotaMap = new TreeMap<Integer, Map<String, int[]>>();
        for (Integer valueIndex : availableValueIndexList) {
            String[] quotaArray = quotaValuesList.get(valueIndex);
            for (Integer quotaIndex : availableQuotaIndexList) {

                QuotaEntropyEntity quotaEntropy = quotaEntropyMap.get(quotaIndex);
                // ��������ڣ����ʼ��QuotaEntropyEntity����
                if (quotaEntropy==null){ quotaEntropy = new QuotaEntropyEntity(); quotaEntropy.quotaIndex = quotaIndex; quotaEntropyMap.put(quotaIndex,quotaEntropy); }

                QuotaEntropyEntity.QuotaValueEntity valueEntity = quotaEntropy.valueMap.get(quotaArray[quotaIndex]);
                // ���ValueCount�����ڣ����ʼ��valueCount map
                if (valueEntity==null){
                    valueEntity = new QuotaEntropyEntity.QuotaValueEntity();
                    valueEntity.valueName = quotaArray[quotaIndex];
                    quotaEntropy.valueMap.put(quotaArray[quotaIndex], valueEntity);
                }
                valueEntity.avaialbeValueIndexList.add(valueIndex);
                // ��count���������������������£�ѡ���ָ���ʱ����ߵ�����ֵ
                if (resultList.get(valueIndex)) valueEntity.valueCountArray[0] = valueEntity.valueCountArray[0]+1;
                else valueEntity.valueCountArray[1] = valueEntity.valueCountArray[1]+1;
            }
        }
        return quotaEntropyMap;
    }

    /**
     * �������ָ���entropy ��ֵ�Լ�gainInfo
     * @param quotaEntropyMap ָ��map����
     * @param totalSize �����ܴ�С
     */
    private void calculateQuotaEntropyAndInfoGain(Map<Integer, QuotaEntropyEntity> quotaEntropyMap, int totalSize, double parentEntropy){
        for (Integer quotaIndex : quotaEntropyMap.keySet()) {
            Map<String, QuotaEntropyEntity.QuotaValueEntity> valueCountMap = quotaEntropyMap.get(quotaIndex).valueMap;
            // �ܵ�entrpy = ռ��*��entropy+ռ��*��entropy+...
            for (String valueKey : valueCountMap.keySet()) {
                // ����value���Ե�entropy
                int[] count = valueCountMap.get(valueKey).valueCountArray;
                valueCountMap.get(valueKey).entropy = entropy(count[0], count[1])*(count[0]+count[1])/totalSize;
                // ���㵱ǰ�ܵ�entropy
                quotaEntropyMap.get(quotaIndex).entropy += valueCountMap.get(valueKey).entropy;
                // ����split information
                quotaEntropyMap.get(quotaIndex).splitInfo += splitInformation(count[0]+count[1], totalSize);
            }
            // ��Ϣ���� = ��ѡ��ڵ��entropy-��ǰѡ���entropy
            quotaEntropyMap.get(quotaIndex).gainInfo = parentEntropy-quotaEntropyMap.get(quotaIndex).entropy;
            // ��Ϣ������ = ��Ϣ����/������Ϣ
            quotaEntropyMap.get(quotaIndex).gainInfoRatio = quotaEntropyMap.get(quotaIndex).gainInfo/quotaEntropyMap.get(quotaIndex).splitInfo;
        }
    }

    /**
     * ��ѯ��С��ֵ��һ��ָ��, ��Ҫ��{@link #calculateQuotaEntropyAndInfoGain(Map, int, double)} ����ִ�к���Ч
     * @param quotaEntropyMap ָ��map����
     * @return ��С��ֵ��ָ�����
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
     * ������ֵ
     * @param yesCount Ϊtrue��count��
     * @param noCount Ϊfalse��count��
     * @return ��ֵ
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
    // ָ����󣬰����أ�ֵ����
    private static class QuotaEntropyEntity  {
        int quotaIndex;        //
        Map<String, QuotaValueEntity> valueMap = new TreeMap<String, QuotaValueEntity>();
        double entropy = 0;
        double gainInfo = 0;                   // ��Ϣ����
        double gainInfoRatio ;                 // ��Ϣ������
        double splitInfo ;                     // ������Ϣ
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
        // ָ��ֵ���󣬰�����ǰָ���¸���ֵ�ý��count��
        private static class QuotaValueEntity{
            String valueName;
            int[] valueCountArray = new int[]{0,0};
            List<Integer> avaialbeValueIndexList = new ArrayList<Integer>();
            double entropy ;         // ��ǰֵʱ�����
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
    // �������ڵ�
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
