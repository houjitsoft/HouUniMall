package com.houjiang.job.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 满减赠折基本信息表
 * @TableName ${database.prefix.datav}hou_datav_reduction_policy_base
 * @author Amos
 */

@Data
public class DcenterReductionPolicyBaseVO implements Serializable {
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


}