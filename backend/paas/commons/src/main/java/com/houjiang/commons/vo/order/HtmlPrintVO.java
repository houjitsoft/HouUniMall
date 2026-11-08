package com.houjiang.commons.vo.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HtmlPrintVO {
	/**
	 * 订单列表
	 */
	private List<OrderReseonVO> Order;

	/**
	 * 打印模板
	 */
	private String PrintTemplate;

	/**
	 * 电子商务ID
	 */
	private String EBusinessID;

	/**
	 * 唯一请求编号
	 */
	private String UniquerRequestNumber;

	/**
	 * 结果代码
	 */
	private String ResultCode;

	/**
	 * 原因
	 */
	private String Reason;


}
