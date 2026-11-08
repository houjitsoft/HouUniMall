package com.houjiang.merchant.common.logic;

import com.houjiang.merchant.common.entity.mall.MallShop;
import com.houjiang.merchant.common.entity.mall.MallShopAccountLog;
import com.houjiang.merchant.common.enums.ShopAccountEnum;
import com.houjiang.merchant.common.exception.OperateException;
import com.houjiang.merchant.common.mapper.mall.MallShopAccountLogMapper;
import com.houjiang.merchant.common.mapper.mall.MallShopMapper;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.StringUtils;

import java.math.BigDecimal;

public class CommonMallShopAccountLogLogic {

    /**
     * @notes 创建账户流水
     * @param array $params
     * @return bool
     * @author Amos
     * @datetime 2023-02-06 19:25:29
     */
    public static void createAccountLog(MallShopAccountLog log) {
        MallShopMapper mallShopMapper = SpringUtils.getBean(MallShopMapper.class);
        MallShop shop = mallShopMapper.selectById(log.getShopId());
        MallShopAccountLogMapper mallShopAccountLogMapper = SpringUtils.getBean(MallShopAccountLogMapper.class);
        if (StringUtils.isNull(shop)) {
            throw new OperateException("找不到店铺信息");
        }
        BigDecimal leftAmount = new BigDecimal(0);
        if (log.getChangeObject().equals(ShopAccountEnum.CHANGE_OBJECT_SHOP_MONEY.getCode())) {
            leftAmount = shop.getMoney();
        } else if (log.getChangeObject().equals(ShopAccountEnum.CHANGE_OBJECT_SHOP_GATHER_MONEY.getCode())) {
            leftAmount = new BigDecimal(shop.getGatherMoney());
        }
        log.setSn(mallShopAccountLogMapper.randMakeOrderSn("sn"));
        log.setCreateTime(System.currentTimeMillis() / 1000);
        log.setUpdateTime(System.currentTimeMillis() / 1000);
        log.setLeftAmount(leftAmount);
        mallShopAccountLogMapper.insert(log);
    }
}
