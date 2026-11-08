package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户评论
 *
 * @author Amos
 *
 */
@Data
public class CommentsEntityVO {

	/**
	 * 订单ID
	 */
	private String orderId;

	/**
	 * 商品ID
	 */
	private Integer goodsId;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 用户头像
	 */
	private String userAvatar;

	/**
	 * 商品名称
	 */
	private String goodsName;

	/**
	 * 评分
	 */
	private Integer scores;

	/**
	 * 评论内容
	 */
	private String content;

	/**
	 * 其他附加内容
	 */
	private String additionalContent;

	/**
	 * 其他附加内容的时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date additionalContentTime;

	/**
	 * 图片链接
	 */
	private String image;

	/**
	 * 其他附加图片链接
	 */
	private String additionalImage;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	/**
	 * 解释内容
	 */
	private String explainContent;

	/**
	 * 其他附加解释内容
	 */
	private String additionalExplainContent;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;

	/**
	 * 其他附加回复时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date additionalReplyTime;

	/**
	 * 图片列表
	 */
	private String[] imagesList;

	/**
	 * 其他附加图片列表
	 */
	private String[] additionalImagesList;

	/**
	 * 商品规格描述
	 */
	private String goodsSpec;


	/**
	 * 附加天数
	 */
	private Integer additionalDay;
	/**
	 * 是否匿名评价 0 不匿名 1  匿名
	 */
	private Integer isanonymous;
	/**
	 * 评论等级 good neutral  bad
	 */
	private String result;
	/**
	 * 商品图片
	 */
	private String goodsImage;
	/**
	 * 评价状态
	 */
	private Integer evaluateStatus;

}
