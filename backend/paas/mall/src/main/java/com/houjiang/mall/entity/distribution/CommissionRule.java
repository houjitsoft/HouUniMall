package com.houjiang.mall.entity.distribution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金规则实体类
 * 表名: hou_mall_commission_rule
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_commission_rule")
public class CommissionRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则类型 1-全店通用 2-指定分类 3-指定商品
     */
    private Integer type;

    /**
     * 关联ID(分类ID或商品ID)
     */
    private Long relatedId;

    /**
     * 一级佣金比例(%)
     */
    private BigDecimal level1Rate;

    /**
     * 二级佣金比例(%)
     */
    private BigDecimal level2Rate;

    /**
     * 固定佣金金额(一级)
     */
    private BigDecimal level1Amount;

    /**
     * 固定佣金金额(二级)
     */
    private BigDecimal level2Amount;

    /**
     * 计算方式 1-按比例 2-固定金额
     */
    private Integer calcType;

    /**
     * 优先级(数字越大优先级越高)
     */
    private Integer priority;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
