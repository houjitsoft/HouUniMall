package com.houjiang.merchant.common.enums;

import com.alibaba.fastjson2.JSONArray;

import java.util.*;
import java.util.stream.IntStream;

/**
 * @author Amos
 */
public class OrderSettingEnum {

    public final static Integer SETTLEMENT_CYCLE_DAY = 0;

    public final static Integer SETTLEMENT_CYCLE_WEEK = 1;

    public final static Integer SETTLEMENT_CYCLE_MONTH = 2;


    public final static List<Integer> SETTLEMENT_CYCLE_ARR = new ArrayList() {{
        add(SETTLEMENT_CYCLE_DAY);
        add(SETTLEMENT_CYCLE_WEEK);
        add(SETTLEMENT_CYCLE_MONTH);
    }};

    public final static Map<Integer, String> SETTLEMENT_CYCLE_TEXT_ARR = new HashMap() {{
        put(SETTLEMENT_CYCLE_DAY, "日结");
        put(SETTLEMENT_CYCLE_WEEK, "周结");
        put(SETTLEMENT_CYCLE_MONTH, "月结");
    }};


    public final static JSONArray SETTLEMENT_CYCLE_WEEK_DAYS = new JSONArray() {{
        add(1);
        add(2);
        add(3);
        add(4);
        add(5);
        add(6);
        add(7);
    }};


    public final static Map<Integer, String> SETTLEMENT_CYCLE_WEEK_TEXT_DAYS = new HashMap() {{
        put(1, "星期一");
        put(2, "星期二");
        put(3, "星期三");
        put(4, "星期四");
        put(5, "星期五");
        put(6, "星期六");
        put(7, "星期日");

    }};


    public static int[] getMonthTextDays() {
        return Arrays.copyOfRange(IntStream.range(0, 29).toArray(), 1, 29);
    }
}
