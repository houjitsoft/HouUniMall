package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * @ClassName ManageGoodsFormatResultVO
 * @Description ManageGoodsFormatResultVO
 * @Author Amos
 * @Date 2023/5/25 9:47
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGoodsFormatResultVO {

	/**
	 * id
	 */
	private Integer id;

	/**
	 * 名字
	 */
	private String name;

	/**
	 * 位置
	 */
	private Integer position;

	/**
	 * 店铺id
	 */
	private Integer shopId;

	/**
	 * 内容
	 */
	private String content;

}
