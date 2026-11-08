package com.houjiang.payment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 卡集合
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardIncomeListVO {

    /**
     * 商户订单id
     */
    private String orderId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 昵称
     */
    private String userNickname;

    /**
     * 金额
     */
    private BigDecimal recordMoney;

    /**
     * 标题
     */
    private String recordTitle;

    /**
     * 描述
     */
    private String recordDesc;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recordTime;

    /**
     * 1,购物;2,转账;3,充值;4,提现;5,退款;10,佣金;11,结算;12,蜂卡充值;13,蜂卡收益
     */
    private Integer tradeTypeId;

    /**
     * 付款状态 1待付款, 2待配货,3待发货,4已发货,5已签收, 6已完成, 7已取消, 8处理中
     */
    private Integer recordStatus;

    /**
     * 充值的付款方式:7蜂卡充值;9蜂卡支付宝;10蜂卡微信
     */
    private Integer depositPayChannel;

    /**
     * 卡支付金额
     */
    private BigDecimal tradePaymentRechargeCard;

    /**
     * 支付方式:1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付,6-购物卡支付，7-充值卡+支付宝，8-充值卡+微信,9-蜂币支付，10-蜂币+支付宝，11-蜂币+微信'
     */
    private Integer payCode;

    /**
     * 退款金额
     */
    private BigDecimal orderRefundAmount;

    /**
     * 第三方支付金额
     */
    private BigDecimal tradeThirdPartyAmount;

    /**
     * 
     */
    private Integer pastDue;
    /**
     *
     */
    private Integer isCancel;
    /**
     *
     */
    private Integer virtualGoods;
    /**
     *
     */
    private Integer orderIsShopTake;

    /**
     * 退款状态
     */
    private String orderRefundStatus;

    /**
     *退货状态
     */
    private String orderReturnStatus;
    /**
     *订单金额
     */
    private BigDecimal orderPaymentAmount;

  

    private Integer orderStatus;

    /**
     *1-实物；2-虚拟；3-门店
     */
    private Integer orderType;

    /**
     *
     */
    private Integer returnFlag;

    /**
     * 是否存在退货退款
     */
    private String isExistReturn;


}
