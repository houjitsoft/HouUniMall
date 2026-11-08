package com.houjiang.job.vo;

import lombok.Data;

import java.util.Date;

/**
 * 数据大屏限时秒杀活动表
 * @TableName ${database.prefix.datav}hou_datav_seckill_base
 * @author Amos
 */

@Data
public class DcenterSeckillBaseVO {
    /**
     * 限时编号
     */

    private Integer seckillId;

    /**
     * 活动名称
     */
    private String seckillName;

    /**
     * 活动开始时间
     */
    private Date seckillStartTime;

    /**
     * 活动结束时间
     */
    private Date seckillEndTime;


}