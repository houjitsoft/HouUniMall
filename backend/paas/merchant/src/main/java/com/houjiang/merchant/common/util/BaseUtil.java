package com.houjiang.merchant.common.util;

import com.houjiang.merchant.common.validation.Assert;
import com.houjiang.merchant.common.enums.PaymentEnum;

import java.math.BigDecimal;

/**
 * 金额处理工具
 * @author Amos
 */
public class BaseUtil {

    private BaseUtil() {}

    /**
     * 将单位为元的金额转换为单位为分
     *
     * @param yuan 单位为元的字符型值
     * @return int
     */
    public static String getPayId(String type, Integer createId) {
        String ret = "";
        switch (type) {
            case "mallOrder": ret = "order-" + createId; break;
            case "mallOrderCreate": ret = "orders-" + createId; break;
            case "userRecharge": ret = "user_recharge-" + createId; break;
            default: ;
        }
        return ret;
    }

}
