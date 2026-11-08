package com.houjiang.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品详情信息
 *
 * @author Amos
 */
@Data
public class ReturnGoodsDetailsListDTO {

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架
     */
    private Integer commonState;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品邮费
     */
    private BigDecimal shippingFee;

    /**
     * 订单邮费
     */
    private BigDecimal orderShippingFee;

    /**
     * 商品实付金额
     */
    private BigDecimal orderGoodsPaymentAmount;

    /**
     * 订单实付金额
     */
    private BigDecimal orderPaymentAmount;

    /**
     * 商品数量
     */
    private Integer orderGoodsNum;

    /**
     * 退货数量
     */
    private Integer orderGoodsReturnnum;

    /**
     * 商品金额 （实付金额）
     */
    private BigDecimal orderGoodsAmount;

    /**
     * 规格值
     */
    private String orderSpecChineseValue;

    /**
     * 评价状态
     */
    private Integer orderGoodsEvaluationStatus;

    /**
     * 积分商品所需积分
     */
    private Integer needIntegral;

    /**
     * 积分商品
     */
    private Integer goodsIntegral;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 1普通商品 2供应商商品
     */
    private Integer goodsType;

    /**
     * 活动ID
     */
    private Integer promotionId;
}
