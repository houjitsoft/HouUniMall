package com.houjiang.commons.vo.shop;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class ShopTreasureGradeVO {

	/** 聚宝等级:1银牌2金牌3vip4钻石' */
	private Integer treasureGradeId;
	/** 聚宝等级:1银牌2金牌3vip4钻石' */
	private String treasureGrade;
	/** 聚宝付款信息1:尚未缴纳2:已缴纳3审核中4审核失败 */
	private Integer treasureStatus;
	/** 聚宝付款金额 */
	private BigDecimal treasurePayment;
	/** 聚宝商家缴纳备注信息 */
	private String treasurePaymentRemark;
	/** 付款凭证 */
	private String treasureJoinVoucher;

	/** 平台审核原因 */
	private String treasurePaymentReason;


}
