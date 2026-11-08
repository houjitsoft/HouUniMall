package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class HouVoucherTemplateVO {
	/**
	 * 代金券模版编号
	 */
	private Integer voucherTId;
	/**
	 * 代金券模版名称
	 */
	private String voucherTTitle;
	/**
	 * 代金券模版有效期开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date voucherTStartDate;
	/**
	 * 代金券模版有效期结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date voucherTEndDate;
	/**
	 * 代金券模版面额
	 */
	private BigDecimal voucherTPrice;
	/**
	 * 代金券使用时的订单限额
	 */
	private String voucherTLimit;

	/**
	 * 店铺id
	 */
	private Integer shopId;

	/**
	 * 店铺名
	 */
	private String shopName;
	/**
	 * 代金券模版的创建者id
	 */
	private String voucherTCreatorId;
	/**
	 * 代金券模版状态(1-有效,2-失效)
	 */
	private Integer voucherTState;
	/**
	 * 模版可发放的代金券总数
	 */
	private Integer voucherTTotal;
	/**
	 * 模版已发放的代金券数量
	 */
	private Integer voucherTGiveout;
	/**
	 * 模版已经使用过的代金券
	 */
	private Integer voucherTUsed;
	/**
	 * 模版的创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date voucherTAddDate;
	/**
	 * 模版的最后修改时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date voucherTUpdateDate;
	/**
	 * 每人限领张数
	 */
	private Integer voucherTEachlimit;
	/**
	 * 是否领取： 0未领取1未用2不可领取3已用4过期5回收
	 */
	private Integer receiveOrNot = 0;
	/**
	 * 代金券类型：1-单个店铺，2-多店铺参与
	 */
	private Integer voucherTType;
	/**
	 * 代金券类型：1 是店铺优惠券 2是商品优惠券
	 */
	private Integer isShopVoucher;

}
