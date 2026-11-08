package com.houjiang.commons.vo.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopdpexpressVO {

	/**
	 * 快递ID
	 */
	private Integer expressId;

	/**
	 * 店铺ID
	 */
	private Integer shopId;

	/**
	 * 客户名称
	 */
	private String customerName;

	/**
	 * 客户密码
	 */
	private String customerPwd;

	/**
	 * 月结编码
	 */
	private String monthCode;

	/**
	 * 发件网点
	 */
	private String sendSite;

	/**
	 * 发件员工
	 */
	private String sendStaff;

	/**
	 * 物流编码
	 */
	private String logisticCode;




}