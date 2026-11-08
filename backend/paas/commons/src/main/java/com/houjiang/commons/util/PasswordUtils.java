package com.houjiang.commons.util;

/**
 * @author Amos
 */
public class PasswordUtils {

    private static final HouBCryptPasswordEncoder ENCODER = new HouBCryptPasswordEncoder();

    /**
     * BCryptPasswordEncoder加密
     */
    public static String BCryptPasswordEncoder(String str) {
        return ENCODER.encode(str);
    }

    /**
     * BCryptPasswordEncoder比较（兼容 $2y$ / $2b$ 哈希）
     */
    public static Boolean BCryptPasswordMatches(String str1, String str2) {
        return ENCODER.matches(str1, str2);
    }
}
