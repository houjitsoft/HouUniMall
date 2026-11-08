package com.houjiang.commons.vo.goods;

import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class ReceiveGoodsDetailsVO {
	private Integer shopId;

	private Integer plusGoods;

	private String shopName;

	private Integer commonId;

	private Integer goodsId;

	private String goodsName;
	/**
	 * 商品图片
	 */
	private String goodsImage;

	private BigDecimal goodsPrice;
	/**
	 * 供应商商品指导价
	 */
	private BigDecimal guidePrice;

	private Integer goodsStock;
	/**
	 * 规格名称
	 */
	private String commonSpecName;
	/**
	 * 商品规格
	 */
	private String goodsSpecChineseValue;
	/**
	 * 1-上架 2-下架 0店铺关闭手动下架
	 */
	private Integer cartStatus;
	/**
	 * 最大购买数量
	 */
	private Integer goodsMaxSale;
	/**
	 * 最小购买数量
	 */
	private Integer goodsMinSale;
	/**
	 * 商品审核 1通过，0未通过，10审核中
	 */
	private Integer commonVerify;
	/**
	 * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架
	 */
	private Integer commonState;

	private Integer shopInvoices;
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
	 */
	private Integer commonIsXian;
	/**
	 * 每人限购 0代表不限购
	 */
	private Integer commonLimit;
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
	private String  periodOfValidity;
	/**
	 * 过期退款 0 是 1 否
	 */
	private Integer pastDue;
	/**
	 * 0是1否
	 */
	private Integer integralGoods;

	private Integer needIntegral;

	private Integer commonIsVirtual;
	/**
	 * 活动状态
	 */
	private Integer activityGoodsState;
	/**
	 * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）
	 */
	private Integer dealerGoodsFlag;

	/**
	 * 商品类型1普通商品   2供应商商品
	 */
	private Integer goodsType;






}
