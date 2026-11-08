package com.houjiang.activity.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class DrawTimesDetailsVO {

	/**
	 * 大转盘用户抽奖次数
	 */
	private Integer timeId;
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
	 * 今日已抽奖次数
	 */
	private Integer numSweepstakesToday;

}
