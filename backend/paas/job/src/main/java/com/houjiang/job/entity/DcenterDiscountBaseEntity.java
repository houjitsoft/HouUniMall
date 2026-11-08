package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据大屏限时折扣活动表
 * @TableName ${database.prefix.datav}hou_datav_discount_base
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_discount_base")
@Data
public class DcenterDiscountBaseEntity implements Serializable {
    /**
     * 限时编号
     */
    @TableId(type = IdType.AUTO)
    private Integer discountId;

    /**
     * 活动名称
     */
    private String discountName;

    /**
     * 活动开始时间
     */
    private Date discountStartTime;

    /**
     * 活动结束时间
     */
    private Date discountEndTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}