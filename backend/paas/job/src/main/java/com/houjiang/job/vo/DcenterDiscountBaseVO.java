package com.houjiang.job.vo;

import lombok.Data;

import java.util.Date;

/**
 * 数据大屏限时折扣活动表
 * @TableName ${database.prefix.datav}hou_datav_discount_base
 * @author Amos
 */

@Data
public class DcenterDiscountBaseVO {
    /**
     * 限时编号
     */

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


}