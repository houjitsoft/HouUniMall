package com.houjiang.commons.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class WapShopShippingResultVO {

	/** 同一模板总运费 */
	private BigDecimal grossFreight;

	private List<WapShippingQueryResultVO> goodsBaseList;



}
