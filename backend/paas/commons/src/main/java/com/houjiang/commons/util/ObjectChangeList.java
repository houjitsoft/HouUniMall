package com.houjiang.commons.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Amos
 */
public class ObjectChangeList {
    /**
     * 将Object转换成List类型（如果不是List<?>对象，不能转换）
     *
     * @param obj   Object对象
     * @param clazz 数据类型
     * @param <T>   泛型类型
     * @return 转换结果
     */
    public static <T> List<T> castObjToList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                // 根据业务需要来决定这里要不要将null值还原放进去
                if (o == null) {
                    result.add(clazz.cast(null));
                    continue;
                }
                // 转换前的前置判断，避免clazz.cast的时候出现类转换异常
                if (o.getClass().equals(clazz)) {
                    // 将对应的元素进行类型转换
                    result.add(clazz.cast(o));
                }
            }
            return result;
        }
        return result;
    }
}
