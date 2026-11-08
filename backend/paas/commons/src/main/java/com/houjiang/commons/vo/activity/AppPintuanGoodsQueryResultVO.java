package com.houjiang.commons.vo.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class AppPintuanGoodsQueryResultVO {

	private Integer pintuanId;
	/**
	 * 几人成团
	 */
	private Integer personNum;
	/**
	 * 开团时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date startTime;
	/**
	 * 结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date endTime;

	private Integer detailId;
	/**
	 * 团购价格
	 */
	private BigDecimal price;
	/**
	 * 单独购买价格
	 */
	private BigDecimal priceOne;
	/**
	 * 剩余时间
	 */
	private Long overplusTime;
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团
	 */
	private Integer commonIsXian;
	/**
	 * 是否可用，1正常 0结束 2 待开启
	 */
	private Integer status;
}
