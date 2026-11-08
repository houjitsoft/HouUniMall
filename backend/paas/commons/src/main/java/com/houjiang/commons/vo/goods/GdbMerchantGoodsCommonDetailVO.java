package com.houjiang.commons.vo.goods;


import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class GdbMerchantGoodsCommonDetailVO {
	private Integer commonId;

	private String commonBody;
	/**
	 * 商品属性
	 */
	private List<ManageGoodsPropertyAddVO> propertys;
	/**
	 * 关联板式
	 */
	private ManageGoodsFormatResultVO commonFormatidTop;

	private ManageGoodsFormatResultVO commonFormatidBottom;

	private String brandName;

	private List<GdbAttributeAssionParamVO> attributeParam;




}
