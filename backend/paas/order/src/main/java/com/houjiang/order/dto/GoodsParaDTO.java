package com.houjiang.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 商品入参
 *
 * @author Amos
 *
 */
@Data
public class GoodsParaDTO extends PageHelper {
	/**
	 * 门店ID
	 */
	private Integer shopGateId;

	/**
	 * 用户名称
	 */
	private String userName;

	/**
	 * 分数
	 */
	private Integer score;

	/**
	 * 商品名称
	 */
	private String goodsName;

	/**
	 * 商店名称
	 */
	private String shopName;
	/**
	 * 开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;
	/**
	 * 结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;

}
