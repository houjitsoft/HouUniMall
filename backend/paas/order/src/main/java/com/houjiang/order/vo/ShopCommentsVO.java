package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 店铺评价实体
 * 
 * @author Amos
 *
 */
@Data
public class ShopCommentsVO {

	/** 评价ID，唯一标识一次评价 */
	private Integer evaluationId;
	/** 订单ID，关联到此次评价的订单 */
	private String orderId;
	/** 商家ID，标识评价对象的商家 */
	private Integer shopId;
	/** 用户名，进行评价的用户 */
	private String userName;
	/** 商家名称，被评价的商家 */
	private String shopName;
	/** 评价描述评分，用户对商品描述的满意度评分 */
	private Integer evaluationDesccredit;
	/** 评价服务评分，用户对商家服务的满意度评分 */
	private Integer evaluationServicecredit;
	/** 评价物流评分，用户对物流配送的满意度评分 */
	private Integer evaluationDeliverycredit;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date evaluationCreateTime;

}
