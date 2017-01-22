package com.hbhs.algorithm.classify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by walter.xu on 2017/1/22.
 * ID3决策树相关类 {@link DecisionTreeID3}
 * 定义对象中的某一个字段为结果集，一般定义在为boolean类型的字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DecisionTreeID3QuotaResult {
}
