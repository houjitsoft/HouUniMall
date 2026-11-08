package com.houjiang.merchant.common.logic;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.entity.distribution.DistributionOrder;
import com.houjiang.merchant.common.entity.mall.MallOrderAfterSale;
import com.houjiang.merchant.common.enums.DistributionEnum;
import com.houjiang.merchant.common.enums.OrderAfterSaleEnum;
import com.houjiang.merchant.common.enums.YesNoEnum;
import com.houjiang.merchant.common.mapper.distribution.DistributionOrderMapper;
import com.houjiang.merchant.common.mapper.mall.MallOrderAfterSaleMapper;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class CommonOrderAfterSaleLogic {

    /**
     * @notes 查找订单是否有售后中的记录
     * @param $order_id
     * @return bool
     * @author Amos
     * @datetime 2025-01-23 10:11:25
     */
    public static Boolean hasOrderAfterSaleIng(Integer orderId) {
        MallOrderAfterSaleMapper mapper = SpringUtils.getBean(MallOrderAfterSaleMapper.class);
        QueryWrapper<MallOrderAfterSale> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId)
            .in("status", new ArrayList<Integer>() {{
                add(OrderAfterSaleEnum.STATUS_APPLY.getCode());
                add(OrderAfterSaleEnum.STATUS_AGREE.getCode());
            }}).eq("delete_time", 0)
            .last("limit 1");
        MallOrderAfterSale orderAfterSale = mapper.selectOne(queryWrapper);
        return StringUtils.isNull(orderAfterSale) ? false : true;
    }

    /**
     * @notes 查找订单是否有待结算的分销订单
     * @param $order_id
     * @return bool
     * @author Amos
     * @datetime 2025-01-23 10:06:40
     */
    public static Boolean findWaitSettlement(Integer orderId) {
        DistributionOrderMapper mapper = SpringUtils.getBean(DistributionOrderMapper.class);
        QueryWrapper<DistributionOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId)
            .eq("status", DistributionEnum.ORDER_STATUS_WAIT.getCode())
            .eq("delete_time", 0)
            .last("limit 1");
        DistributionOrder order = mapper.selectOne(queryWrapper);
        return StringUtils.isNull(order) ? false : true;
    }

    /**
     * @notes 查找订单待结算的售后列表
     * @param $order_id
     * @return int
     * @author Amos
     * @datetime 2025-01-23 10:06:40
     */
    public static Long orderWaitSettlementCount(Integer orderId) {
        Long count = 0L;
        MallOrderAfterSaleMapper mapper = SpringUtils.getBean(MallOrderAfterSaleMapper.class);
        QueryWrapper<MallOrderAfterSale> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId)
            .eq("status", OrderAfterSaleEnum.STATUS_SUCCESS.getCode())
            .eq("settlement_is", YesNoEnum.NO.getCode())
            .eq("delete_time", 0);
        count = mapper.selectCount(queryWrapper);
        return count;
    }

    /**
     * @notes 查找订单待结算的售后列表
     * @param $order_id
     * @return int
     * @author Amos
     * @datetime 2025-01-23 10:06:40
     */
    public static List<MallOrderAfterSale> orderWaitSettlementList(Integer orderId) {
        MallOrderAfterSaleMapper mapper = SpringUtils.getBean(MallOrderAfterSaleMapper.class);
        QueryWrapper<MallOrderAfterSale> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId)
                .eq("status", OrderAfterSaleEnum.STATUS_SUCCESS.getCode())
                .eq("settlement_is", YesNoEnum.NO.getCode())
                .eq("delete_time", 0);
        List<MallOrderAfterSale> list = mapper.selectList(queryWrapper);
        return list;
    }
}
