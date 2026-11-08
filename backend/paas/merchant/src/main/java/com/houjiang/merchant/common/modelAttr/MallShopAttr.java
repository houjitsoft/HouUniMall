package com.houjiang.merchant.common.modelAttr;

import com.houjiang.merchant.common.enums.ShopEnum;

/**
 * @author Amos
 */
public class MallShopAttr {

    /**
     * 是否自营
     * @param type
     * @return
     */
    public static Integer getCanApplyRefundAttr(Integer type) {
        if (type.equals(ShopEnum.TYPE_SELF) == true) {
            return 1;
        } else {
            return 0;
        }
    }
}
