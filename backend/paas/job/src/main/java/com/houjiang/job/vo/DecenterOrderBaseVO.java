package com.houjiang.job.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName DecenterOrderBaseVO
 * @Description
 * @Author Amos
 * @Date 2025/1/14 10:25
 * @Version 1.0
 * @author Amos
 */

@Data
public class DecenterOrderBaseVO {

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 卖家店铺id
     */
    private Integer shopId;

    /**
     * 卖家店铺名称
     */
    private String shopName;

    /**
     * 支付方式1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付,6-购物卡支付，7-充值卡+支付宝，8-充值卡+微信,9-蜂币支付，10-蜂币+支付宝，11-蜂币+微信, 13-账期支付, 14-主管支付，16-paypal支付
     */
    private Integer payCode;

    /**
     * 应付金额（商品实际支付金额 + 运费）
     */
    private BigDecimal orderPaymentAmount;

    /**
     * 订单状态 1.待付款 2.已付款 3.待发货 4.已发货 5.已签收 6.确认收货 7.取消 8.退款中 9.已退款 11.待自提\r\n12 虚拟订单已过期
     */
    private Integer orderStatus;

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 买家姓名
     */
    private String buyerUserName;

    /**
     * 收货人的姓名
     */
    private String orderReceiverName;

    /**
     * 收货人的详细地址
     */
    private String orderReceiverAddress;

    /**
     * 收货人的联系方式
     */
    private String orderReceiverContact;

    /**
     * 手机端1PC 2wap微信浏览器 3安卓 5ios 6微信小程序 7wap浏览器
     */
    private Integer orderFrom;

    /**
     * 积分商品订单 0是 1否
     */
    private Integer integralGoods;

    /**
     * 退货状态:0是无退货,1是退货中,2是退货完成，3退货不通过
     */
    private Integer orderReturnStatus;

    /**
     * 退款状态:0是无退款,1是退款中,2是退款完成,3退款不通过
     */
    private Integer orderRefundStatus;

    /**
     * 收货人收货地址区域ID
     */
    private Integer orderReceiverProvinceId;

    /**
     * 活动ID 0 普通点单 1 砍价（废弃） 2 秒杀 3 限时折扣 4拼团 5 满减增折
     */
    private Integer orderPromotionId;

    /**
     * 订单生成时间
     */
    private Date orderCreateTime;

    /**
     * 优惠价格（商品金额-支付金额）
     */
    private BigDecimal orderDiscountFee;
}
