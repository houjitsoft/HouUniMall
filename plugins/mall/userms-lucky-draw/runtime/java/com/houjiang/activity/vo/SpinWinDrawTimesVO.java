package com.houjiang.activity.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class SpinWinDrawTimesVO {
	/**
	 * 参与人数
	 */
	private Integer partakeNum = 0;
	/**
	 * 剩余抽奖次数
	 */
	private Integer surplusDrawTimes;
	/**
	 * 转盘背景
	 */
	private String turntableBackground;
	/**
	 * 参与人数限制是否限制：1-不限，2-限制
	 */
	private Integer partakeNumIsLimit;
	/**
	 * 今日剩余次数
	 */
	private Integer surplusTimes;


}
