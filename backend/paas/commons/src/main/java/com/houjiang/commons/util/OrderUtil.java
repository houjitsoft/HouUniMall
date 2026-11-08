package com.houjiang.commons.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author Amos
 */
public class OrderUtil {
    /**
     * 生成订单编号
     */


    public static String getOrderNo() {
        SimpleDateFormat dmDate = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return "WPS" + dmDate.format(date) + getRandom(8);
    }

    public static String getRandom(int len) {
        Random r = new Random();
        StringBuilder rs = new StringBuilder();
        for (int i = 0; i < len; i++) {
            rs.append(r.nextInt(10));
        }
        return rs.toString();
    }
}
