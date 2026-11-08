package com.houjiang.commons.vo.goods;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoneGoodsQueryDTO extends PageHelper {

	/**
	 *
	 */
	private Integer id;

	/**
	 *专区类型：1,传统滋补;2,调味酱料;3,冲饮;4,当季热卖;5,地方特产;6,生鲜;7,粮油;8,米面杂粮;9,茗茶;10,领券;11,名酒;12,南北干货;13,自营优品;14,实惠好货;15,包邮专区;16,特价专区
	 */
	private Integer noneType;

	/**
	 *商品名
	 */
	private String commonName;

	/**
	 *店铺名
	 */
	private String shopName;

	/**
	 *商品审核 1通过，0未通过，10审核中
	 */
	private Integer activityVerify;

	/**
	 *
	 */
	private Integer virtualGoods;

	/**
	 * 轮播地址
	 */
	private String actionSlide;
	/**
	 * 详情跳转类型
	 */
	private Integer actionType;
	/**
	 *跳转详情
	 */
	private DetailModelVO actionDetail;
	/**
	 *跳转详情
	 */
	@JsonIgnore
	private String actionDetailStr;
	/**
	 *排序，数字越大，越靠前
	 */
	private Integer displayOrder;
	/**
	 *更新人
	 */
	@JsonIgnore
	private Integer updateBy;
	/**
	 *
	 */
	@JsonIgnore
	private Date updateDate;


	private Integer shopId;



	/** 1特产推荐;2新品推荐;3为你推荐;4蜂狂特价;5新人专区;6平台活动专区;7:9.9包邮专区;8:品牌生活;9:好货溯源 */
	private Integer opType;
	/** 10:猜你喜欢;11:时令果蔬;12:节日钜惠;13:限时折扣;14:超级拼团; */


	private String tableName;





}
