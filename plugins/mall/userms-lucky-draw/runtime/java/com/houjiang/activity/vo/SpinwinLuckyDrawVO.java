package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class SpinwinLuckyDrawVO {

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 用户名
	 */
	private String userName;
	/**
	 * 用户头像
	 */
	private String userAvatar;

	/**
	 * 活动id
	 */
	private Integer activityId;

	/**
	 * 奖项id
	 */
	private Integer prizeId;
	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateDate;
	/**
	 * 总抽奖次数
	 */
	private Integer luckyDrawTimes;
	/**
	 * 剩余抽奖次数
	 */
	private Integer surplusDrawTimes;
	/**
	 * 中奖次数
	 */
	private Integer winningTimes;
	/**
	 * 今日抽奖次数
	 */
	private Integer numberOfDailyLottery;
	/**
	 * 参与人数
	 */
	private Integer partakeNum;
	/**
	 * 今日已抽奖次数
	 */
	private Integer numSweepstakesToday;

	/**
	 * 红包编码
	 */
	private Integer redpacketId;

}
