package com.houjiang.goods.vo;

import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
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
	 * 商品名
	 */
	private String commonName;
	/**
	 * 副标题，即商品广告词
	 */
	private String commonPromotionTips;
	/**
	 * 商品价格
	 */
	private BigDecimal commonPrice;
	/**
	 * 销售量
	 */
	private Integer commonSalenum;
	/**
	 * 市场价
	 */
	private BigDecimal commonMarketPrice;
	/**
	 * 商品图片
	 */
	private String commonImage;
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
	 */
	private Integer commonIsXian;
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
	 */
	private Integer activityStatus;
	/**
	 * 活动详情
	 */
	private ActivityGoodsDetailDTO activityGoodsDetail;
	/**
	 * 角标图片
	 */
	private String cornerUrl;


}
