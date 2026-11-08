package com.houjiang.merchant.common.modelAttr;

import com.houjiang.merchant.common.enums.OrderAfterSaleEnum;

/**
 * @author Amos
 */
public class MallOrderAfterSaleAttr {

    /**
     * 买家可寄回
     * @param subStatus
     * @return
     */
    public static Integer getBuyerCanReturnAttr(Integer subStatus) {
        if (subStatus.equals(OrderAfterSaleEnum.SUB_STATUS_WAIT_BUYER_RETURN.getCode())) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * getTypeTextAttr
     *
     */
    public static String getTypeTextAttr(Integer code) {
        return OrderAfterSaleEnum.getTypeText(code);
    }
}
