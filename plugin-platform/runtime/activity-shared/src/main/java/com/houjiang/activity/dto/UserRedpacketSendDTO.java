package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class UserRedpacketSendDTO {

	/**
	 * 红包所有者id
	 */
	private String redpacketOwnerId;
	/**
	 *红包所有者名称
	 */
	private String redpacketOwnerName;
	/**
	 *红包模板id
	 */
	private Integer redpacketTId;
	/**
	 *红包标题
	 */
	private String redpacketTTitle;
	/**
	 *红包描述改为用户须知
	 */
	private String redpacketTDesc;
	/**
	 *红包有效期开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketTStartDate;
	/**
	 *红包有效期结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketTEndDate;
	/**
	 *发放日期
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date redpacketActiveDate;
	/**
	 *红包面额
	 */
	private Integer redpacketTPrice;
	/**
	 *红包使用时的订单限额
	 */
	private Integer redpacketTOrderlimit;
	/**
	 *红包状态(1-未用,2-已用,3-过期)
	 */
	private Integer redpacketState;
	/**
	 *红包总数
	 */
	private Integer redpacketTTotal;
	/**
	 *模版已发放的红包数量
	 */
	private Integer redpacketTGiveout;
	/**
	 *红包类型，1-新人注册红包，2-普通红包，3-单品红包 4-店铺红包 5-多品新人红包 6-发放的红包 默认2',
	 */
	private Integer redpacketType;
	/**
	 *店铺红包
	 */
	private String shopLimit;
	/**
	 *单品红包存放
	 */
	private String goodsCatLimit;
	/**
	 *红包来源：1-用户自己领取，2-其它用户转增
	 */
	private Integer redpacketFrom =2;
	/**
	 *用户信息
	 */
	private List<RedpacketSendUserDTO> userInfos;
	/**
	 *附件地址
	 */
	private String filePath;
	/**
	 *
	 */
	private String key;
	/**
	 *附件
	 */
	private MultipartFile file;
	/**
	 *获取此红包需满足的人数
	 */
	private Integer needSatisfyPersonNum;
	/**
	 *规则开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date ruleStartDate;


}
