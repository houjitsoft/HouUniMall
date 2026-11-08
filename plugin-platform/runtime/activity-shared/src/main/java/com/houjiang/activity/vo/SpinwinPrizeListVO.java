package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class SpinwinPrizeListVO {


	/**
	 * 奖项id
	 */
	private Integer prizeId;
	/**
	 *奖项等级
	 */
	private String prizeGrade; 
	/**
	 *奖项名称
	 */
	private String prizeName; 
	/**
	 * 礼品id
	 */
	private Integer giftId;
	/**
	 *活动id
	 */
	private Integer activityId; 
	/**
	 *礼品总数
	 */
	private Integer giftNum; 
	/**
	 *更新人
	 */
	private String updateBy; 
	/**
	 *更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateDate; 
	/**
	 *是否中奖1中奖2谢谢参与
	 */
	private Integer isWin; 
	/**
	 *奖项排序
	 */
	private Integer displayOrder; 
	/**
	 *给出的礼品数量
	 */
	private Integer giveOutGiftNum; 
	/**
	 *活动名称
	 */
	private String activityTitle;



}
