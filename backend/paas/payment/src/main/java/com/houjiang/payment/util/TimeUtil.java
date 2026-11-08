package com.houjiang.payment.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 时间处理工具类
 * 按照文档规范：使用13位毫秒时间戳
 * 
 * @author Amos
 */
public class TimeUtil {
    
    /**
     * 日期时间格式
     */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /**
     * 获取当前13位毫秒时间戳
     * 
     * @return 13位时间戳字符串
     */
    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * 获取当前13位毫秒时间戳（Long类型）
     * 
     * @return 13位时间戳
     */
    public static Long getCurrentTimestampLong() {
        return System.currentTimeMillis();
    }
    
    /**
     * 获取当前格式化时间 yyyyMMddHHmmss
     * 
     * @return 格式化后的时间字符串
     */
    public static String getCurrentDateTimeStr() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }
    
    /**
     * 时间戳转LocalDateTime
     * 
     * @param timestamp 13位毫秒时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime timestampToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
    
    /**
     * 时间戳转格式化字符串
     * 
     * @param timestamp 13位毫秒时间戳
     * @return yyyyMMddHHmmss格式字符串
     */
    public static String timestampToDateTimeStr(Long timestamp) {
        LocalDateTime dateTime = timestampToLocalDateTime(timestamp);
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }
    
    /**
     * LocalDateTime转13位时间戳
     * 
     * @param dateTime LocalDateTime
     * @return 13位毫秒时间戳
     */
    public static Long localDateTimeToTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
    
    /**
     * 验证时间戳是否有效（13位）
     * 
     * @param timestamp 时间戳
     * @return true-有效 false-无效
     */
    public static boolean isValidTimestamp(String timestamp) {
        if (timestamp == null || timestamp.length() != 13) {
            return false;
        }
        try {
            Long.parseLong(timestamp);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
