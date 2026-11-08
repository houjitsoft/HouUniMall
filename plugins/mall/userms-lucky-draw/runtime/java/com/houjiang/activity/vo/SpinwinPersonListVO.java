package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class SpinwinPersonListVO {

	/**
	 * 
	 */
	private Integer id;
	/**
	 *奖项id
	 */
	private Integer prizeId; 
	/**
	 *活动id
	 */
	private Integer activityId; 
	/**
	 *用户id
	 */
	private String userId; 
	/**
	 *用户名称
	 */
	private String userName; 
	/**
	 *用户头像
	 */
	private String userAvatar; 
	/**
	 *抽奖时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateDate; 
	/**
	 *奖项等级
	 */
	private String prizeGrade; 
	/**
	 *奖项名称
	 */
	private String prizeName; 
	/**
	 *活动标题
	 */
	private String activityTitle; 



}
