package com.houjiang.commons.vo.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class AppDiscountGoodsQueryResultVO {

	private Integer discountGoodsId;
	/**
	 * 限时折扣价格
	 */
	private BigDecimal discountPrice;
	/**
	 * 开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date goodsStartTime;
	/**
	 * 结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date goodsEndTime;
	/**
	 * 剩余时间
	 */
	private Long overplusTime;
	/**
	 * 购买上限
	 */
	private Integer goodsUpperLimit;
	/**
	 * 购买下限，0为不限制
	 */
	private Integer goodsLowerLimit;
	/**
	 * 已购买数据
	 */
	private Integer goodsBuyCount;

	private Integer discountUpperTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date preStartTime;

	private Integer discountGoodsState;

	private BigDecimal discountAmount;

	/**
	 * 限时折扣标题
	 */
	private String discountTitle;

	/**
	 * 限时折扣id
	 */
	private Integer discountId;


}
