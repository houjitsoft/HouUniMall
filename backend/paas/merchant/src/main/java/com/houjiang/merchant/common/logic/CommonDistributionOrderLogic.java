package com.houjiang.merchant.common.logic;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.distribution.DistributionOrder;
import com.houjiang.merchant.common.enums.DistributionEnum;
import com.houjiang.merchant.common.mapper.distribution.DistributionOrderMapper;
import com.houjiang.merchant.common.util.SpringUtils;

import java.math.BigDecimal;

public class CommonDistributionOrderLogic {

    /**
     * @notes 订单分销金额
     * @param $order_id
     * @return float
     * @author Amos
     * @datetime 2025-01-23 11:08:16
     */
    public static BigDecimal orderDistributionMoney(Integer orderId) {
        BigDecimal distributionMoney = BigDecimal.ZERO;
        DistributionOrderMapper distributionOrderMapper = SpringUtils.getBean(DistributionOrderMapper.class);
        QueryWrapper<DistributionOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("status", DistributionEnum.ORDER_STATUS_SUCCESS.getCode());
        queryWrapper.eq("delete_time", 0);
        distributionMoney = distributionOrderMapper.sum("money", queryWrapper);
        return distributionMoney;
    }
}
