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
public class GoodsOrderDetailsVO {

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品价格
     */
    private String goodsPrice;

    /**
     * 快递公司
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
     * 退货状态
     */
    private String goodsReturnStatus;

    /**
     * 退货数量
     */
    private String orderGoodsReturnnum;

    /**
     * 规格
     */
    private String orderSpecChineseValue;

    /**
     * 实付金额
     */
    private BigDecimal orderGoodsPaymentAmount;

    /**
     * 代金券支付
     */
    private BigDecimal voucherPrice;

    /**
     * 红包
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
     * 供应商订单总价 实付价 （含运费）
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
    private BigDecimal taxRate;

    /**
     * 改价次数
     */
    private Integer orderGoodsAdjustNum;

    /**
     * 改价最初金额
     */
    private BigDecimal orderGoodsAdjustBeforePrice;

    /**
     * 会员折扣
     */
    private Integer goodsMemberDiscount;

    /**
     * 会员等级
     */
    private String gradeName;

    /**
     * 商品邮费
     */
    private BigDecimal shippingFee;

    /**
     * 发货数量
     */
    private Integer shipmentQuantity;

    /**
     * 发货状态 0待发货 1部分发货 2发货完成
     */
    private Integer shipmentStatus;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 用户id
     */
    private String buyerUserId;

    /**
     * 店铺经销折扣
     */
    private BigDecimal dealerDiscount;

    /**
     * 含税金额  *  下单数量 - 经销折扣金额
     */
    private BigDecimal totalTaxInclusiveAmount;

    /**
     * 最低价
     */
    private BigDecimal minPrice;

    /**
     * 最高价
     */
    private BigDecimal maxPrice;

    /**
     * 协议价
     */
    private BigDecimal agreementPrice;

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
     * 商品规格
     */
    private String goodsSpec;
}
