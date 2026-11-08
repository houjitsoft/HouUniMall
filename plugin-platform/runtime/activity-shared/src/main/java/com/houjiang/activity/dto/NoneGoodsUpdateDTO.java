package com.houjiang.activity.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class NoneGoodsUpdateDTO {

	/**
	 * 商品id集合
	 */
	private List<Integer> commonIdList;

	/**
	 * 1新增,2编辑
	 */
	private Integer opType;
	/**
	 *排序，数字越大，越靠前
	 */
	private Integer displayOrder;
	/**
	 *更新人
	 */
	private String updateBy;
	/**
	 *更新时间
	 */
	private Date updateDate;
	/**
	 *商品类型
	 */
	private Integer noneType;
	/**
	 *  1特产推荐;2新品推荐;3为你推荐;4蜂狂特价;5新人专区;6平台活动专区;7:9.9包邮专区;8:品牌生活;9:好货溯源;10:猜你喜欢;11:时令果蔬;
	 * 	 12:节日钜惠;13:限时折扣;14:超级拼团;
	 *
	 * 	更新后的专区类型
	 */
	private List<Integer> noneGoodsIdList;
	/**
	 *商品审核 1通过，0未通过，10审核中
	 */
	private Integer activityVerify = 1; 
	/**
	 *审核备注
	 */
	private String activityVerifyRemark; 
	/**
	 *活动角标
	 */
	private Integer cornerId; 
	/**
	 *商品ID
	 */
	private Integer commonId;

	/**
	 *表名
	 */
	private String tableName;


}
