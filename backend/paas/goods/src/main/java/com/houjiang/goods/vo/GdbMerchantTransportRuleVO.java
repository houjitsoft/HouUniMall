package com.houjiang.goods.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class GdbMerchantTransportRuleVO {
	/**
	 * 
	 */
	private Long id;
	/**
	 *区域城市名称
	 */
	private String areaName; 
	/**
	 *1按重量 2按件数 3按体积
	 */
	private Integer ruleType; 
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;
	/**
	 *模板id，与transport_template表中的id对应
	 */
	private Integer transportTemplateId; 
	/**
	 *物流类型，扩展字段
	 */
	private String logisticsType; 
	/**
	 *默认数量
	 */
	private BigDecimal defaultNum; 
	/**
	 *默认运费
	 */
	private BigDecimal defaultPrice; 
	/**
	 *增加数量
	 */
	private BigDecimal addNum; 
	/**
	 *增加运费
	 */
	private BigDecimal addPrice; 
	/**
	 *区域城市id
	 */
	private String areaIds; 
	/**
	 *是否默认规则0:否1:是
	 */
	private Integer defaultType; 

}
