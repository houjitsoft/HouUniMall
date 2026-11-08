package com.houjiang.order.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @ClassName DateUtil
 * @Description
 * @Author Amos
 * @Date 2023/7/26 16:36
 * @Version 1.0
 * @author Amos
 */
public class DateUtil {

    /**
     * 格式化String时间
     *
     * @param timeFromat   String类型格式
     * @param defaultValue 异常时返回的默认值
     * @return
     */
    public static Date parseStrToDate(String timeFromat, Date defaultValue) {
        try {
            SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String dateStr = dfDate.format(date);
            DateFormat dateFormat = new SimpleDateFormat(timeFromat);
            return dateFormat.parse(dateStr);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 根据开始时间和结束时间判断是否间隔 time 小时或天
     *
     * @param beginTime
     * @param endTime
     * @param time
     * @param type
     * @return
     */
    public static Boolean intervalHour(Date beginTime, Date endTime, Integer time, String type) {
        long beginMillisecond = beginTime.getTime();
        long endMillisecond = endTime.getTime();
        long timeHaoMia = 3600000L;
        if (type.equals("h")) {
            timeHaoMia = timeHaoMia * time;
        } else if (type.equals("d")) {
            timeHaoMia = timeHaoMia * 24 * time;
        }
        if (endMillisecond - beginMillisecond >= timeHaoMia) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取月初时间
     *
     * @param date
     * @return
     */
    public static Date dateBeginMouth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        /**
         1. getActualMinimum(Calendar.DAY_OF_MONTH)是拿到当月最小天数作为第一天

         2. calendar.set(Calendar.DAY_OF_MONTH, 1)也能获取当月第一天
         */
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    /**
     * 判断是否在同一个月
     *
     * @return false:不在同一个月内，true在同一个月内
     */
    public static boolean isMonth(Date date1, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        int year1 = calendar1.get(Calendar.YEAR);
        int year2 = calendar2.get(Calendar.YEAR);
        int month1 = calendar1.get(Calendar.MONTH);
        int month2 = calendar2.get(Calendar.MONTH);
        System.out.println(year1 + "  " + month1);
        System.out.println(year2 + "  " + month2);
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    }

}
