package com.houjiang.activity.vo;

import cn.hutool.core.util.DesensitizedUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.activity.HouRedpacketTemplateVO;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class PrizeRecordResultVO extends HouRedpacketTemplateVO {
	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 用户名
	 */
	private String userName;
	/**
	 * 头像地址
	 */
	private String userAvatar;
	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateDate;

	/**
	 * 礼品名称
	 */
	private String giftName;

	/**
	 * 礼品是否是红包1-是,2-否
	 */
	private String giftIsRedpacket;

	/**
	 * 礼品图片
	 */
	private String giftImage;
	/**
	 * 是否中奖 1中奖2不中奖
	 */
	private Integer isWin;
	/**
	 * 1-未用,2-已用,3-过期
	 */
	private Integer redpacketState;


	public String getUserName() {
		// 处理用户名
		if (null != userName) {
			userName = DesensitizedUtil.idCardNum(userName,1,1);
		}
		return userName;
	}

}
