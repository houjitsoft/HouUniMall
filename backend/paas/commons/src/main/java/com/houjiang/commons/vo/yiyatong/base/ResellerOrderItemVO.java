package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ResellerOrderItemVO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 9:21
 * @Version 1.0
 * @author Amos
 */
@Data
public class ResellerOrderItemVO {

    /**
     * 商品发送数量
     */
    private Integer sendGoodsQuantity;
    /**
     * 总价格
     */
    private BigDecimal totalPrice;
    /**
     * 商品ID
     */
    private String goodsId;
    /**
     * 可退款商品数量
     */
    private Integer refundGoodsCan;
    /**
     * 退货状态
     */
    private Integer returnStatus;
    /**
     * 商品规格ID
     */
    private String goodsSpecId;
    /**
     * 配送费
     */
    private BigDecimal deliveryPrice;
    /**
     * 商品图片URL
     */
    private String goodsPhotoUrl;
    /**
     * 可发送数量
     */
    private Integer canSendCount;
    /**
     * 退货标识
     */
    private Integer returnFlag;
    /**
     * 单价
     */
    private BigDecimal perPrice;
    /**
     * 退货状态名称
     */
    private String returnStatusName;
    /**
     * 已退运费
     */
    private BigDecimal totalHasPostFee;
    /**
     * 产品规格, json格式保存
     */
    private String goodsSpecJson;
    /**
     * 发货状态：1-已发货，2-部分发货
     */
    private Integer sendType;
    /**
     * 商品数量
     */
    private Integer goodsQuantity;
    /**
     * 商品单位
     */
    private String goodsUnit;
    /**
     * 订单项ID
     */
    private String id;
    /**
     * 每件商品的配送费
     */
    private BigDecimal perDeliveryPrice;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     *  可售后商品数(退换货)
     */
    private Integer returnGoodsNum;


}
