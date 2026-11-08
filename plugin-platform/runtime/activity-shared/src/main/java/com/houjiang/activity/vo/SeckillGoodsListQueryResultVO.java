package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class SeckillGoodsListQueryResultVO {
	private Integer commonId;
	/**
	 * 商品名称
	 */
	private Integer goodsId;
	/**
	 * 商品名称
	 */
	private String commonName;
	/**
	 * 商品图片
	 */
	private String commonImage;
	/**
	 * 商品价格
	 */
	private BigDecimal commonPrice;
	/**
	 * 限时折扣价格
	 */
	private BigDecimal seckillPrice;
	/**
	 * 库存
	 */
	private String commonSalenum;
	/**
	 * 广告词
	 */
	private String commonPromotionTips;

	/**
	 * 开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date goodsStartTime;

	/**
	 * 结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date goodsEndTime;

	/**
	 * 限购数量
	 */
	private Integer seckillUpperLimit;


	/**
	 * 购买上限0为不限制
	 */
	private Integer goodsUpperLimit;

	/**
	 * 秒杀成功数
	 */
	private Integer seckillNum;

	/**
	 * 商品评论数
	 */
	private Integer commonEvaluate;

	/**
	 * 状态， 1-正常/2-结束/3-管理员关闭
	 */
	private Integer seckillGoodsState;

	/**
	 * 店铺名
	 */
	private String shopName;

	/**
	 * 店铺id
	 */
	private Integer shopId;
	/**
	 * 1开始 0未开始
	 */
	private Integer activityStartStatus;
	/**
	 * 剩余时间
	 */
	private Long overplusTime;
	/**
	 * 角标图片
	 */
	private String cornerUrl;

	/**
	 * 秒杀ID
	 */
	private Integer seckillId;

	/**
	 * 当前活动已购买数量
	 */
	private Integer buyNum;
}
