package com.houjiang.commons.vo.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouVoucherBaseVO {

	/**
	 * 代金券编号
	 */
	private Integer voucherId;
	/**
	 *代金券编码
	 */
	private String voucherCode; 
	/**
	 *代金券模版编号
	 */
	private Integer voucherTId; 
	/**
	 *代金券标题
	 */
	private String voucherTitle; 
	/**
	 *代金券描述
	 */
	private String voucherDesc; 
	/**
	 *代金券有效期开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date voucherStartDate; 
	/**
	 *代金券有效期结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date voucherEndDate; 
	/**
	 *代金券面额
	 */
	private BigDecimal voucherPrice;
	/**
	 *代金券使用时的订单限额
	 */
	private Double voucherLimit; 
	/**
	 *代金券的店铺id
	 */
	private Integer voucherShopId;
	/**
	 *代金券状态(1-未用,2-已用,3-过期,4-收回)
	 */
	private Integer voucherState; 

	/**
	 * 代金券发放日期
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date voucherActiveDate;

	/**
	 * 代金券所有者id
	 */
	private String voucherOwnerId; 
	/**
	 *代金券所有者名称
	 */
	private String voucherOwnerName;
	/**
	 *使用该代金券的订单编号
	 */
	private String voucherOrderId;
	/**
	 *可用金额
	 */
	private Integer available;
	/**
	 *店铺名
	 */
	private String shopName;
	/**
	 *代金券类型：1-单个店铺，2-多店铺参与
	 */
	private Integer voucherType;
	/**
	 *限制的商品
	 */
	private List<Integer> avaliableGoods;
	/**
	 * 商品图片
	 */
	private String commonImage;
	/**
	 *代金券类型：1 是店铺优惠券 2是商品优惠券
	 */
	private Integer isShopVoucher;
	/**
	 *代金券模版编号
	 */
	private List<Integer> voucherTIdList;
	/**
	 * 代金券
	 */
	private List<HouVoucherBaseVO> houVoucherBaseVOList;
	/**
	 * 代金券编号
	 */
	private String voucherNo;
	/**
	 * 商品图片数组
	 */
	private List<String> commonImageList;

	public HouVoucherBaseVO(String voucherTitle, BigDecimal voucherPrice, Double voucherLimit, StringBuilder voucherNo,
						   List<Integer> voucherTIdList, List<HouVoucherBaseVO> houVoucherBaseVOList,
						   List<String> commonImageList) {
		this.voucherTitle = voucherTitle;
		this.voucherPrice = voucherPrice;
		this.voucherLimit = voucherLimit;
		this.voucherNo = voucherNo.toString();
		this.voucherTIdList = voucherTIdList;
		this.houVoucherBaseVOList = houVoucherBaseVOList;
		this.commonImageList = commonImageList;
	}
}
