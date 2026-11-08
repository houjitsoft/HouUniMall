package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 满减赠折基本信息表
 * @TableName ${database.prefix.datav}hou_datav_reduction_policy_base
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_reduction_policy_base")
@Data
public class DcenterReductionPolicyBaseEntity implements Serializable {
    /**
     * 活动ID
     */
    @TableId(type = IdType.AUTO)
    private Integer promotionId;

    /**
     * 活动名称
     */
    private String promotionName;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}