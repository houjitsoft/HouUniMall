package com.houjiang.commons.vo.goods;

import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 批发市场 商品信息
 * @author Amos
 */
@Data
public class GdsListQueryResultVO {

	/**
	 * 通用ID
	 */
	private Integer commonId;

	/**
	 * 商品ID
	 */
	private String goodsId;

	/**
	 * 商品ID（第一个）
	 */
	private Integer goodsIdOne;

	/**
	 * 通用名称
	 */
	private String commonName;

	/**
	 * 通用促销提示
	 */
	private String commonPromotionTips;

	/**
	 * 通用价格
	 */
	private BigDecimal commonPrice;

	/**
	 * 通用销售数量
	 */
	private Integer commonSalenum;

	/**
	 * 通用市场价格
	 */
	private BigDecimal commonMarketPrice;

	/**
	 * 通用图片
	 */
	private String commonImage;

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
	 * 店铺名称
	 */
	private String shopName;

	/**
	 * 店铺ID
	 */
	private Integer shopId;

	/**
	 * 分类名称
	 */
	private String catName;

	/**
	 * 通用库存
	 */
	private Integer commonStock;

	/**
	 * 已售
	 */
	private Integer goodsSalenum;
	/**
	 * 拼团价格
	 */
	private BigDecimal price;
	/**
	 * 拼团单独购买价格
	 */
	private BigDecimal priceOne;
	/**
	 * 是否自营
	 */
	private String shopSelfSupport;
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
	 * 平均发货时间
	 */
	private String avgShippingTime;

	private Integer commonIsXian;
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
	 */
	private Integer activityStatus;
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
	 * 是否需要经销审核 1是 2否
	 */
	private Integer sellCheck;

	/**
	 * 1一键上架 2立即购买
	 */
	private Integer  sellDeliver;

	/**
	 * 操作类型 1一键上架  2立即购买 3申请经销商 4已经销
	 */
	private Integer operateType;

	/**
	 * 上架id
	 */
	private Integer putawayId;
	/**
	 * 门店id
	 */
	private Integer shopGateId;
	/**
	 * 门店名称
	 */
	private String shopGateName;
	/**
	 * 经度
	 */
	private String longitude;
	/**
	 * 纬度
	 */
	private String latitude;

	private ActivityGoodsDetailDTO activityGoodsDetailDTO;




	public void setOperateType2(GdsListQueryResultVO goods) {
		if (null != goods.getSellCheck()) {
			if (goods.getSellCheck().intValue() == 1) {
				this.operateType = CommonType.supplierGoodsStatus.APPLY.getCode();
			} else {
				this.operateType = goods.getSellDeliver();
			}
		}
	}
}
