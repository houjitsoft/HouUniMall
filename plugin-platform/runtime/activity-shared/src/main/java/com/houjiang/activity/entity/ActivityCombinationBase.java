package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 组合活动基础表实体类
 *
 * @author Amos
 * @date 2026-01-04
 */
@Data
@TableName("hou_addon_activity_combination_base")
public class ActivityCombinationBase implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组合活动id
     */
    @TableId(value = "combination_id", type = IdType.AUTO)
    private Long combinationId;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 组合活动名称
     */
    private String combinationName;

    /**
     * 组合活动类型 1固定组合 2搭配组合
     */
    private Integer combinationType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 组合活动状态 0-未开始;1-已开始;2-已结束；3-已失效
     */
    private Integer combinationStatus;

    /**
     * 组合活动价格
     */
    private BigDecimal combinationPrice;

    /**
     * 组合活动主图
     */
    private String combinationImage;

    /**
     * 运费模板id
     */
    private Long transportTemplateId;

    /**
     * 售卖区域id
     */
    private Long transportAreaId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 组合活动原价
     */
    private BigDecimal combinationOriginalPrice;
}
