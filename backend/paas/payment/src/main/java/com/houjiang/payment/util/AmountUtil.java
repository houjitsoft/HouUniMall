package com.houjiang.payment.util;

import com.houjiang.payment.constant.PaymentConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额处理工具类
 * 按照文档规范：金额单位为分，不能带小数
 * 
 * @author Amos
 */
public class AmountUtil {
    
    /**
     * 元转分
     * 100.00元 -> 10000分
     * 
     * @param yuan 元金额
     * @return 分金额（整数）
     */
    public static Long yuanToFen(BigDecimal yuan) {
        if (yuan == null) {
            return 0L;
        }
        return yuan.multiply(BigDecimal.valueOf(PaymentConstants.AMOUNT_YUAN_TO_FEN))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }
    
    /**
     * 分转元
     * 10000分 -> 100.00元
     * 
     * @param fen 分金额
     * @return 元金额
     */
    public static BigDecimal fenToYuan(Long fen) {
        if (fen == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(fen)
                .divide(BigDecimal.valueOf(PaymentConstants.AMOUNT_YUAN_TO_FEN), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 验证金额是否有效
     * 
     * @param amount 金额（元）
     * @return true-有效 false-无效
     */
    public static boolean isValidAmount(BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        // 金额必须大于0
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        // 金额最多两位小数
        return amount.scale() <= 2;
    }
    
    /**
     * 格式化金额显示
     * 
     * @param amount 金额
     * @return 格式化后的字符串，如：100.00
     */
    public static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }
}
