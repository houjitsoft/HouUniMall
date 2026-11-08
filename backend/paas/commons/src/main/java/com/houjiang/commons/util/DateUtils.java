package com.houjiang.commons.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DateUtils extends org.apache.commons.lang.time.DateUtils {

    public static final Logger logger= LoggerFactory.getLogger(DateUtils.class);

    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static final String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 生成单号
     *
     * @return
     */
    public static String getTimeStamp() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        int random = (int) (Math.random() * 9000 + 1000);
        String timestamp = dateString + random;
        return timestamp;

    }

    /**
     * 获取当前日期加随机六位数的字符串
     *
     * @return
     */
    public static String getDateRandomSix() {
        String random = (int) ((Math.random() * 9 + 1) * 100000) + "";
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMdd");
        String sDateSuffix = dateformat.format(new Date());
        return sDateSuffix + random;
    }

    /**
     * 随机获取两位数
     *
     * @return
     */
    public static int randomLengthByTwo() {
        int random = (int) (Math.random() * 90 + 10);
        return random;
    }

    /**
     * 获取当前时间时间戳（后九位）
     *
     * @return
     */
    public static String nowtime() {
        int seconds = (int) (System.currentTimeMillis() / 1000);
        String str = String.valueOf(seconds);
        String time = str.substring(1, 10);
        return time;
    }


    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    public static String getNowDay(){
        return DateUtils.getYear() + "-" + DateUtils.getMonth() + "-" + DateUtils.getDay();
    }


    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDateToDf() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        Date date = null;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    /**
     * 获取当前时间
     *
     * @return
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public static String getNowDateToString() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }


    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getCNDate() {
        return parseDateToStr("YYYY年MM月DD", new Date());
    }


    public static String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    public static String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static String parseDateToStr(final String format, final Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    public static Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = Long.valueOf(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 获取两个日期相差的秒数.
     *
     * @param start
     * @param end
     * @return
     */
    public static int getIntervalSeconds(Date start, Date end) {
        long s = start.getTime() / 1000;
        long e = end.getTime() / 1000;
        return (int) (e - s);
    }

    /**
     * 获得当前时间戳，精确到毫秒！
     *
     * @return
     * @author Amos
     * @time 2015-10-14 23:17
     */
    public static String getTimeMark() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int mouth = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int haomiao = c.get(Calendar.MILLISECOND);
        return "" + year + mouth + day + hour + minute + second + haomiao;
    }

    /**
     * 获得当前时间戳，精确到毫秒！(不要年份)
     *
     * @return
     * @author Amos
     * @time 2015-10-14 23:17
     */
    public static String getTimeMark1() {
        Calendar c = Calendar.getInstance();
        int mouth = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int haomiao = c.get(Calendar.MILLISECOND);
        return "" + mouth + day + hour + minute + second + haomiao;
    }

    /**
     * 获取距离当前时间后的一天日期对象
     *
     * @param today
     * @return
     */
    public static Date getNextDay(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    /**
     * 获取距离当前时间前的一天日期对象
     *
     * @param today
     * @return
     */
    public static Date getPrevDay(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }


    /**
     * 获取距离当前时间后的day天日期对象
     *
     * @param today
     * @return
     */
    public static Date addDays(Date today, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    /**
     * 输入日期字符串返回3天后的时间
     *
     * @param strDate
     * @return
     */
    public static String strToDateLong(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date date = formatter.parse(strDate, pos);
        date.setTime(date.getTime() + 3 * 24 * 60 * 60 * 1000);
        String strToDate = formatter.format(date);
        return strToDate;
    }
    /**
     * 是否超时（一天期限）
     *
     * @param expired
     * @return
     */
    public static boolean isExpired(Date expired) {
        return !isNotExpired(expired);
    }

    /**
     * 是否超时（一天期限）
     *
     * @param expired
     * @return
     */
    public static boolean isNotExpired(Date expired) {
        long now = System.currentTimeMillis();
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return expired == null || (expired.getTime() - now) > 0;
    }


    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static String dateTime() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }


    /**
     * 获取当前年份
     */
    public static String getSysYear() {
        Calendar date = Calendar.getInstance();
        String year = String.valueOf(date.get(Calendar.YEAR));
        return year;
    }

    /**
     * 获取年份
     *
     * @return
     */
    public static String getYear() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        String year = formatter.format(currentTime);
        return year;

    }

    /**
     * 获取月份
     *
     * @return
     */
    public static String getMonth() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM");
        String month = formatter.format(currentTime);
        return month;

    }

    /**
     * 获取日期
     *
     * @return
     */
    public static String getDay() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd");
        String day = formatter.format(currentTime);
        return day;

    }


    /**
     * 将字符串日期转换为日期格式
     *
     *
     * @param datestr
     * @return
     *
     */
    public static Date stringToTime(String datestr) {

        if (datestr == null || datestr.equals("")) {
            return null;
        }
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);
        try {
            date = df.parse(datestr);
        } catch (ParseException e) {
            date = DateUtils.stringToDate(datestr, "yyyyMMdd");
        }
        return date;
    }

    /**
     * 将字符串日期转换为日期格式 自定義格式
     *
     * @param datestr
     * @return
     *
     */
    public static Date stringToDate(String datestr, String dateformat) {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(dateformat);
        try {
            date = df.parse(datestr);
        } catch (ParseException e) {
        }
        return date;
    }

    /**
     * 格式化指定时间.
     *
     * @param date
     * @return
     */
    public static String formate(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
            return SDF.format(date);
        }
    }

    public static String formateYear(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF = new SimpleDateFormat("yyyy");
            return SDF.format(date);
        }
    }

    public static String formateYearAndMonth(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF = new SimpleDateFormat("yyyyMM");
            return SDF.format(date);
        }
    }

    public static String formateYearAndDay(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd");
            return SDF.format(date);
        }
    }


    public static String formateMonth(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF = new SimpleDateFormat("MM");
            return SDF.format(date);
        }
    }

    public static String formate1(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF = new SimpleDateFormat("yy-MM-dd");
            return SDF.format(date);
        }
    }

    public static String formate2(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF = new SimpleDateFormat("MM-dd");
            return SDF.format(date);
        }
    }

    public static String formate3(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM");
            return SDF.format(date);
        }
    }

    /**
     * 格式化指定时间.
     *
     * @param date
     * @return 返回时间格式为：yyyy-MM-dd HH:mm:ss
     */
    public static String formateForSdfCn(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF_CN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return SDF_CN.format(date);
        }
    }

    /**
     * 格式化指定时间.
     *
     * @param date
     * @return 返回时间格式为：yyyy-MM-dd HH:mm:ss
     */
    public static String formateForSdfCnT(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF_CN = new SimpleDateFormat("HH:mm:ss");
            return SDF_CN.format(date);
        }
    }

    /**
     * 格式化指定时间.
     *
     * @param date
     * @return 返回时间格式为：yyyy-MM-dd HH:mm:ss
     */
    public static String formateForSdfCnN(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF_CN = new SimpleDateFormat("yyyy年MM月dd日");
            return SDF_CN.format(date);
        }
    }

    /**
     * 格式化指定时间.
     *
     * @param date
     * @return
     */
    public static String formateForSdfCt(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF_CT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            return SDF_CT.format(date);
        }
    }

    /**
     * 格式化指定时间.
     *
     * @param date
     * @return
     */
    public static String formateForSdfCt1(Date date) {
        if (date == null) {
            return "";
        } else {
            SimpleDateFormat SDF_CT1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            return SDF_CT1.format(date);
        }
    }

    /**
     * 得到两个日期间的间隔天数
     *
     * @param start
     * @param end
     * @return
     */
    public static long getBetweenDays(Date start, Date end) {
        long day = (end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000);
        return day;
    }

    /**
     * 添加小时.
     *
     * @param date
     * @param amount
     * @return
     */
    public static Date addHours(Date date, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.HOUR_OF_DAY, amount);
        return c.getTime();
    }

    public static String format(Date date, String format) {
        if(date == null){
            return "";
        }
        SimpleDateFormat SDF = new SimpleDateFormat(format);
        return SDF.format(date);
    }

    /**
     * 获取当前时间戳精确到毫秒
     *
     * @param flag 是否加随机数 (例： flag = true 201703221337259833313249 flag = false
     *             20170322133725983 )
     * @return
     */
    public static String getTimestamp(boolean flag) {
        String timeStamp = "";
        Calendar cal = Calendar.getInstance();
        timeStamp += cal.get(Calendar.YEAR);
        timeStamp += getSplicezero(cal.get(Calendar.MONTH) + 1);
        timeStamp += getSplicezero(cal.get(Calendar.DAY_OF_MONTH));
        timeStamp += getSplicezero(cal.get(Calendar.HOUR_OF_DAY));
        timeStamp += getSplicezero(cal.get(Calendar.MINUTE));
        timeStamp += getSplicezero(cal.get(Calendar.SECOND));
        timeStamp += getSplicezero(cal.get(Calendar.MILLISECOND));
        if (flag) {

            try {
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                timeStamp += (int) ((Math.random() * 9 + 1) * 1000000);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

        }
        return timeStamp;
    }

    /**
     * 数值不够两位时在数值前面补0
     *
     * @param num
     * @return
     */
    public static String getSplicezero(int num) {
        String str = "";
        if (num < 10) {
            str += "0" + num;
        } else {
            str += num;
        }
        return str;
    }


    /**
     * 获取当天起始时间戳（0点）
     *
     * @return
     */
    public static Timestamp getDayBegin() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTimeInMillis());
    }

    /**
     * 获取当天结束时间戳（24点）
     *
     * @return
     */
    public static Long getDayEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime().getTime();
    }

    /**
     * 获取当天结束时间（24点）
     *
     * @return
     */
    public static String getDayStartTime2() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 0);
        todayEnd.set(Calendar.MINUTE, 0);
        todayEnd.set(Calendar.SECOND, 0);
        todayEnd.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat df = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);
        return df.format(todayEnd.getTime());
    }

    /**
     * 获取当天结束时间（24点）
     *
     * @return
     */
    public static String getDayEndTime2() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        SimpleDateFormat df = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);
        return df.format(todayEnd.getTime());
    }

    /***
     *
/**
     * 方法说明：字符串转Date 方法名称：parseCNDate
     *
     * @param source
     * @return 返回值：Date
     */
    public static Date parseCNDate(String source) {
        try {
            SimpleDateFormat SDF_CN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return SDF_CN.parse(source);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /***
     *
/**
     * 方法说明：字符串转Date 方法名称：parseCN2Date
     *
     * @param source
     * @return 返回值：Date
     */
    public static Date parseCN2DateDay(String source) {
        try {
            SimpleDateFormat SDF_CN2 = new SimpleDateFormat("yyyy/MM/dd");
            return SDF_CN2.parse(source);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /***
     *
/**
     * 方法说明：字符串转Date 方法名称：parseCN2Date
     *
     * @param source
     * @return 返回值：Date
     */
    public static Date parseCN2Date(String source) {
        try {
            SimpleDateFormat SDF_CN2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return SDF_CN2.parse(source);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /***
     *
/**
     * 方法说明：字符串转Date 方法名称：parseDateSDF
     *
     * @param source
     * @return 返回值：Date
     */
    public static Date parseDateSDF(String source) {
        try {
            SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
            return SDF.parse(source);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /***
     *
/**
     * 方法说明：字符串转Date 方法名称：parseDateSDF
     *
     * @param source
     * @return 返回值：Date
     */
    public static Date parseDateSDFToMonth(String source) {
        try {
            SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM");
            return SDF.parse(source);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * @param date1
     * @param date2
     * @return
     * @Description 展示剩余时间:hh:mm:ss
     */
    public static String getRemainTime(Date date1, Date date2) {
        long date = date2.getTime() - date1.getTime();
        long day = date / (1000 * 60 * 60 * 24);
        long hour1 = (date / (1000 * 60 * 60));
        long hour = (date / (1000 * 60 * 60) - day * 24);
        long min = ((date / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long s = (date / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        return hour1 + ":" + min + ":" + s;
    }

    /**
     * 获取距离当前时间前的一天日期对象
     *
     * @param today
     * @return
     */
    public static Date getPrevHour(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.HOUR, -1);
        return calendar.getTime();
    }

    /**
     * 根据当前月份往后推一个月
     *
     * @param today
     * @return
     */
    public static Date getPushBack(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 根据当前月份往前推一个月一天
     *
     * @param today
     * @return
     */
    public static Date getPerMonth(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 根据当前月份往前推一个月
     *
     * @param today
     * @return
     */
    public static Date getPerMonth2(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 根据当前月份往前推一天
     *
     * @param today
     * @return
     */
    public static Date getPerDay(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    /**
     * 根据当前月份往前推七天
     *
     * @param today
     * @return
     */
    public static Date getPerDayBySeven(Date today) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }


    /**
     * 比较两个日期月份是否相同
     *
     * @param d1
     * @param d2
     * @return
     */
    public static boolean sameDate(Date d1, Date d2) {
        if (null == d1 || null == d2) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        cal1.set(Calendar.DATE, 1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);
        cal2.set(Calendar.DATE, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);
        return cal1.getTime().equals(cal2.getTime());
    }

    /***
     *
/**
     * 方法说明：字符串转Date 方法名称：parseCNDate
     *
     * @param source
     *            20171123231504000 或 20171123231504000+0800
     * @return 返回值：Date
     */
    public static Date parseSDFORDERDate(String source) {
        SimpleDateFormat SDF_ORDER_6 = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
        try {
            Date date = null;
            if (source.indexOf("+") > 0) {
                date = SDF_ORDER_6.parse(source);
            } else {
                SimpleDateFormat SDF_ORDER_4 = new SimpleDateFormat("yyyyMMddHHmmss");
                date = SDF_ORDER_4.parse(source);
            }
            return date;
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * 返回指定日期的日历，并设置时间间隔
     *
     * @param date
     * @param beforeDays
     * @param intervalTime
     * @return
     */
    public static List<String> getDatePeriod(Date date, int beforeDays, int intervalTime) {
        List<String> datePeriodList = new ArrayList<String>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int inputDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        for (int i = beforeDays - intervalTime; i >= 0; i = i - intervalTime) {
            cal.set(Calendar.DAY_OF_YEAR, inputDayOfYear - i);
            datePeriodList.add(dateFormat.format(cal.getTime()));
        }
        return datePeriodList;
    }

    /**
     * @param start
     * @param end
     * @return
     * @Description 获取两个时间相减的月份
     */
    public static int getMonthNum(Date start, Date end) {
        if (start.after(end)) {
            Date t = start;
            start = end;
            end = t;
        }
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);
        Calendar temp = Calendar.getInstance();
        temp.setTime(end);
        temp.add(Calendar.DATE, 1);

        int year = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
        int month = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

        if ((startCalendar.get(Calendar.DATE) == 1) && (temp.get(Calendar.DATE) == 1)) {
            return year * 12 + month + 1;
        } else if ((startCalendar.get(Calendar.DATE) != 1) && (temp.get(Calendar.DATE) == 1)) {
            return year * 12 + month;
        } else if ((startCalendar.get(Calendar.DATE) == 1) && (temp.get(Calendar.DATE) != 1)) {
            return year * 12 + month;
        } else {
            return (year * 12 + month - 1) < 0 ? 0 : (year * 12 + month);
        }
    }


    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(String str,String fmt) {
        if (str == null) {
            return null;
        }

        return parseDate(str, fmt);

    }

    /***
     *
/**
     * 方法说明：字符串转Date 方法名称：parseDateSDF
     *
     * @param source
     * @return 返回值：Date
     */
    public static Date parseDateSDF2(String source) {
        try {
            SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd");
            return SDF.parse(source);
        } catch (ParseException e) {
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * 将时间设置为当天的开始时间（00:00:00）
     *
     * @param date 日期字符串，格式为"yyyy-MM-dd"
     * @return 日期的开始时间
     */
    public static Date parseDateStartTime(Date date) {
        // 转换为LocalDateTime
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // 设置时间为当天的开始时间
        LocalDateTime startOfDay = localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);

        // 转换回Date类型
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将日期设置为当天的结束时间（23:59:59）
     *
     * @param date 日期字符串，格式为"yyyy-MM-dd"
     * @return 日期的结束时间
     */
    public static Date parseDateEndTime(Date date) {
        // 将Date转换为LocalDateTime
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // 设置时间为当天的最后一刻，即23:59:59
        LocalDateTime endOfDay = localDateTime.withHour(23).withMinute(59).withSecond(59).withNano(0);

        // 转换回Date类型
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    /***
     *
/**
     * 方法说明：Date格式化字符串 方法名称：parseDateSDF
     *
     * @param date
     * @return 返回值：String
     */
    public static String parseDateSDF2(Date date) {
        SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd");
        return SDF.format(date);
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算 fromDate 到 toDate 相差多少天
     *
     * @param fromDate
     * @param toDate
     * @return 天数
     *
     */
    public static long getSecondByMinusDate(Object fromDate, Object toDate) {

        Date f = DateUtils.chgObject(fromDate);

        Date t = DateUtils.chgObject(toDate);

        long fd = f.getTime();
        long td = t.getTime();

        return (td - fd);
    }


    /**
     * 將OBJECT類型轉換為Date
     *
     * @param date
     * @return
     */
    public static Date chgObject(Object date) {

        if (date != null && date instanceof Date) {
            return (Date) date;
        }

        if (date != null && date instanceof String) {
            return DateUtils.parseDateSDF2((String) date);
        }

        return null;

    }


    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 根据年计算年龄
     * @param birthTimeString yyyy-MM-dd
     * @return age 年龄
     */
    public static int getAgeFromBirthTime(String birthTimeString) {
        // 先截取到字符串中的年、月、日
        String[] strs = birthTimeString.trim().split("-");
        int selectYear = Integer.parseInt(strs[0]);
        int selectMonth = Integer.parseInt(strs[1]);
        int selectDay = Integer.parseInt(strs[2]);
        // 获得当前时间的年、月、日
        Calendar cal = Calendar.getInstance();
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH) + 1;
        int dayNow = cal.get(Calendar.DATE);

        // 用当前年月日减去生日年月日
        int yearMinus = yearNow - selectYear;
        int monthMinus = monthNow - selectMonth;
        int dayMinus = dayNow - selectDay;

        // 先大体赋值
        int age = yearMinus;
        return age;
    }

    public static final String LONG_DATE = "yyyy-MM-dd HH:mm:ss";

    /**
     * 时间字符串转化为日期类型，格式为yyyy-MM-dd HH:mm:ss
     *
     * @param dateTime
     *            时间字符串，格式为yyyy-MM-dd HH:mm:ss
     * @return Date
     */
    public static Date parseDateTime(String dateTime){
        try {
            return new SimpleDateFormat(LONG_DATE).parse(dateTime);
        } catch (ParseException e) {
            return null;
        }
    }


    /**
     * 获取间隔天数的时间对象
     * @param date  计算的时间
     * @param interval 间隔的天数
     * @return
     */
    public static Date getDateInterval(Date date,int interval){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, interval);
        return c.getTime();
    }

    /**
     * 获取间隔天数的格式化时间
     * @param date  计算的时间
     * @param interval 间隔的天数
     * @param pattern 格式化字符串：yyyyMMdd
     * @return
     */
    public static String getDateIntervalFormat(Date date,int interval,String pattern) {
        Date yesterday = getDateInterval(date,interval);
        return DateFormatUtils.format(yesterday, pattern);
    }

    /**
     * 中国标准时间转换为时间对象
     * @param dateString
     * @return
     */
    public static  Date str2Date(String dateString) {
        // 转换时间格式 ==> "Wed Nov 21 2018 00:00:00 GMT+0800 (中国标准时间)"
        dateString = dateString.replace("GMT", "").replaceAll("\\(.*\\)", "").trim();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss Z", Locale.US);
        // 只保留 年月日
        LocalDate ldt1 = LocalDate.parse(dateString, df);
        // 2018-11-21
        String t_date = ldt1.toString().replaceAll("T", "\\ ");
        return DateUtils.parseDateSDF(t_date);
    }

    private static String parseTime( String dateStr ) {
        // 转换时间格式 ==> "Wed Nov 21 2018 00:00:00 GMT+0800 (中国标准时间)"
        // 定义一个用于返回的变量
        String parseDate;
        // 字符串转Date
        // 定义一个 用于格式化字符串的变量
        SimpleDateFormat sdf =
                new SimpleDateFormat( "EEE MMM dd yyyy HH:mm:ss 'GMT+0800 (中国标准时间)'", Locale.US );
        // 创建 Date 对象
        Date date = new Date();
        try {
            // 将目标字符串按指定格式解析
            date = sdf.parse( dateStr );
        } catch (ParseException e) {
            throw new RuntimeException( "时间格式转换错误" + date);
        }
        //Date转字符串
        // 再次指定转换的格式
        sdf = new SimpleDateFormat( "yyyy-MM-dd" );
        // 按指定日期格式转换
        parseDate = sdf.format( date );
        //parseDate: 2018-11-21
        // 返回处理后的结果
        return parseDate;
    }


    /**
     * 当天的开始时间
     * @return
     */
    public static Date getStartTime() {
        //当天零点
        LocalDateTime today_start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        //转Date
        Date date = Date.from(today_start.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }

    /**
     * 当天的结束时间
     * @return
     */
    public static Date getEndTime() {
        //当天零点
        LocalDateTime today_end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        //转Date
        Date date = Date.from(today_end.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }

    /**
     * 比较当前时间与传入的时间大小
     *
     * @param end
     * @return
     */
    public static Boolean compareDate2(Date end) {

        Date nowDate = new Date();
        return nowDate.getTime() <= end.getTime();
    }

    /**
     * 比较当前时间与传入的时间大小
     *
     * @param dateTime
     * @return
     */
    public static Boolean compareDate2(LocalDateTime dateTime) {
        // 可以根据需要指定特定的时区
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = dateTime.atZone(zoneId);
        Date end =  Date.from(zonedDateTime.toInstant());
        Date nowDate = new Date();
        return nowDate.getTime() <= end.getTime();
    }

    /**
     * 比较当前时间与传入的时间大小
     *
     * @param start
     * @return
     */
    public static Boolean compareDate(Date start, Date end) {

        Date nowDate = new Date();
        return nowDate.getTime() > start.getTime() && nowDate.getTime() < end.getTime();
    }


    /**
     * 比较时间
     * @param time1
     * @param time2
     * @return
     */
    public static boolean compareDate(String time1,String time2) {
        if(StringUtils.isBlank(time1) || StringUtils.isBlank(time2)){
            return true;
        }else {
            Date date1 = parseDateTime(time1);
            Date date2 = parseDateTime(time2);
            return date1.before(date2);
        }
    }

    /**
     * 比较两个日期大小
     *
     * @param nowDate
     * @param endDate
     * @return
     */
    public static boolean getDateSpecific(Date nowDate, Date endDate) {
        int compareTo = nowDate.compareTo(endDate);
        return compareTo <= 0;
    }

    /**
     * 获取当前时间时间戳
     *
     * @return
     */
    public static Integer timeToStamp() {
        int seconds = (int) (System.currentTimeMillis() / 1000);
        return seconds;
    }

    /**
     * 指定时间转换时间戳
     *
     * @return
     */
    public static Integer timeToStamp(Date date) {
        int seconds = (int) (date.getTime() / 1000);
        return seconds;
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
        if ("h".equals(type)) {
            timeHaoMia = timeHaoMia * time;
        } else if ("d".equals(type)) {
            timeHaoMia = timeHaoMia * 24 * time;
        }
        return endMillisecond - beginMillisecond >= timeHaoMia;
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
     * 输入日期和天数返回 月底 加day天数后的时间
     *
     * @param day
     * @return
     */
    public static Date dateLongToDay(Date date, Integer month, Integer day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.add(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
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

    /**
     * 判断时间是否在时间段内
     *
     * @param nowTime
     * @param beginTime
     * @param endTime
     * @return
     */
    public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        // 设置当前时间
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        // 设置开始时间
        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);
        // 设置结束时间
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        // 处于开始时间之后，和结束时间之前的判断
        // date.after(begin) &&
        return date.before(end);
    }

    /**
     * 在输入日期上增加（+）或减去（-）月份
     *
     * @param date   输入日期
     * @param imonth 要增加或减少的月分数
     */
    public static Date addMonth(Date date, int imonth) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(date);
        cd.add(Calendar.MONTH, imonth);
        return cd.getTime();
    }
    /**
     * 获取当前时间前后的时间
     * @param day
     * @return
     */
    public static String getAfterDate(int day) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, +day);
        date = calendar.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat(LONG_DATE);
        String dateString = formatter.format(date);
        return dateString;
    }

    /**
     * 判断某个时间是否是在当前时间的30天之内    true 表示在30天之内
     */
    public static boolean isLatestWeek(Date addtime,Date now) {
        //得到日历
        Calendar calendar = Calendar.getInstance();
        //把当前时间赋给日历
        calendar.setTime(now);
        //设置为30天前
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        //得到7天前的时间
        Date before7days = calendar.getTime();
        return before7days.getTime() < addtime.getTime();

    }

    public static String dateConvert(String x) throws ParseException {

        SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);

        Date date = sdf1.parse(x);
        System.out.println("。。。。" + date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sDate = sdf.format(date);
        System.out.println(sDate);
        return sDate;
    }

    /**
     * String 格式化
     */
    public static String dateGeShiHua(String datestr,String format){
        String dateFormatStr = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            //String 转date
            Date date = simpleDateFormat.parse(datestr);
            //date格式化
            dateFormatStr = sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormatStr;
    }
    /**
     * 根据指定时间 获取n 天前的开始时间和结束时间
     * @return
     */
    public static Map<String,String> scheduleTimeNday(int year, int month) {
        Map<String,String> map = new HashMap<String,String>();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //开始时间
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate localDate = yearMonth.atDay(1);
        LocalDateTime startOfDay = localDate.atStartOfDay();
        ZonedDateTime zonedDateTime = startOfDay.atZone(ZoneId.of("Asia/Shanghai"));
        Date startData = Date.from(zonedDateTime.toInstant());
        //结束时间
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        LocalDateTime localDateTime = endOfMonth.atTime(23, 59, 59, 999);
        ZonedDateTime zonedDateTime2 = localDateTime.atZone(ZoneId.of("Asia/Shanghai"));
        Date endData = Date.from(zonedDateTime2.toInstant());




        map.put("startTimeStr",ft.format(startData));
        map.put("endTimeStr",ft.format(endData));

        return map;
    }


    /**
     *根据时间间隔生成随机时间
/**
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date randomDate(String beginDate, String endDate) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 构造开始日期
            Date start = format.parse(beginDate);
            // 构造结束日期
            Date end = format.parse(endDate);
            // getTime()表示返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
            if (start.getTime() >= end.getTime()) {
                return null;
            }
            long date = random(start.getTime(), end.getTime());

            return new Date(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static long random(long begin, long end) {
        long rtn = begin + (long) (Math.random() * (end - begin));
        // 如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
        if (rtn == begin || rtn == end) {
            return random(begin, end);
        }
        return rtn;
    }

    /**
     * 计算 fromDate 到 toDate 相差多少天
     *
     * @param fromDate
     * @param toDate
     * @return 天数
     */
    public static long getDayByMinusDate(Date fromDate, Date toDate) {
        long fd = fromDate.getTime();
        long td = toDate.getTime();
        return (td - fd) / (24L * 60L * 60L * 1000L);
    }

    public static boolean isMoreThanAMonth(Date date1, Date date2) {
        // 将Date对象转换为LocalDate对象
        LocalDate currentDate = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // 假设你要判断的时间
        LocalDate targetDate = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // 计算当前日期和目标日期之间的差距
        Period period = Period.between(targetDate, currentDate);

        // 判断差距的月份是否大于等于1

        return period.toTotalMonths() >= 1;
    }

    /**
     * 判断是不是月初第一天
     *
     * @param date
     * @return
     */
    public static boolean isFirstDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
        return calendar.get(Calendar.DAY_OF_MONTH) == 2;
    }

    public static long getOverplusTime(Date startTime, Date endTime) {

        return (endTime.getTime() - startTime.getTime());
    }

    /**
     * 获取拼团剩余时间
     *
     * @param startTime
     * @param hour
     * @param activityEndTime
     * @return
     */
    public static long getPinTuanOverplusTime(Date startTime, Integer hour, Date activityEndTime) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        Date date = calendar.getTime();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format2 = format.format(date);

        // 持续时间
        long endTime = date.getTime();
        if (activityEndTime.getTime() < endTime) {
            endTime = activityEndTime.getTime();
        }

        long now = (new Date()).getTime();

        return (endTime - now);
    }

    /**
     * 获取上个月月开始时间和结束时间
     *
     * @return
     */
    public static Map getLastMonthTime() throws Exception {
        Long startTime = getLastMonthStartTime();
        Long endTime = getLastMonthEndTime();
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault()));
        String endTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault()));
        Map map = new HashMap();
        map.put("startDate", startTimeStr);
        map.put("endDate", endTimeStr);
        return map;
    }

    public static Long getLastMonthStartTime() throws Exception {
        Long currentTime = System.currentTimeMillis();

        String timeZone = "GMT+8:00";
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
        calendar.setTimeInMillis(currentTime);
        calendar.add(Calendar.YEAR, 0);
        calendar.add(Calendar.MONTH, -1);
        // 设置为1号,当前日期既为本月第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }


    public static Long getLastMonthEndTime() {
        Long currentTime = System.currentTimeMillis();

        String timeZone = "GMT+8:00";
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
        calendar.setTimeInMillis(currentTime);
        calendar.add(Calendar.YEAR, 0);
        calendar.add(Calendar.MONTH, -1);
        // 获取当前月最后一天
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTimeInMillis();
    }

    /**
     * 获取昨天开始时间和结束时间
     *
     * @return
     */
    public static Map getYesterdayTime() {
        Long startTime = getBeginDayOfYesterday();
        Long endTime = getEndDayOfYesterDay();
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault()));
        String endTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault()));
        Map map = new HashMap();
        map.put("startDate", startTimeStr);
        map.put("endDate", endTimeStr);
        return map;
    }

    public static Long getBeginDayOfYesterday() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTimeInMillis();
    }




    public static Long getEndDayOfYesterDay() {

        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.add(Calendar.DAY_OF_MONTH, -1);


        return cal.getTimeInMillis();
    }


    /**
     * 获取当月开始时间和结束时间
     *
     * @return
     */
    public static Map getMonthTime() {
        Long startTime = getMonthStartTime();
        Long endTime = getMonthEndTime();
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault()));
        String endTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault()));
        Map map = new HashMap();
        map.put("startDate", startTimeStr);
        map.put("endDate", endTimeStr);
        return map;
    }

    /**
     * 获取当月开始时间
     */
    public static Long getMonthStartTime() {

        Long currentTime = System.currentTimeMillis();

        String timeZone = "GMT+8:00";
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
        calendar.setTimeInMillis(currentTime);
        calendar.add(Calendar.YEAR, 0);
        calendar.add(Calendar.MONTH, 0);
        // 设置为1号,当前日期既为本月第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();


    }

    /**
     * 获取当月的结束时间戳
     */
    public static Long getMonthEndTime() {
        Long currentTime = System.currentTimeMillis();

        String timeZone = "GMT+8:00";
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timeZone));
        calendar.setTimeInMillis(currentTime);
        calendar.add(Calendar.YEAR, 0);
        calendar.add(Calendar.MONTH, 0);
        // 获取当前月最后一天
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static String getNowDayZero() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 计算 两个时间相差多少小时
     *
     * @param fromDate
     * @param toDate
     * @return 小时
     *
     */
    public static int getDateHost(Date fromDate, Date toDate) {
        long beginTimestamp = fromDate.getTime();
        long endTimestamp = toDate.getTime();
        long difference = endTimestamp - beginTimestamp;
        double hours = difference / (1000 * 60 * 60);
        return (int) hours;
    }
    /**
     * 根据指定时间 获取n 天前的开始时间和结束时间
     * @return
     */
    public static Map<String,String> scheduleTimeNday(Date date,Integer amount) {
        Map<String,String> map = new HashMap<String,String>();

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.add(Calendar.DAY_OF_MONTH, amount);
        Calendar cal2 = new GregorianCalendar();
        cal2.setTime(date);
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59);
        cal2.add(Calendar.DAY_OF_MONTH, amount - 1);
        Long startTime = cal2.getTimeInMillis();
        Long endTime = cal.getTimeInMillis();
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault()));
        String endTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault()));

        map.put("startTimeStr",startTimeStr);
        map.put("endTimeStr",endTimeStr);

        return map;
    }
    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getCNTime() {
        return parseDateToStr("YYYY年MM月dd日 HH:mm", new Date());
    }

    /**
     * 获取当前日期
     *
     * @return 当前日期
     */
    public static String getCurrentDate() {
        String datestr = null;
        SimpleDateFormat df = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        datestr = df.format(new Date());
        return datestr;
    }

    /**
     * 将字符串日期转换为日期格式
     *
     * @param datestr
     * @return
     */
    public static Date stringToDate(String datestr) {

        if (datestr == null || datestr.isEmpty()) {
            return null;
        }
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        try {
            date = df.parse(datestr);
        } catch (ParseException e) {
            date = DateUtils.stringToDate(datestr, "yyyyMMdd");
        }
        return date;
    }

    /**
     * 获取今天开始时间
     *
     * @return
     */
    public static String getBeginDayOfToday() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 0);
        Long startTime = cal.getTimeInMillis();
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault()));
    }

    /**
     * 获取今天结束时间
     *
     * @return
     */
    public static String getEndDayOfToday() {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.add(Calendar.DAY_OF_MONTH, 0);
        Long endTime = cal.getTimeInMillis();
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String endTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault()));
        return endTimeStr;
    }


    /**
     * 根据当前时间 获取n 天前的开始时间和结束时间
     *
     * @return
     */
    public static Map<String, String> getNdayData(Integer amount) {
        Map<String, String> map = new HashMap<String, String>();

        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.add(Calendar.DAY_OF_MONTH, amount);
        Calendar cal2 = new GregorianCalendar();
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59);
        cal2.add(Calendar.DAY_OF_MONTH, amount - 1);
        Long startTime = cal2.getTimeInMillis();
        Long endTime = cal.getTimeInMillis();
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault()));
        String endTimeStr = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault()));

        map.put("startTimeStr", startTimeStr);
        map.put("endTimeStr", endTimeStr);

        return map;
    }

    /** 获取指定时间的开始时间 */
    public static String getTimeStart(String date) {
        String start = null;
        //指定的日期格式
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //指定日期
            Date date2 = format.parse(date);
            //开始时间
            start = format2.format(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return start;
    }

    /** 获取指定时间的结束时间 */
    public static String getTimeEnd(String date) {
        String end = null;
        //指定的日期格式
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //指定日期
            Date date2 = format.parse(date);
            int dayMis = 1000 * 60 * 60 * 24;
            long currentMillisecond = date2.getTime();
            long resultMis = currentMillisecond + (dayMis - 1);
            Date resultDate = new Date(resultMis);
            //结束时间
            end = format2.format(resultDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return end;
    }

    public static String UpdateTimeFormat(String dateStr, String dateformat) {
        Date date;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //指定的日期格式
        DateFormat format = new SimpleDateFormat(dateformat);
        try {
            date = df.parse(dateStr);
            dateStr = format.format(date);
        } catch (ParseException ignored) {

        }
        return dateStr;
    }
    /**
     * 将给定的日期加上指定的分钟数，并返回新的日期。
     *
     * @param date 原始日期
     * @param minutes 要增加的分钟数
     * @return 新的日期
     */
    public static Date addMinutesToDate(Date date, int minutes) {
        // 使用 Calendar 类来操作日期
        Calendar calendar = Calendar.getInstance();
        // 设置日历的时间为给定的日期
        calendar.setTime(date);
        // 增加指定的分钟数
        calendar.add(Calendar.MINUTE, minutes);

        // 返回新的日期
        return calendar.getTime();
    }

    /**
     * 生成供应商店铺id
     * 因为部分表是商家 和 供应商是公用的  会导致表中数据串 因此供应商id 要手动创建
     *
     * @return
     */
    public static Integer getSupplierShopId() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        String dateString = formatter.format(currentTime);
        int random2 = (int) (Math.random() * 900 + 10);
        String timestamp = dateString + random2;
        return Integer.valueOf(timestamp);

    }

    /**
     * 获取数据为2024-05-09T15:29:21.993+08:00
     */
    public static String getSpecialNowDate() {
        // 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();
        // 指定时区
        ZoneId zone = ZoneId.of("+08:00");
        ZonedDateTime zonedDateTime = now.atZone(zone);
        // 创建日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        // 格式化日期时间为字符串
        String formattedDateTime = zonedDateTime.format(formatter);
        System.out.println(formattedDateTime);
        return formattedDateTime;
    }

    /**
     * 获取当天结束时间（24点）
     *
     * @return
     */
    public static String getDayStrEndTime2() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        SimpleDateFormat df = new SimpleDateFormat(DateUtils.YYYY_MM_DD_HH_MM_SS);
        return df.format(todayEnd.getTime());
    }
}
