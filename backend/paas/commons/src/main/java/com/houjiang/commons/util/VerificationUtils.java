package com.houjiang.commons.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @PROJECT_NAME: UniMall
 * @DESCRIPTION:
 * @USER: Amos
 * @DATE: 2023/2/25 20:08
 * @author Amos
 */

public class VerificationUtils {

    private static Pattern patternDigit = Pattern.compile(".*\\d+.*");
    private static Pattern patternContainsLetter = Pattern.compile(".*[a-zA-Z]+.*");
    private static Pattern patternPhone = Pattern.compile("^1[0-9]{10}$");

    /** 判断一个字符串是否含有数字 */
    public boolean hasDigit(String content) {
        boolean flag = false;
        Matcher m = patternDigit.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }

    /** 判断字符串中是否包含字母 **/
    public static boolean isContainsLetter(String input){
        boolean flag = false;
        Matcher matcher = patternContainsLetter.matcher(input);
        if(matcher.matches()){
            flag = true;
        }
        return flag;
    }

    /**
     * 校验手机号
     **/
    public static boolean isPhonePattern(String input) {
        boolean flag = false;
        Matcher matcher = patternPhone.matcher(input);
        if (matcher.matches()) {
            flag = true;
        }
        return flag;
    }

    /**
     * 校验是否为纯数字
     *
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        String reg = "^[0-9]+(.[0-9]+)?$";
        return str.matches(reg);
    }
}
