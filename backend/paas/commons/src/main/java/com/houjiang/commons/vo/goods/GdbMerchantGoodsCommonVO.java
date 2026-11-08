package com.houjiang.commons.vo.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GdbMerchantGoodsCommonVO {
	/** 自增 */
	private Integer commonId;

	/** 商品名称 */
	private String commonName;

	/** 副标题，即商品广告词 */
	private String commonPromotionTips;

	/** 分类id，前台传入 */
	private Integer catId;

	/** 分类名称，前台传入 */
	private String catName;

	/** 前台传入 */
	private String shopId;

	/** 前台传入 */
	private String shopName;

	private String shopCatId;

	private String shopGoodsCatId;

	private Integer shopSelfSupport;

	private Integer shopStatus;

	private Integer commonSpecName;

	private Integer brandId;

	private String brandName;

	private Integer typeId;

	private Integer type;

	private String commonImage;
	/**
	 * 视频
	 */
	private String commonVideo;

	private Integer commonIsScreen;
	/**
	 * 商品状态 0下架，1正常，10违规（禁售）
	 */
	private Integer commonState;

	private String commonStateRemark;

	private Integer commonVerify;

	private String commonVerifyRemark;

	private Date commonAddTime;

	private Date commonSellTime;

	private BigDecimal commonPrice;

	private BigDecimal commonMarketPrice;

	private BigDecimal commonCostPrice;

	private Integer commonStock;

	private Short commonLimit;

	private Integer commonIsMan;

	private Integer commonIsInvoice;

	private Integer commonAlarm;

	private String commonCode;

	private String commonPlatformCode;

	private BigDecimal commonCubage;

	private Integer commonCollect;

	private Integer commonEvaluate;

	private Integer commonSalenum;

	private Byte commonInvoices;

	private Integer commonIsReturn;

	private Integer commonIsRecommend;

	private Integer commonVirtualRefund;

	private Integer transportTypeId;

	private Integer commonType;

	private Integer commonIsLocked;

	private Integer commonShopContract1;

	private Integer commonShopContract2;

	private Integer commonShopContract3;

	private Integer commonShopContract4;

	private Integer commonShopContract5;

	private Integer commonShopContract6;

	private Integer commonIsDirectseller;

	private Integer productLockFlag;

	private Integer productAgentId;

	private Integer districtId;

	private Integer commonParentId;

	private BigDecimal commonCpsCommission;

	private Integer commonGoodsFrom;

	private Integer transportAreaId;

	private Integer transportTemplate;

	private Integer goodsId;

	private String commonProperty;

	private String commonSpecValue;

	private String commonLocation;

	private Integer restrictionTimes;

	private Integer restrictionHours;

	/**
	 * 商品多规格
	 * {param_name:'规格1',sort: 显示顺寻,is_common:是否是公用规格,ref_spec_id:通用规格id,values:规格值属性},
	 */
	private String specParam;
	/**
	 * 导入excel时 写的编码
	 */
	private String importNumber;

	/**
	 * 供应商店铺id
	 */
	private String supplyShopId;

	private String commonUpdateBy;

	private String orderOperator;

	private Integer cornerId;

	private Integer virtualGoods;

	private String periodOfValidity;

	private Integer pastDue;

	private Integer commonClick;
	/**
	 * 创建者
	 */
	private String createUser;

	private String createTime;

	private String updateUser;

	private Integer goodsType;

	private Integer dealerGoodsFlag;

	private Date commonUpdateTime;

	private Date orderOperatorTime;

	private Date updateTime;
	/**
	 * 是否删除0:否;1:是
	 */
	private Integer isDelete;

	private String catIds;

	private Integer dealerType;

	/**
	 * 0 正常  1
	 */
	private Integer commonDistributorFlag;

	private BigDecimal goodsRecommendedMinPrice;

	private BigDecimal goodsRecommendedMaxPrice;
	/**
	 * 发货时效
	 */
	private Integer deliveryTime;
	/**
	 * 售后服务JSON
	 */
	private String afterService;

	public GdbMerchantGoodsCommonVO(Integer commonId, Integer commonState, Integer commonStock,Integer commonDistributorFlag,String specParam) {
		this.commonId = commonId;
		this.commonState = commonState;
		this.commonStock = commonStock;
		this.commonDistributorFlag = commonDistributorFlag;
		this.specParam = specParam;
	}

	public GdbMerchantGoodsCommonVO(Integer commonId, Integer commonState, Integer commonStock,Integer commonDistributorFlag,String specParam,String commonSpecValue) {
		this.commonId = commonId;
		this.commonState = commonState;
		this.commonStock = commonStock;
		this.commonDistributorFlag = commonDistributorFlag;
		this.specParam = specParam;
		this.commonSpecValue = commonSpecValue;
	}
}
