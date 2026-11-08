package com.houjiang.commons.vo.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class ManjianzengzheGoodsQueryResultVO {
	/**
	 * 活动ID
	 */
	private Integer promotionId;
	/**
	 * 开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date startTime;
	/**
	 * 结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date endTime;
	/**
	 * 商品价格
	 */
	private BigDecimal goodsPrice;
	/**
	 * 剩余时间
	 */
	private Long overplusTime;
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
	 */
	private Integer commonIsXian;


	/**
	 * 赠品skuId
	 */
	private Integer goodsId;
}
