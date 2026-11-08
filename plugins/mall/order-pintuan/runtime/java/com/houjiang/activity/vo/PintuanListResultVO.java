package com.houjiang.activity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class PintuanListResultVO {

	/**
	 * 拼团详情
	 */
	private Integer detailId; 
	/**
	 *创建时间 前端不需要
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createdTime; 
	/**
	 *当前参团人数
	 */
	private Integer num; 
	/**
	 *团长名称
	 */
	private String userName; 
	/**
	 *剩余时间
	 */
	private Long overplusTime; 
	/**
	 *团id
	 */
	private String markId; 
	/**
	 *用户头像
	 */
	private String userAvatar; 
	/**
	 *还差几人成团
	 */
	private Integer surplusNum; 
	/**
	 *持续时间
	 */
	private Integer startHour; 
	/**
	 * 活动结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date endTime; 
	/**
	 *判断是否是自己的团：1是2否
	 */
	private Integer checkIsSelfMark; 



}
