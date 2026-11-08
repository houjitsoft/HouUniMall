package com.houjiang.commons.util;

import com.alibaba.fastjson.JSONObject;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
public class ParseMapUtils {



    /**
     * 将Map转换为对象
     *
     * @param map
     * @param cls
     * @return
     */
    public static <T> T parseMap2Object(Map<String, Object> map, Class<T> cls) {
        return JSONObject.parseObject(JSONObject.toJSONString(map), cls);
    }

    /**
     * 将Map转换为对象
     *
     * @param map
     * @param cls
     * @return
     */
    public static <T> T parseMap2String(Map<String, String> map, Class<T> cls) {
        return JSONObject.parseObject(JSONObject.toJSONString(map), cls);
    }

    /**
     * 将Map转换为对象
     *
     * @param map
     * @param cls
     * @return
     */
    public static <T> T parseMap2Double(Map<String, Double> map, Class<T> cls) {
        return JSONObject.parseObject(JSONObject.toJSONString(map), cls);
    }


    /**
     * 对象转Map
     *
     * @param obj
     * @return
     * @throws IllegalAccessException
     */
    public static Map<String, Object> beanToMap(Object obj) throws IllegalAccessException {
        BeanMap beanMap = new BeanMap(obj);
        return convertBeanToMap(beanMap);
    }

    public static Map<String, Object> convertBeanToMap(Object bean) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();

        if (bean instanceof BeanMap beanMap) {
            for (Object key : beanMap.keySet()) {
                map.put((String) key, beanMap.get(key));
            }
        } else {
            Field[] fields = bean.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(bean));
            }
        }

        return map;
    }

    public static Map<String, String> convertWithBeanWrapper(UserInfoVO user) {
        BeanWrapper wrapper = new BeanWrapperImpl(user);
        PropertyDescriptor[] descriptors = wrapper.getPropertyDescriptors();
        Map<String, String> map = new HashMap<>();
        for (PropertyDescriptor descriptor : descriptors) {
            String name = descriptor.getName();
            // 跳过 class 属性
            if ("class".equals(name)) continue;
            Object value = wrapper.getPropertyValue(name);
            map.put(name, value != null ? value.toString() : "");
        }
        return map;
    }

}
