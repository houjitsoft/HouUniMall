package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 限时折扣活动表实体类
 *
 * @author Amos
 * @date 2026-01-04
 */
@Data
@TableName("hou_addon_activity_discount_base")
public class ActivityDiscountBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 限时编号
     */
    @TableId(value = "discount_id", type = IdType.AUTO)
    private Long discountId;

    /**
     * 活动名称
     */
    private String discountName;

    /**
     * 活动开始时间
     */
    private LocalDateTime discountStartTime;

    /**
     * 活动结束时间
     */
    private LocalDateTime discountEndTime;

    /**
     * 用户编号
     */
    private String userId;

    /**
     * 店铺编号
     */
    private Long shopId;

    /**
     * 用户名
     */
    private String userNickName;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 购买下限，1为不限制
     */
    private Integer discountLowerLimit;

    /**
     * 状态， 1-已开始；2-已结束；3-已失效；0-未开始；4-预热中
     */
    private Integer discountState;

    /**
     * 每种商品限购 限购次数
     */
    private Integer discountUpperLimit;

    /**
     * 活动创建时间
     */
    private LocalDateTime discountCreateTime;

    /**
     * 预热时间（小时）
     */
    private Integer preStartHour;

    /**
     * 每种商品限购 限购天数
     */
    private Integer discountUpperTime;

    /**
     * 预热开始时间
     */
    private LocalDateTime preStartTime;
}
