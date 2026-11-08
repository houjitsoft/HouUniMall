package com.houjiang.commons.vo.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class SeckillGoodsQueryResultVO {

	/** 限时活动编号 */
	private Integer seckillId;
	/** 活动名称 */
	private String seckillName;
	/** 活动标题 */
	private String seckillTitle;
	/** 活动说明 */
	private String seckillExplain;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	/** 开始时间 */
	private Date seckillStartTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	/** 结束时间 */
	private Date seckillEndTime;
	/** 购买下限，0为不限制 */
	private Integer seckillLowerLimit;
	/** 状态， 1-正常/2-结束/3-管理员关闭 */
	private Integer seckillGoodsState;
	/** 推荐标志 0-未推荐 1-已推荐 */
	private Integer seckillGoodsRecommend;
	/** 限购数量 */
	private Integer seckillUpperLimit;
	private Integer cancelTime;
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	/**  */
	private Date seckillStartDate;
	/** 状态:0-未开始;1-已开始;2-已结束；3-已失效 */
	private Integer seckillState;
	/** 场次:1-08：00;2-12：00;3-16：00;4-20：00;5-00：00; */
	private Integer seckillHour;


}
