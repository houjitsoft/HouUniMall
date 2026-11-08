package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 商品评论
 * 
 * @author Amos
 *
 */
@Data
public class GoodsCommentsVO {
	/**
	 * 订单ID
	 */
	private String orderId;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 评分
	 */
	private Integer scores;

	/**
	 * 评论内容
	 */
	private String content;

	/**
	 * 图片链接
	 */
	private String image;

	/**
	 * 创建时间，使用"yyyy-MM-dd HH:mm:ss"格式，"GMT+8"时区，"@"符号表示注解修饰符，指示JSON序列化和反序列化该字段时使用指定的格式和时区。
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 商品名称
	 */
	private String goodsName;

	/**
	 * 商店名称
	 */
	private String shopName;

	/**
	 * 评价商品ID，用于标识被评价的商品。具体含义根据实际业务语境确定。在具体业务语境中定义。
	 */
	private Integer evaluationGoodsId;

}
