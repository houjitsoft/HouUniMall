package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName GdsImageDetailResultVO
 * @Description GdsImageDetailResultVO
 * @Author Amos
 * @Date 2023/5/25 9:47
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdsImageDetailResultVO {

	/**
	 * 图片路径
	 */
	private String imagesImage;

	/**
	 * 图片显示顺序
	 */
	private Integer imagesDisplayorder;

	/**
	 * 图片是否为默认图片
	 */
	private Integer imagesIsDefault;



}
