package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.goods.SeckillGoodsListDTO;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 限时秒杀商品
 *
 * @author Amos
 *
 */
@Data
public class SeckillGoodsUpdateDTO extends UserTokenBeanDTO {

	/**
	 * 限时秒杀商品表
	 */
	private Integer seckillGoodsId;

	/**
	 *限时活动编号
	 */
	private Integer seckillId;

	/**
	 *活动名称
	 */
	private String seckillName;

	/**
	 *活动标题
	 */
	private String seckillTitle;

	/**
	 * 活动说明
	 */
	private String seckillExplain;

	/**
	 *商品图片
	 */
	private String goodsImage;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

	/**
	 *开始时间
	 */
	private Date goodsStartTime;

	/**
	 *结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date goodsEndTime;

	/**
	 *购买下限，0为不限制
	 */
	private Integer goodsLowerLimit;

	/**
	 *状态， 1-正常/2-结束/3-管理员关闭
	 */
	private Integer seckillGoodsState;

	/**
	 *推荐标志 0-未推荐 1-已推荐
	 */
	private Integer seckillGoodsRecommend;

	/**
	 *限购数量
	 */
	private Integer goodsUpperLimit;

	/**
	 *秒杀数量
	 */
	private Integer seckillNum;

	/**
	 *商品编号
	 */
	private Integer goodsId;

	/**
	 * 商品id
	 */
	private Integer commonId;

	/**
	 *店铺编号
	 */
	private String shopId;

	/**
	 *商品名称
	 */
	private String goodsName;

	/**
	 *商品原价
	 */
	private BigDecimal goodsPrice;

	/**
	 *限时秒杀价格
	 */
	private BigDecimal seckillPrice;

	/**
	 *秒杀商品信息
	 */
	private List<SeckillGoodsListDTO> seckillGoodsList;


}
