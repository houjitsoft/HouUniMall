package com.houjiang.goods.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class GdbMerchantTransportTemplateVO {
	/**
	 *
	 */
	private Integer id;
	/**
	 *模板名称
	 */
	private String name;
	/**
	 *
	 */
	private String shopId;
	/**
	 *状态(默认模板)，1开启(是)，0关闭(否)
	 */
	private Integer status;
	/**
	 *固定运费(暂未使用，可不传)
	 */
	private BigDecimal constPrice;
	/**
	 * 是否包邮 0不包邮 1全国包邮 2自定义
	 */
	private Integer isFreePostage;
	/**
	 *规则类型,1重量2件数
	 */
	private Integer ruleType;
	/**
	 *满多少kg/件包邮
	 */
	private Double freePostageCond;
	/**
	 * 包邮区域
	 */
	private String freePostageAreas;
	/**
	 *低于包邮条件的运费
	 */
	private BigDecimal shippingPrice;
	/**
	 *详细设置
	 */
	private List<GdbMerchantTransportRuleVO> transportRules; 

}
