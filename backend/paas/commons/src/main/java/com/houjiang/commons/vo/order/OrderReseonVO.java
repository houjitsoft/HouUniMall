package com.houjiang.commons.vo.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderReseonVO {
	/**
	 * 标记目的地
	 */
	private String MarkDestination;

	/**
	 * 原始名称
	 */
	private String OriginName;

	/**
	 * 物流编码
	 */
	private String LogisticCode;

	/**
	 * 承运商编码
	 */
	private String ShipperCode;

	/**
	 * 订单编码
	 */
	private String OrderCode;

	/**
	 * 快递鸟订单编码
	 */
	private String KDNOrderCode;

	/**
	 * 原始编码
	 */
	private String OriginCode;

	/**
	 * 包裹编码
	 */
	private String PackageCode;

	/**
	 * 分拣编码
	 */
	private String SortingCode;



}
