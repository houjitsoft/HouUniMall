package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class DiscountGoodsListVO {
	/**
	 * 限时折扣商品id
	 */
	private Integer discountGoodsId;
	/**
	 * 商品编号
	 */
	private Integer goodsId;
	/**
	 * 商品id
	 */
	private Integer commonId;
	/**
	 * 店铺编号
	 */
	private Integer shopId;
	/**
	 * 商品名称
	 */
	private String goodsName;
	/**
	 * 商品原价
	 */
	private BigDecimal goodsPrice;
	/**
	 * 限时折扣价格
	 */
	private BigDecimal discountPrice;
	/**
	 * 商品图片
	 */
	private String goodsImage;
	/**
	 * 折扣
	 */
	private BigDecimal discountAmount;
	/**
	 * 规格名
	 */
	private String specValueName;
	/**
	 * 规格名
	 */
	private String goodsSpec;
	/**
	 * 限时折扣id
	 */
	private Integer discountId;
	/**
	 * 是否参加了活动  0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
	 */
	private Integer commonIsXian;
	/**
	 * 结束时间
	 */
	private Date endDateTime;
	/**
	 * 开始时间
	 */
	private Date startDateTime;




}
