package com.hbhs.algorithm.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Bucket sort<br>
 * 桶排序 (Bucket sort)或所谓的箱排序，是一个排序算法，工作的原理是将数组分到有限数量的桶子里。每个桶子再个别排序（有可能再使用别的排序算法或是以递归方式继续使用桶排序进行排序）。桶排序是鸽巢排序的一种归纳结果。当要被排序的数组内的数值是均匀分配的时候，桶排序使用线性时间（Θ（n））。但桶排序并不是 比较排序，他不受到 O(n log n) 下限的影响。<br>
 * <br>执行流程如下<br>
 * 1 设置一个定量的数组当作空桶子。<bR>
 * 2 寻访串行，并且把项目一个一个放到对应的桶子去。<bR>
 * 3 对每个不是空的桶子进行排序。<bR>
 * 4 从不是空的桶子里把项目再放回原来的串行中。<bR>
 * @author walter.xu
 *
 */
public class BucketSort {

	/**
	 * 桶排序
	 * @param sequenceList
	 */
	public static void sort(List<Integer> sequenceList){
		// 1 求取sequence中获取到需要除以的值，返回值为10/100/1000/10000...
		int divisionValueOfSequence = getDivisionValueOfSequence(sequenceList);
		// 2 按照除数来计算
		sort(sequenceList, divisionValueOfSequence);
	}
    // 按照指定的数位来排序
	private static void sort(List<Integer> sequenceList, int divisionValue){
	    // 1 初始化待比较的桶数组, 类型为List<Integer>, 保存的为序列
		List<Integer>[] bucketArray = new List[10];

		// 2 迭代待排序序列，并放到对应的桶中
		for(int value: sequenceList){
			int index = value/divisionValue%10;  //通过该运算获取到我们需要排序的那一位数字，并放入到对应桶中
			if (bucketArray[index]==null) bucketArray[index] = new ArrayList<>();
			bucketArray[index].add(value);
		}
		// 4 迭代桶队列，对每一个桶中的数据做排序，我们这里还是采用桶排序(也可以采用其他的排序方式，如插入排序)
		if (divisionValue>1)
			for(List<Integer> bucketDataList: bucketArray){
				if (bucketDataList!=null&&bucketDataList.size()>0) sort(bucketDataList, divisionValue/10);
			}
		// 清楚数据，并按照bucket中的重新设置排序好的序列
		sequenceList.clear();
		for (List<Integer> list : bucketArray) {
			if (list!=null&&list.size()>0) sequenceList.addAll(list);
		}
	}

	private static int getDivisionValueOfSequence(List<Integer> sequenceArray){
		// 计算待排序中的最大值
		int max = sequenceArray.get(0);
		for(int target: sequenceArray){
			if (target>max) max = target;
		}
		// 求取第一次的除数，只会为10^n
		int divisionValue = 1;
		while(max>=10){
			divisionValue *= 10;
			max /= 10;
		}
		return divisionValue;
	}

}
