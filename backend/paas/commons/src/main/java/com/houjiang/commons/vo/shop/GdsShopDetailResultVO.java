package com.houjiang.commons.vo.shop;

import lombok.Data;

/**
 * @ClassName GdsShopDetailResultVO
 * @Description GdsShopDetailResultVO
 * @Author Amos
 * @Date 2023/5/25 9:47
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdsShopDetailResultVO {

	private Integer shopId;

	private String shopName;
	/**
	 * 用户id
	 */
	private String userId;
	/**
	 * 商家用户名
	 */
	private String userName;
	/**
	 * 店铺logo
	 */
	private String shopLogo;
	/**
     * 商品数量
	 */
	private Integer shopCount;
	/**
     * 粉丝数
	 */
	private Integer shopCollect;

	private String wapShopLogo;

	private Double overallScore;
	/**
     * 是否开具增值税发票 1是，0否
	 */
	private Integer shopInvoices;
	/**
     * 买家电话
	 */
	private String shopTel;
	/**
     * 1:已缴纳2:尚未缴纳3审核中
	 */
	private Integer depositStatus;
	/**
     * 是否自营
	 */
	private String shopSelfSupport;

	private Integer invoiceActive;
	/**
	 * 所在地，使用最后一级分类
	 */
	private Integer districtId;
	/**
	 * 店铺类型: 1-卖家店铺; 2:供应商店铺 3 品牌店铺
	 */
	private Integer shopType;
	/**
	 * 店铺等级
	 */
	private Integer shopGradeId;
}
