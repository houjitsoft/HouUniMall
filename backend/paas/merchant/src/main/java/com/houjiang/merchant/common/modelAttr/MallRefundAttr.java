package com.houjiang.merchant.common.modelAttr;

import com.houjiang.merchant.common.enums.LogMoneyEnum;
import com.houjiang.merchant.common.enums.RefundEnum;

/**
 * @author Amos
 */
public class MallRefundAttr {
    public static Integer getCanApplyRefundAttr(Integer type)  {
        Integer ret = 0;

        switch (type) {
            case 0:
            case 1:
            case 2:
            case 3:
                ret = LogMoneyEnum.USER_MONEY_ORDER_CANCEL.getCode();
                break;
            case 4:
                ret = LogMoneyEnum.USER_MONEY_ORDER_REFUND.getCode();
                break;
            default:
        }
        return ret;
    }
}
