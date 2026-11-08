package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName OrderReturnGoodsItemVO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 10:41
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderReturnGoodsItemVO {

    /**
     * 分销市场基础价格
     */
    private BigDecimal distriMarketBasePrice;
    /**
     * 订单编号
     */
    private String orderSn;
    /**
     * 总价格
     */
    private BigDecimal totalPrice;
    /**
     * 商品ID
     */
    private String goodsId;
    /**
     * 订单产品表ID, 关联表t_order_item的ID
     */
    private String orderItemId;
    /**
     * 退款商品
     */
    private BigDecimal postFee;
    /**
     * 退货状态
     */
    private String returnStatus;
    /**
     * 总金额
     */
    private BigDecimal totalMoney;
    /**
     * 商品规格ID
     */
    private String goodsSpecId;
    /**
     * 供应商退款金额
     */
    private BigDecimal supplierRefundAmount;
    /**
     * 商品图片URL
     */
    private String goodsPhotoUrl;
    /**
     * 单价
     */
    private BigDecimal perPrice;
    /**
     * 商品规格JSON
     */
    private String goodsSpecJson;
    /**
     * 退货编号
     */
    private String returnSn;
    /**
     * 商品数量
     */
    private Integer goodsQuantity;
    /**
     * 商品单位
     */
    private String goodsUnit;
    /**
     * ID
     */
    private String id;
    /**
     * 发货状态
     */
    private String sendGoodStatus;
    /**
     * 分销市场价格
     */
    private BigDecimal distriMarketPrice;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 售后类型，1：退款，2：退货退款，3：换货，4：仅退款,5：部分退款
     */
    private Integer returnType;
    /**
     * 基础价格
     */
    private BigDecimal basePrice;
}
