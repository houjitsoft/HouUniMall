package com.houjiang.commons.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @ClassName UUIDUtil
 * @Author Amos
 * @Date 2022/2/10 09:49
 * @Version 1.0
 * @author Amos
 */
public class UUIDUtil {
    public static String getUUID() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate = simpleDateFormat.format(date);
        return formattedDate + getRandom();
    }

    /** 创建一个6位随机数生成器 */
    public static String getRandom() {
        String sources = "0123456789";
        Random rand = new Random();
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < 6; j++) {
            sb.append(sources.charAt(rand.nextInt(9)));
        }
        return sb.toString();
    }

    /** 创建一个随机数生成器 */
    public static String getRandom(int num) {
        String sources = "0123456789";
        Random rand = new Random();
        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < num; j++) {
            sb.append(sources.charAt(rand.nextInt(9)));
        }
        return sb.toString();
    }

    public static String getUUID1() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss");
        String formattedDate = simpleDateFormat.format(date);
        return formattedDate + getRandom();
    }

}
