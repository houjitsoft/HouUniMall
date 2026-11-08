package com.houjiang.activity.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class CheckReceivedVcOrNotResultVO {

	/**
	 * 代金券状态(1-未用,2-已用,3-过期,4-收回)
	 */
	private Integer voucherState;
	/**
	 *  代金券模版编号
	 */
	private Integer voucherTId;



}
