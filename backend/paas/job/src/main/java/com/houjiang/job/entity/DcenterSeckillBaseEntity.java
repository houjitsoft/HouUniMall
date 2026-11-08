package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据大屏限时秒杀活动表
 * @TableName ${database.prefix.datav}hou_datav_seckill_base
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_seckill_base")
@Data
public class DcenterSeckillBaseEntity implements Serializable {
    /**
     * 限时编号
     */
    @TableId(type = IdType.AUTO)
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}