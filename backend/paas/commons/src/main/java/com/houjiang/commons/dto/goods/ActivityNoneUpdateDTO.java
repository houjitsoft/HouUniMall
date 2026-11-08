package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class ActivityNoneUpdateDTO {

	/**
	 * 更新时间
	 */
	private Date updateDate;

	/**
	 * 商品id
	 */
	private Integer commonId;

	/**
	 * 1特产推荐;2新品推荐;3为你推荐;4蜂狂特价;5新人专区;6平台活动专区;7:9.9包邮专区;8:品牌生活;9:好货溯源
	 * 	10:猜你喜欢;11:时令果蔬;12:节日钜惠;13:限时折扣;14:超级拼团;
	 */
	private Integer opType;

	/**
	 * 审核状态 1通过，0未通过，10审核中
	 */
	private Integer activityVerify=10;

	private String tableName;



}
