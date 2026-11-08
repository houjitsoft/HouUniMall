package com.houjiang.activity.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class PintuanMarkMessageVO {

	/**
	 *团长标识
	 */
	private String markId;

	/**
	 * 拼团详情
	 */
	private Integer detailId; 
	/**
	 *
	 */
	private Integer pintuanId;
	/**
	 *创建时间
	 */
	private Date createdTime; 
	/**
	 *当前参团人数
	 */
	private Integer num; 
	/**
	 *0,等待成团。1为已成团，2为成团失败，3为已完成退款
	 */
	private Integer status; 
	/**
	 *还差几人成团
	 */
	private Integer surplusNum; 
	/**
	 *几人成团
	 */
	private Integer personNum; 
	/**
	 *团长ID
	 */
	private String userId; 
	/**
	 *用户头像
	 */
	private String userAvatar; 
	/**
	 *用户名
	 */
	private String userName; 
	/**
	 *订单号
	 */
	private String orderId; 
	/**
	 *'在团购，人中排第几。0为单独团
	 */
	private Integer ranking; 
	/**
	 *0拼团，1单独购买
	 */
	private Integer type; 
	/**
	 *
	 */
	private Integer goodsId;
	/**
	 *支付状态 0 未付款 1 已付款 2 以退款
	 */
	private Integer payTtart; 

}
