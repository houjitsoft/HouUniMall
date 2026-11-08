package com.houjiang.payment.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
 /**
 * @ClassName
 * @Description  交易订单表-强调唯一订单-充值则先创建充值订单
 * @Author Amos
 * @Date 2023-05-31 16:13:57
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_pay_consume_trade")
@Data
public class PayConsumeTradeEntity  implements Serializable {
    /**
     * 交易订单id
     */
    private String consumeTradeId;
    /**
     * 商户订单id
     */
    private String orderId;
    /**
     * 买家id
     */
    private String buyerId;
    /**
     * 卖家id
     */
    private String sellerId;
    /**
     * 付款人id
     */
    private String payUserId;
    /**
     * 订单状态()
     */
    private Integer orderStateId;
    /**
     * 交易类型
     */
    private Integer tradeTypeId;
    /**
     * 支付渠道
     */
    private Integer paymentChannelId;
    /**
     * 1:担保交易  2：直接交易
     */
    private Integer tradeType;
    /**
     * 总付款额度  
     */
    private Double orderPaymentAmount;
    /**
     * 实付金额，在线支付金额
     */
    private BigDecimal tradePaymentAmount;
    /**
     * 第三方支付金额
     */
    private Double tradeThirdPartyAmount;
    /**
     * 余额支付
     */
    private Double tradePaymentMoney;
    /**
     * 充值卡余额支付
     */
    private Double tradePaymentRechargeCard;
    /**
     * 折扣优惠
     */
    private Double tradeDiscount;
    /**
     * 年-月-日
     */
    private Date tradeDate;
    /**
     * 年
     */
    private Integer tradeYear;
    /**
     * 月
     */
    private Integer tradeMonth;
    /**
     * 日
     */
    private Integer tradeDay;
    /**
     * 标题
     */
    private String tradeTitle;
    /**
     * 描述
     */
    private String tradeDesc;
    /**
     * 创建时间
     */
    private Date tradeCreateTime;
    /**
     * 付款时间
     */
    private Date tradePayTime;
    /**
     * 结束时间
     */
    private Date tradeFinishTime;
    /**
     * 时间戳
     */
    private Date ts;
    /**
     * 兑换积分商品所用的积分
     */
    private Integer totalIntegral;
    /**
     * 积分商品 0 是 1 否
     */
    private Integer integralGoods;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
