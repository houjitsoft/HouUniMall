package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 查询已售订单商品详情
 *
 * @author Amos
 */
@Data
public class SoldOrderByGoodsDetailsVO {

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品状态
     */
    private Integer goodsStatus;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 订单商品数量
     */
    private Integer orderGoodsNum;

    /**
     * 退货数量
     */
    private Integer orderGoodsReturnnum;

    /**
     * 退货状态
     */
    private Integer goodsReturnStatus;

    /**
     * 退款状态
     */
    private Integer goodsRefundStatus;

    /**
     * 规格
     */
    private String orderSpecChineseValue;

    /**
     * 评价状态
     */
    private Integer orderGoodsEvaluationStatus;

    /**
     * 商品实付
     */
    private BigDecimal orderGoodsAmount;

    /**
     * 1.线上支付  2.线下支付 3.货到付款
     */
    private Integer paymentCode;

    /**
     * 商家备注
     */
    private String orderShippingMessage;

    /**
     * 收货时间（最晚收货时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderReceiverDate;

    /**
     * 1：会员商品，2：阶梯价商品
     */
    private Integer hyOrStep;

    /**
     * 询价单号
     */
    private String inquiryId;

    /**
     * 含税单价
     */
    private BigDecimal goodsTaxPrice;

    /**
     * 未税单价
     */
    private BigDecimal goodsWeiPrice;

    /**
     * 订货编码
     */
    private String goodsCode;

    /**
     * 优惠券金额
     */
    private BigDecimal voucherPrice;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date seckillCancelTime;

    /**
     * 发货时效
     */
    private Integer deliveryTime;

    /**
     * 活动ID
     */
    private Integer promotionId;

    /**
     * 虚拟订单 0 是 1 否
     */
    private Integer virtualGoods;
    /**
     * 1普通订单   2商家批发订单(商家购买批发商商品订单)  3爆品绑定订单   4直播订单
     */
    private Integer orderType;
    /**
     * 用户订单是否供应商代发货 1是  2否
     */
    private Integer dropShipping;
    /**
     * 发货数量
     */
    private Integer shipmentQuantity;

    /**
     * 发货状态 0待发货 1部分发货 2发货完成
     */
    private Integer shipmentStatus;


    /**
     * 积分比例
     */
    private Double pointsRate;



    /**
     * 现金金额
     */
    private BigDecimal cashPrice;
}
