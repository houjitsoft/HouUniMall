package com.houjiang.merchant.common.logic;

import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.StringUtils;

import java.math.BigDecimal;

/**
 * @author Amos
 */
public class CommonShopLogic {
    /**
     * @notes 增加店铺余额
     */
    public static void incMoney(Integer shopId, BigDecimal money) {
        MallShopMapper mallShopMapper = SpringUtils.getBean(MallShopMapper.class);
        MallShop shop = mallShopMapper.selectById(shopId);
        if (StringUtils.isNull(shop)) {
            return;
        } else {
            shop.setMoney(shop.getMoney().add(money));
            mallShopMapper.updateById(shop);
        }
    }
}
