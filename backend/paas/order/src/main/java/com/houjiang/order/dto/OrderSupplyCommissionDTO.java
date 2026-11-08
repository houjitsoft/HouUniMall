package com.houjiang.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @ClassName OrderSupplyCommissionDTO
 * @Description 代发货订单经销商和供应商佣金信息
 * @Author Amos
 * @Date 2023/7/26 9:33
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSupplyCommissionDTO {

    /**
     * 退货商品编号，0为退款
     */
    private Integer orderGoodsId;

    /**
     * 订单号-确认收货
     */
    private String orderId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 退货数量
     */
    private Integer orderGoodsReturnnum;

    /**
     * 实付金额
     */
    private BigDecimal orderGoodsPaymentAmount;

    /**
     * 经销商委员会
     */
    private BigDecimal dealerCommission;

    /**
     * 供应委员会
     */
    private BigDecimal supplyCommission;

    /**
     * 经销商恢复委员会
     */
    private BigDecimal dealerReturnCommission;

    /**
     * 供应退货佣金
     */
    private BigDecimal supplyReturnCommission;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;
}
