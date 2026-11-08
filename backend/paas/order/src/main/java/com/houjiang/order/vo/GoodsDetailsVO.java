package com.houjiang.order.vo;

import com.houjiang.commons.vo.order.OrderGoodsBarterVO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 已售订单商品详情
 *
 * @author Amos
 */
@Data
public class GoodsDetailsVO {

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 商品名
     */
    private String goodsName;

    /**
     * 商品图
     */
    private String goodsImage;

    /**
     * 商品价格
     */
    private String goodsPrice;

    /**
     * 快递名
     */
    private String expressName;

    /**
     * 订单商品数量
     */
    private Integer orderGoodsNum;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 退款状态：0：无退款 1：退款中 2：退款完成  3：商家拒绝退款
     */
    private String goodsRefundStatus;

    /**
     * 退货数量
     */
    private String orderGoodsReturnnum;

    /**
     * 规格
     */
    private String orderSpecChineseValue;

    /**
     * 实付价格
     */
    private Double orderGoodsPaymentAmount;

    /**
     * 优惠券金额
     */
    private BigDecimal voucherPrice;

    /**
     * 红包抵扣金额
     */
    private BigDecimal orderRptPrice;

    /**
     * 商品id
     */
    private String commonId;

    /**
     * 供应商商品价格
     */
    private BigDecimal supperGoodsPrice;

    /**
     * 应商订单总价 实付价 （含运费）
     */
    private BigDecimal supperPaymentAmount;

    /**
     * 供应商订单总价 (不含运费)
     */
    private BigDecimal supperOrderAmount;

    /**
     * 1：会员商品，2：阶梯价商品
     */
    private Integer hyOrStep;

    /**
     * 单位
     */
    private String dw;

    /**
     * 含税单价
     */
    private BigDecimal goodsTaxPrice;

    /**
     * 未税单价
     */
    private BigDecimal goodsWeiPrice;

    /**
     * 税率
     */
    private Double taxRate;

    /**
     * 分类名
     */
    private String catName;

    /**
     * 退货状态
     */
    private String  goodsReturnStatus;


    /**
     * 改价次数
     */
    private Integer orderGoodsAdjustNum;

    /**
     * 改价最初金额
     */
    private BigDecimal orderGoodsAdjustBeforePrice;


    private Integer goodsPlusDis;

    /**
     * 商品总金额
     */
    private BigDecimal orderGoodsAmount;

    /**
     * 手工调整金额
     */
    private BigDecimal orderGoodsAdjustFee;

    /**
     * 发货数量
     */
    private Integer shipmentQuantity;

    /**
     * 发货状态 0待发货 1部分发货 2发货完成
     */
    private Integer shipmentStatus;

    /**
     * 发货店铺名称（可能为快递公司名称）
     */
    private String deliverShopName;

    /**
     * 用户订单是否供应商代发货 1是  2否
     */
    private Integer dropShipping;

    /**
     * 发货店铺ID
     */
    private Integer deliverShopId;

    /***
     * 店铺经销折扣
     */
    private BigDecimal dealerDiscount;

    /**
     * 运费
     */
    private BigDecimal shippingFee;


    /**
     * 订单商品版本号
     */
    private Integer verId;

    /**
     * 换货商品ID
     */
    private Integer barterGoodsId;

    /**
     * 换货商品信息
     */
    private OrderGoodsBarterVO barterGoods;

    /**
     * 积分比例
     */
    private Double pointsRate;



    /**
     * 现金金额
     */
    private BigDecimal cashPrice;

}
