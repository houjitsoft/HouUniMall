package com.houjiang.activity.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class SpinWinDrawDetailVO {
	/**
	 * 参与人数是否显示：1-显示，2-隐藏
	 */
	private Integer partakeNumIsDisplay;
	/**
	 * 参与人数
	 */
	private Integer partakeNum;
	/**
	 * 参与人数限制是否限制：1-不限，2-限制
	 */
	private Integer partakeNumIsLimit;
	/**
	 * 参与人数限制
	 */
	private Integer partakeNumLimit;
	/**
	 * 总抽奖机会:是否限制：1-不限，2-限制
	 */
	private Integer totalSweepstakesIsLimit;
	/**
	 * 总抽奖机会
	 */
	private Integer totalSweepstakesLimit;
	/**
	 * 转盘背景
	 */
	private String turntableBackground;
	/**
	 * 每人中奖次数
	 */
	private Integer winningTimesPerPerson;



}
