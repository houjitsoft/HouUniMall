package com.houjiang.commons.util;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Amos
 */
public class CheckPhoneUtil {
    /** 定义邮箱的正则表达式 */
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final String pattern = "^((1[2,3,4,5,7,6,8,9][0-9]))\\d{8}$";

    public static Boolean checkPhone(String phone) {
        //长度和不为空
        if (StringUtils.isEmpty(phone)){
            return false;
        }
        if (phone.trim().length() != 11){
            return false;
        }
        return Pattern.matches(pattern, phone);
    }
    /**
     * 验证字符串是否是有效的邮箱地址
     *
     * @param email 要验证的字符串
     * @return 如果是有效的邮箱地址，返回 true；否则返回 false
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }
}
