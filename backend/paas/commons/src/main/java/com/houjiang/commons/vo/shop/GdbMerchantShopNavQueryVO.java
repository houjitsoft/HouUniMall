package com.houjiang.commons.vo.shop;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 不能随意修改
 * @author Amos
 *
 */
@Data
public class GdbMerchantShopNavQueryVO {

	private Integer id;
	/**
	 * 导航名称
	 */
	private String title;
	/**
	 * 导航排序
	 */
	private Integer displayorder;
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;
	/**
	 * 店铺导航的外链URL
	 */
	private String url;
	/**
	 * 店铺导航外链是否在新窗口打开：0不开新窗口1开新窗口，默认是0
	 */
	private Integer target;
	/**
	 * 导航内容
	 */
	private String detail;
	/**
	 * 导航是否显示 0 不显示 1显示'
	 */
	private Integer status;




}
