package com.houjiang.commons.util.secverifyapiutil;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;

/**
 * BaseUtils
 * Date: 2019/6/24
 * Time: 15:10
 *
 * @author Amos
 */
public class BaseUtils {

    public static boolean isEmpty(Object target) {
        if (target == null) {
            return true;
        }
        if (target instanceof String && target.equals("")) {
            return true;
        } else if (target instanceof Collection) {
            return ((Collection<?>) target).isEmpty();
        } else if (target instanceof Map) {
            return ((Map<?, ?>) target).isEmpty();
        } else if (target.getClass().isArray()) {
            return Array.getLength(target) == 0;
        }
        return false;
    }

    /** 解密 */
    public static String getFromBase64(String str) {
        if (str == null) {
            return null;
        }

        try {
            // 使用java.util.Base64进行解码
            byte[] decodedBytes = Base64.getDecoder().decode(str);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            // 捕获并处理非法参数异常
            e.printStackTrace();
            return null;
        }
    }

}