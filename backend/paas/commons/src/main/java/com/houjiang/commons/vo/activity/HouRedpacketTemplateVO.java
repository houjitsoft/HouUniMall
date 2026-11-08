package com.houjiang.commons.vo.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
/**
 * @ClassName HouRedpacketTemplateVO
 * @Description 红包表
 * @Author Amos
 * @Date 2023-05-23 13:21:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class HouRedpacketTemplateVO {

	private Integer redpacketTId;
	/**
	 * 红包类型，1-新人注册红包，2-普通红包，3-单品红包 4-店铺红包,5-单品新人红包 默认2
	 */
	private Integer redpacketTType;
	/**
	 * 红包模版名称
	 */
	private String redpacketTTitle;
	/**
	 * 红包模版描述
	 */
	private String redpacketTDesc;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketTStartDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketTEndDate;

	private Integer redpacketTPrice;

	private String shopLimit;

	private String goodsCatLimit;
	/**
	 * 红包使用时的消费限额
	 */
	private Integer redpacketTOrderlimit;

	private Integer isAllowTransfer;

	private Integer redpacketTState;

	private Integer redpacketTTotal;

	private Integer redpacketTGiveout;

	private Integer redpacketTUsed;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketTAddDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketTUpdateDate;

	private Integer redpacketTPoints;

	private Integer redpacketTEachlimit;

	private Integer redpacketTUserGradeLimit;

	private String redpacketTImg;
	/**
	 * 红包领取方式，1-积分兑换(默认)，2-卡密兑换，3-免费领取
	 */
	private Integer redpacketTAccessMethod;

	private Integer redpacketTRecommend;
	/**
	 * 0未领取1立即使用2不可领取3已用4过期5回收6尚未开始7已抢光
	 */
	private Integer receiveOrNot = 0;
	/**
	 * 多品红包类型：1多品配置2专区
	 */
	private Integer manyRedpacketType;
	/**
	 * 多品红包跳转类型
	 */
	private Integer actionType;
	/**
	 *  跳转名称
	 */
	private String actionName;
	/**
	 * 多品红包跳转地址
	 */
	private String manyRedpacketSkipUrl;
	/**
	 * 红包领用端口
	 */
	private String redpacketGetPlatform;
	/**
	 * 红包领用端口
	 */
	private long overplusTime;
	/**
	 * 领取进度 0-1 （已经领取 / 红包总数 ）
	 */
	private BigDecimal redPackageGetProgress;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date robRedpacketStartDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date robRedpacketEndDate;
	/**
	 * 红包是否倒计时1-否2-是
	 */
	private Integer redpacketIsBooking;

	/** 红包失效天数 */
	private Integer redTermofvalidity;
}
