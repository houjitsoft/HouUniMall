package com.houjiang.commons.vo.order;


import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class WapShopShippingQueryResultVO {

	private Integer shopId;

	private String shopName;

	private BigDecimal shippingFee;

	/** 店铺免额度 */
	private BigInteger shopFreeShipping;

	private String buyerMessage;

	/** 是否开具增值税发票 1是，0否 */
	private Integer shopInvoices;

	/** 所在地 */
	private Integer districtId;

	private List<WapShopShippingResultVO> goodsBaseList;

	/** 来源：手机端 1PC 2wap 3安卓 5ios 6微信小程序 7wap浏览器 */
	private Integer platForm;

	/** 聚宝等级:0非聚宝店铺1银牌2金牌3vip4钻石 */
	private Integer treasureGradeId;

	private Boolean shopSelfSupport;

	/**
	 * 经销商店铺id
	 */
	private Integer dealerShopId;

	/**
	 * 运费数组
	 */
	private List<BigDecimal> shippingFeeList;

	/**
	 * 高亮数组
	 */
	private List<Integer> highlightList;

	/**
	 * 原始单号
	 */
	private String originalOrder;

	/**
	 * 发票功能开关状态:0-关闭;1-开启
	 */
	private Integer invoiceSwitchStatus;

	/**
	 * 质保金状态 0 无需缴纳 1 已缴纳 2 请及时补充 3 即将限权 4 已限权
	 */
	private Integer retentionStatus;
}
