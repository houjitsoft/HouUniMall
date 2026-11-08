package com.houjiang.commons.vo.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class AppSeckillGoodsQueryResultVO {

	/**
	 * 限时秒杀价格
	 */
	private BigDecimal seckillPrice;
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

	private Integer goodsLowerLimit;

	private Integer goodsUpperLimit;
	/**
	 * 秒杀库存
	 */
	private Integer seckillNum;
	/**
	 * 秒杀总库存
	 */
	private Integer seckillNums;

	private Integer seckillGoodsState;

	private String seckillTitle;

	private Integer seckillGoodsId;

	private Integer commonId;

	/** 商品名称 */
	private String goodsName;

	/** 商品价格 */
	private BigDecimal goodsPrice;


	/**
	 * 商品图片
	 */
	private String goodsImage;

	/** 规格名 */
	private String goodsSpec;
	/** 规格名 */
	private String specValueName;

	/** 秒杀id */
	private Integer seckillId;


	/** 商品库存 */
	private Integer goodsStock;

	private String commonName;
	/** 是否参加了活动 */
	private Integer commonIsXian;

	private Integer goodsId;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	/** 开始时间 */
	private Date startDateTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	/** 结束时间 */
	private Date endDateTime;

	private Integer shopId;







}
