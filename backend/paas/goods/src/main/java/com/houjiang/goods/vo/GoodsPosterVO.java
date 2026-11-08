package com.houjiang.goods.vo;


import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 不能随意修改
 *
 * @author Amos
 */
@Data
public class GoodsPosterVO {
	/**
	 * 商品id
	 */
	private Integer commonId;
	/**
	 * 商品id
	 */
	private String goodsId;
	/**
	 * 商品id
	 */
	private Integer goodsIdOne;
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
	 * 商品名
	 */
	private BigDecimal commonMarketPrice;
	/**
	 * 商品图片
	 */
	private String goodsImage;
	/**
	 * 商品状态 0下架，1正常，10违规（禁售）
	 */
	private Integer commonState;
	/**
	 * 评论数
	 */
	private Integer commonEvaluate;
	/**
	 * 收藏量
	 */
	private Integer commonCollect;
	/**
	 * 店铺名
	 */
	private String shopName;
	/**
	 * 店铺id
	 */
	private Integer shopId;
	/**
	 * 分类名
	 */
	private String catName;
	/**
	 * 商品库存
	 */
	private Integer commonStock;
	/**
	 * 分类id
	 */
	private Integer catId;
	/**
	 * 商品名称初始
	 */
	private String commonNameOrignial;
	/**
	 * 店铺商品主图 任意一个商品
	 */
	private String commonImage;

	/**
	 * 1:商城价2:小宝价格3:商城和小宝价组合
	 */
	private Integer commonType;
	/**
	 * 小宝价格
	 */
	private BigDecimal commonBabyPrice;

	/**
	 * 好评数
	 */
	private Integer qty; 
	/**
	 *  平均发货时间
	 */
	private String avgShippingTime;
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
	 * 虚拟商品 0 是 1 否
	 */
	private Integer virtualGoods;

	/**
	 * 虚拟商品有效期
	 */
	private String periodOfValidity;

	/**
	 * 过期退款 0 是 1 否
	 */
	private Integer pastDue;

	/**
	 * 积分商品 0 是 1 否
	 */
	private Integer integralGoods;

	/**
	 * 所需的积分
	 */
	private Integer needIntegral;

	/**
	 * 是否是询价商品
	 */
	private Integer isEnquiry;

	/**
	 * 商品库存
	 */
	private Integer goodsStock;

	/**
	 * 商品名
	 */
	private String goodsName;
	/**
	 * 品牌名称
	 */
	private String brandName;
	/**
	 * 商品规格
	 */
	private String goodsSpec;
	/**
	 * 商品价格
	 */
	private BigDecimal goodsPrice;
	/**
	 * 单位
	 */
	private String dw;
	/**
	 * 含税价格
	 */
	private BigDecimal taxInclusiveAmount;
	/**
	 * 税率
	 */
	private Double taxRate;
	/**
	 * 积分支付比例
	 */
	private Double pointsRate;

	/**
	 * 现金价格
	 */
	private BigDecimal cashPrice;
}
