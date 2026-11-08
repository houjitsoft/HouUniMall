package com.houjiang.commons.dto.shop;


import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopTreasureGradeUpdateDTO extends UserTokenBeanDTO {

	/** 聚宝等级:1银牌2金牌3vip4钻石' */
	private Integer treasureGradeId;
	/** 聚宝付款信息1:尚未缴纳2:已缴纳3审核中4审核失败 */
	private Integer treasureStatus;
	/** 聚宝付款金额 */
	private BigDecimal treasurePayment;
	/** 聚宝商家缴纳备注信息 */
	private String treasurePaymentRemark;
	/** 付款凭证 */
	private String treasureJoinVoucher;

	private String shopId;

	/** 平台审核原因 */
	private String treasurePaymentReason;


}
