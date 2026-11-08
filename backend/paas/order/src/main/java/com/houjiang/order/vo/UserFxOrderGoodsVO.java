package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 *  UserFxOrderGoodsVO 订单商品详情
 * @author Amos
 */
@Data
public class UserFxOrderGoodsVO {
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品commonId
     */
    private Integer commonId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 商品图片
     */
    private String goodsImage;
    /**
     * 商品图片
     */
    private String commonImage;
    /**
     * 商品数量
     */
    private Integer orderGoodsNum;
    /**
     * 退款数量
     */
    private Integer orderRefundNum;
    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;
    /**
     * 商品规格
     */
    private String goodsSpec;
    /**
     * 退货状态:0是无退货,1是退货中,2是退货完成 3商家拒绝退货
     */
    private Integer goodsReturnStatus;
    /**
     * 退款状态：0：无退款 1：退款中 2：退款完成  3：商家拒绝退款
     */
    private Integer goodsRefundStatus;
    /**
     * 运费
     */
    private BigDecimal shippingFee;
    /**
     * 退款金额
     */
    private BigDecimal returnPrice;
    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除
     */
    private Integer commonState;
    /**
     * 虚拟商品
     */
    private Integer virtualGoods;
    /**
     * 商品库存
     */
    private Integer commonStock;
    /**
     * 一级推广佣金比率
     */
    private BigDecimal oneTgRate;
    /**
     * 二级推广佣金比率
     */
    private BigDecimal twoTgRate;
    /**
     * 分享佣金比例
     */
    private BigDecimal fxRate;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 分类名称
     */
    private String catName;
    /**
     * 订单数
     */
    private Integer orderCount;
    /**
     * 预估佣金
     */
    private BigDecimal estimatedCommission;
    /**
     * 商品价格
     */
    private BigDecimal commonPrice;
    /**
     * 订单商品金额
     */
    private BigDecimal orderGoodsAmount;

}
