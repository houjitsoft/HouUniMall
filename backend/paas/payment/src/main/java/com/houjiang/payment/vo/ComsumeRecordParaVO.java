package com.houjiang.payment.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单记录展示
 *
 * @author Amos
 *
 */
@Data
public class ComsumeRecordParaVO  {
	/**
	 * 订单id
	 */
	private String orderId;

	/**
	 * 创建时间
	 */
	private Date recordTime;

	/**
	 * 交易类型1获得佣金 2提现
	 */
	private Integer tradeTypeId;

	/**
	 * 付款状态 1待付款, 2待配货,3待发货,4已发货,5已签收, 6已完成, 7已取消, 8处理中
	 */
	private Integer recordStatus;


	/**
	 * 0未删除 1删除
	 */
	private Integer recordDelete;

	/**
	 * 记录总额
	 */
	private BigDecimal recordMoney;

	/**
	 * 交易记录id
	 */
	private Integer consumeRecordId;

	/**
	 * 操作之后的金额
	 */
	private BigDecimal recordAfterMoney;

	/**
	 * 支付方式1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付,6-购物卡支付，7-充值卡+支付宝，8-充值卡+微信,9-蜂币支付，10-蜂币+支付宝，11-蜂币+微信'
	 */
	private Integer payCode;

	/**
	 * 实际支付单号
	 */
	private String recordPayorder;

	/**
	 * 用户资金-预付款充值、转账、结算后的款项等等
	 */
	private BigDecimal userMoney;


	/**
	 * 退货状态
	 */
	private Integer orderReturnStatus;


	/**
	 * 退款状态
	 */
	private Integer orderRefundStatus;


}
