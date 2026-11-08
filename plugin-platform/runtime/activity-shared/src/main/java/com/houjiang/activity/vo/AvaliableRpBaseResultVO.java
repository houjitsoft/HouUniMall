package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class AvaliableRpBaseResultVO {

	/**
	 * 红包编码
	 */
	private Integer redpacketId;
	/**
	 *红包模板id
	 */
	private Integer redpacketTId;
	/**
	 * 红包标题
	 */
	private String redpacketTitle;
	/**
	 * 红包描述
	 */
	private String redpacketDesc;
	/**
	 *红包有效期开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketStartDate;
	/**
	 *红包有效期结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketEndDate;
	/**
	 *红包价格
	 */
	private Integer redpacketPrice;
	/**
	 *红包所有者id
	 */
	private String redpacketOwnerId;
	/**
	 *
	 */
	private Integer avaliableRp;
	/**
	 *红包使用商品分类限制，默认为空，不限分类，多个商品分类ID之间用逗号分隔
	 */
	private String goodsCatLimit;
	/**
	 * 店铺红包id
	 */
	private String shopLimit;
	/**
	 *店铺id
	 */
	private String shopId;

	/**
	 * 红包类型，1-新人注册红包，2-普通红包，3-单品红包 4-店铺红包 5-单品新人红包 默认2
	 */
	private Integer redpacketTType;
	/**
	 * 红包使用时的订单限额
	 */
	private Integer redpacketTOrderlimit;
	/**
	 * 1-未用,2-已用,3-过期
	 */
	private Integer redpacketState;
	/**
	 * 红包领用端口 0无限制 1PC 2wap 3安卓 5ios 6微信小程序 7wap浏览器
	 */
	private String redpacketUsePlatform;

}
