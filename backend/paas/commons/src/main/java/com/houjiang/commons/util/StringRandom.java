package com.houjiang.commons.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author Amos
 */
public class StringRandom {
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Date cardNumId = null;
    private static Random random = new Random();

    /** 生成随机数字和字母, */
    public static String getStringRandom(int length) {

        String val = "";

        // length为几位密码
        for (int i = 0; i < length; i++) {
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            // 输出字母还是数字
            if ("char".equalsIgnoreCase(charOrNum)) {
                // 输出是大写字母还是小写字母
                //int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
                int temp = random.nextInt(26) + 97;
                if (temp == 108) {
                    temp = temp + 1;
                }
                val += (char) temp;
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                val += String.valueOf(random.nextInt(10));
            }
        }
        return val;
    }

    /*
/**
     * 生成随机数
     */
    public static String getStringNum() {
        Date date = new Date();

        String str = format.format(date);
        int x = random.nextInt(9000) + 1000;
        try {
            cardNumId = format.parse(str);
        } catch (ParseException e) {
            // str 是同一个 format 刚格式化出来的，走不到这里；
            // 真走到了说明格式串被改过，早期只 printStackTrace 然后继续用 null 的 cardNumId，后面必然 NPE
            throw new IllegalStateException("时间戳解析失败，无法确定卡号/时间戳基准", e);
        }

        // 随机卡号
        //String  cardNum= (x + String.valueOf(cardNumId.getTime()).substring(2, 10));
        String cardNum = x + "";
        return cardNum;
    }

    /** 时间戳 */
    public static int getIntNum() {
        Date date = new Date();

        String str = format.format(date);
        try {
            cardNumId = format.parse(str);
        } catch (ParseException e) {
            // str 是同一个 format 刚格式化出来的，走不到这里；
            // 真走到了说明格式串被改过，早期只 printStackTrace 然后继续用 null 的 cardNumId，后面必然 NPE
            throw new IllegalStateException("时间戳解析失败，无法确定卡号/时间戳基准", e);
        }
        // 时间戳
        int creatTime = (int) (cardNumId.getTime() / 1000);
        return creatTime;
    }

}
