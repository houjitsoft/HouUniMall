package com.houjiang.merchant.service.mall;

import com.houjiang.merchant.common.core.PageResult;
import com.houjiang.merchant.validate.commons.PageValidate;
import com.houjiang.merchant.vo.mall.MallSettlementListedVo;
import com.houjiang.merchant.vo.mall.MallSettlementCountVo;

import java.math.BigDecimal;

/**
 *  结算表接口类
 * @author Amos
 */
public interface IMallSettlementService {

    /**
     * 已结算订单 数量
     * @param shopId
     * @return
     */
    Integer getSettlementOrders(Integer shopId);

    /**
     * 已结算的订单金额
     * @param shopId
     * @return
     */
    BigDecimal getSettlementOrderMoney(Integer shopId);

    /**
     * 已结算入账金额
     * @param shopId
     * @return
     */
    BigDecimal getSettlementMoney(Integer shopId);

    /**
     * 已结算交易服务费
     * @param shopId
     * @return
     */
    BigDecimal getSettlementServiceMoney(Integer shopId);

    /**
     * 统计数据
     * @param shopId
     * @return
     */
    MallSettlementCountVo count(Integer shopId);

    /**
     * MallSettlement列表
     * @param pageValidate
     * @return
     */
    PageResult<MallSettlementListedVo> list(PageValidate pageValidate, Integer shopId);
}
