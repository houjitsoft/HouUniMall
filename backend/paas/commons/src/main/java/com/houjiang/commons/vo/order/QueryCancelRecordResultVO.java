package com.houjiang.commons.vo.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryCancelRecordResultVO {

    /** orderID */
    private String orderId;

    /**  */
    private Integer shopId;

    private String shopName;

    /** 买家id */
    private String buyerUserId;

    /** 买家姓名 */
    private String buyerUserName;

    /** 订单生成时间 */
    private String orderCreateTime;

    /** 商品总价格 */
    private BigDecimal orderGoodsAmount;

    /** 应付金额 */
    private BigDecimal orderPaymentAmount;

    /** 订单状态 */
    private Integer orderStatus;

    /** 虚拟订单0是1否 */
    private Integer virtualGoods;

    /** 虚拟订单有效时间 */
    private String periodOfValidity;

    /** 是否过期退款 */
    private Integer pastDue;

    /** 虚拟商品兑换码 */
    private String redeemCode;

    /** 虚拟商品兑换码二维码 */
    private String redeemCodeUrl;

    /**  */
    private Integer orderGoodsId;

    /**  */
    private Integer goodsId;

    /** 商品名称 */
    private String goodsName;

    /** 商品价格 */
    private BigDecimal goodsPrice;

    /** 商品数量 */
    private Integer orderGoodsNum;
    /**
     * 商品图片
     */
    private String goodsImage;

    /** 是否核销 */
    private Integer isCancel;

    /** 用户名 */
    private String userName;

    /** 退货状态:0是无退货,1是退货中,2是退货完成，3退货不通过 */
    private Integer orderReturnStatus;

    /** 退款状态:0是无退款,1是退款中,2是退款完成,3退款不通过 */
    private Integer orderRefundStatus;

    /** 是否是门店自提订单 1是 2否 */
    private Integer orderIsShopTake;


}
