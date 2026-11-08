package com.houjiang.commons.vo.order;

import lombok.Data;

import java.util.List;

/**
 * 店铺编号查询具体信息
 *
 * @author Amos
 *
 */
@Data
public class StoreInformationVO {
	/**
	 * 优惠券id
	 */
	private String couponId;
	/**
	 * 优惠券模板id
	 */
	private Integer couponTId;
	/**
	 * 优惠券价格
	 */
	private Double couponPrice;
	/**
	 * 店铺id
	 */
	private Integer shopId;
	/**
	 * 商品信息集合
	 */
	private List<GoodsInformationVO> goodsBaseList;
	/**
	 * 买家留言
	 */
	private String buyerMessage;
	/**
	 * 发票ID
	 */
	private Integer invoiceId;
	/**
	 * 发票信息
	 */
	private InvoiceEntityInfoVO invoiceInfo;
	/**
	 * 优惠券id
	 */
	private List<Integer> couponIdList;

	/**
	 * 发货店铺id
	 */
	private Integer supplierShopId;

	/**
	 * 发票ID
	 */
	private Integer useRebateType;

	/**
	 * 供应商商品订单拆分前的原始订单
	 */
	private String originalOrder;

}
