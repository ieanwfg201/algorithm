package com.hbhs.algorithm.classify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by walter.xu on 2017/1/22.
 * ID3����������� {@link DecisionTreeID3}
 * ��������е�ĳһ���ֶ�Ϊ�������һ�㶨����Ϊboolean���͵��ֶ�
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DecisionTreeID3QuotaResult {
}
