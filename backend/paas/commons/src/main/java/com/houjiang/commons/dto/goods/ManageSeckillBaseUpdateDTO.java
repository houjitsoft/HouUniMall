package com.houjiang.commons.dto.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * 限时秒杀活动
 * 
 * @author Amos
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ManageSeckillBaseUpdateDTO extends PageHelper {

	/** 限时编号 */
	private Integer seckillId;
	/** 活动名称 */
	private String seckillName;
	/** 活动标题 */
	private String seckillTitle;
	/** 活动说明 */
	private String seckillExplain;
	/** 套餐编号 */
	private Integer comboId;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	/** 活动开始时间 */
	private Date seckillStartTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	/** 活动结束时间 */
	private Date seckillEndTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	/** 活动创建时间 */
	private Date seckillCreateTime;
	/** 用户编号 */
	private String userId;
	/** 店铺编号 */
	private Integer shopId;
	/** 用户名 */
	private String userNickName;
	/** 店铺名称 */
	private String shopName;
	/** 购买下限，1为不限制 */
	private Integer seckillLowerLimit;
	/** 状态， 1-正常/2-结束/3-管理员关闭 */
	private Integer seckillState;
	/** 场次 */
	private Integer seckillHour;
	/** 最高购买数量 */
	private Integer seckillUpperLimit;
	/** 参与次数限制 */
	private Integer seckillJoinLimit;
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date seckillStartDate;
	/** 订单取消时间 */
	private Integer cancelTime;

	private List<SeckillGoodsListDTO> seckillGoodsList;










}
