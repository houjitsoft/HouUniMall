package com.houjiang.merchant.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Map工具类
 */
public class MapUtils {

    /**
     * JSON转 Map<String, String>
     *
     * @author Amos
     * @param json 对象
     * @return Map<String, String>
     */
    public static Map<String, String> jsonToMap(String json){
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        return JSON.parseObject(json, type);
    }

    /**
     * JSON转 Map<String, Object>
     *
     * @author Amos
     * @param json 对象
     * @return Map<String, Object>
     */
    public static Map<String, Object> jsonToMapAsObj(String json){
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        return JSON.parseObject(json, type);
    }

    /**
     * JSON转Map<String, String>
     *
     * @author Amos
     * @param object 对象
     * @return Map<String, String>
     */
    public static Map<String, String> objectToMap(Object object){
        Type type = new TypeToken<Map<String, String>>() {}.getType();
        return JSON.parseObject(JSONObject.toJSONString(object), type);
    }


    /**
     * 对象类型Map合并
     *
     * @author Amos
     * @param map 对象
     * @return Map<String, Object>
     */
    public static Map<String, Object> mergeMapByObj(Map<String, Object> map, Map<String, Object> map1){
        HashMap<String, Object> map2 = new HashMap<>();
        map2.putAll(map);
        map2.putAll(map1);
        return map2;
    }


    /**
     * 字符串类型Map合并
     *
     * @author Amos
     * @param map 对象
     * @return Map<String, String>
     */
    public static Map<String, String> mergeMapByStr(Map<String, String> map, Map<String, String> map1){
        HashMap<String, String> map2 = new HashMap<>();
        map2.putAll(map);
        map2.putAll(map1);
        return map2;
    }

    /**
     * 将查询字符串拆分为Map<String, Object>
     *
     * @param queryString 查询字符串，例如 "goods_sku_id=185&goods_num=1&add_type=1"
     * @return 包含键值对的Map
     */
    public static Map<String, Object> parseQueryStringToMap(String queryString) {
        Map<String, Object> resultMap = new HashMap<>();

        if (queryString == null || queryString.isEmpty()) {
            // 如果字符串为空，返回空Map
            return resultMap;
        }

        // 使用 '&' 分割字符串
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            // 使用 '=' 分割键值对
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                Object value = keyValue[1];
                // 将键值对放入Map中
                resultMap.put(key, value);
            }
        }

        return resultMap;
    }
}
