package com.houjiang.commons.vo.order;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class WapShippingTransportResultVO {

	private Integer id;
	/**
	 * 是否包邮 0不包邮 1全国包邮 2自定义
	 */
	private Integer isFreePostage;
	/**
	 * 规则类型,1重量2件数
	 */
	private Integer ruleType;
	/**
	 * 满多少kg/件包邮
	 */
	private Double freePostageCond;
	/**
	 * 包邮区域
	 */
	private String freePostageAreas;
	/**
	 * 不足包邮件数或重量的运费
	 */
	private BigDecimal shippingPrice;
	/**
	 * 区域城市id
	 */
	private String areaIds;
	/**
	 * 默认数量
	 */
	private BigDecimal defaultNum;
	/**
	 * 默认运费
	 */
	private BigDecimal defaultPrice;
	/**
	 * 增加数量
	 */
	private BigDecimal addNum;
	/**
	 * 增加运费
	 */
	private BigDecimal addPrice;
	/**
	 * 运费单价
	 */
	private BigDecimal shippingRate;
	/**
	 * 处理费
	 */
	private BigDecimal processingFee;
	/**
	 * 是否默认规则0:否1:是
	 */
	private Integer defaultType;

}
