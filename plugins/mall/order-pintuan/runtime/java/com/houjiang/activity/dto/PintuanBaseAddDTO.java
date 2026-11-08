package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class PintuanBaseAddDTO extends UserTokenBeanDTO {

	/**
	 * 拼团id
	 */
	private Integer pintuanId;
	/**
	 *店铺ID
	 */
	private String shopId;
	/**
	 *商家ID
	 */
	private String userId;
	/**
	 *拼团活动名称
	 */
	private String name;
	/**
	 *几人成团
	 */
	private Integer personNum;
	/**
	 *开团时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date startTime;
	/**
	 *结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date endTime;
	/**
	 *创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createdTime;
	/**
	 *创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;
	/**
	 *是否可用，1为可用,0不可用
	 */
	private Integer status;
	/**
	 *是否推荐，0不推荐，1推荐
	 */
	private Integer isRecommend;
	/**
	 *商品ID
	 */
	private Integer commonId;
	/**
	 *持续时长
	 */
	private Integer startHour;
	/**
	 *拼团详情
	 */
	List<PintuanDetailSpecAddDTO> goodsSpecList;



}
