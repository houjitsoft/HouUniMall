package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class JoinPintuanDetailDTO {

	/**
	 * 还差几人
	 */
	private Integer surplusNum; 
	/**
	 *用户创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createdTime; 

	/**
	 *商品名称
	 */
	private String commonName; 

	/**
	 *持续时间
	 */
	private Integer startHour; 

	/**
	 *剩余时间
	 */
	private Long overplusTime; 

	/**
	 *查询购买者信息
	 */
	private List<PintuanBuyerMassageDTO> buyerMessage; 

	/**
	 *活动结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date endTime; 

	/**
	 *0,等待成团。1为已成团，2为成团失败，3为已完成退款
	 */
	private Integer status; 

	/**
	 * 判断是否是自己的团：1是2否
	 */
	private Integer checkIsSelfMark=0;

	/**
	 *团长id
	 */
	private String markId;

	/**
	 *团长名称
	 */
	private String markUserName;



}
