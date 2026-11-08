package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class ReductionPolicyBaseListVO {

	/**
	 *店铺id
	 */
	private Integer shopId;
	/**
	 *店铺名
	 */
	private String shopName;
	/**
	 *店铺活动名称
	 */
	private String name;
	/**
	 *几人成团
	 */
	private Integer personNum;
	/**
	 *开团时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date startTime;
	/**
	 *结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date endTime;
	/**
	 *创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createdTime;
	/**
	 *更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;
	/**
	 *是否可用，1为可用,0不可用
	 */
	private Integer status;
	/**
	 *持续时长
	 */
	private Integer startHour;
	/**
	 *原价格
	 */
	private BigDecimal priceOri;
	/**
	 * 团购价格
	 */
	private BigDecimal price;
	/**
	 * 单独购买价格
	 */
	private BigDecimal priceOne;
	/**
	 * 商品详情页地址
	 */
	private String goodsDetailUrl;
	/**
	 *
	 */
	private Integer pintuanId;
	/**
	 *排序
	 */
	private Integer displayOrder;
	/**
	 * 商品id
	 */
	private Integer goodsId;






}
