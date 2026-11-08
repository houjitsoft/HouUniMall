package com.houjiang.activity.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class UserGetVoucherUpdateParamVO extends HouVoucherTemplateVO {

	/**
	 * 代金券所有者id
	 */
	private String voucherOwnerId;

	/**
	 *代金券所有者名称
	 */
	private String voucherOwnerName;
	/**
	 * 代金券状态(1-未用,2-已用,3-过期,4-收回)
	 */
	private Integer voucherState;

	/**
	 *代金券发放日期
	 */
	private Integer voucherActiveDate;

	/**
	 * 每人限领张数
	 */
	private Integer voucherTEachlimit;


}
