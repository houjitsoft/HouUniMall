package com.houjiang.userms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 积分集合响应
 * @author Amos
 */
@Data
public class IntegralValueListVO {

	/**
	 * 积分id
	 */
	private Integer  pointsLogId;

	/**
     * 用户id
     */
    private String userId;

	/**
	 * 用户名称
	 */
	private String userName;

	/**
	 * 积分变更
	 */
	private Integer pointsLogPoints;

	/**
	 * 积分时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date pointsLogTime;

	/**
	 * 操作 会员注册、会员登录、会员购买、换购商品，平台操作
	 */
	private String pointsLogDesc;

	/**
	 * 积分描述
	 */
	private String pointsLogOperate;

	/**
	 * 积分变更2
	 */
	private String pointsLogPoints2;

	/**
	 * 类型 1获取积分2积分消费
	 */
	private Integer pointsLogType;


}
