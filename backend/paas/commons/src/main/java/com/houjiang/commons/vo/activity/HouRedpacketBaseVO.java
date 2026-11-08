package com.houjiang.commons.vo.activity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class HouRedpacketBaseVO {
	private Integer redpacketId;

	private String redpacketCode;

	private Integer redpacketTId;

	private String redpacketTitle;

	private String redpacketDesc;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date redpacketStartDate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date redpacketEndDate;

	private Integer redpacketPrice;

	private Integer redpacketTOrderlimit;

	private Integer isAllowTransfer;

	private Integer redpacketState;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date redpacketActiveDate;

	private String redpacketOwnerId;

	private String redpacketOwnerName;

	private String redpacketOrderId;

	private Integer redpacketTransferState;

	private Integer redpacketFrom;

	private Integer avaliableRp;

	/** 单品红包存放 */
	private String goodsCatLimit;

	/** 单品红包所属shopid */
	private String shopId;

	/**  */
	private String shopLimit;

	/** 红包类型，1-新人注册红包，2-普通红包，3-多品红包 4-店铺红包 5-多品新人红包 6-专属红包 默认2 */
	private Integer redpacketTType;

	/** 多品红包跳转类型 3:链接;12:为你推荐13:特产推荐;14:新品推荐;19:新人专区;20:平台活动专区;21:9.9包邮专区;22:品牌生活;23:好货溯源;24:蜂狂特价;25:猜你喜欢;26:时令果蔬;27:节日钜惠;28:限时折扣;29:超级拼团;34:多品红包列表 */
	private Integer actionType;

	/** 多品红包类型：1多品配置2专区 */
	private Integer manyRedpacketType;

	/** 可用的商品列表 */
	private List<Integer> goodsIdList;


}
