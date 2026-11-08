package com.houjiang.base.vo.index;

import com.houjiang.base.vo.ActivityGoodsDetailVO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName SecordNameListVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class WapHallGoodsQueryVO {
	/**
	 * 商品id
	 */
	private Integer commonId;
	/**
	 * 商品id
	 */
	private Integer goodsId;
	/**
	 * 商品名称
	 */
	private String commonName;

	private String commonPromotionTips;

	private BigDecimal commonPrice;

	private Integer commonSalenum;

	private BigDecimal commonMarketPrice;

	private String commonImage;

	private Integer commonIsXian;
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
	 */
	private Integer activityStatus;
	/**
	 * 活动详情
	 */
	private ActivityGoodsDetailVO activityGoodsDetail;
	/**
	 * 角标图片
	 */
	private String cornerUrl;


}
