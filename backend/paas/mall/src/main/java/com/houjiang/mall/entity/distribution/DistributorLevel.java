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
 * 分销等级实体类
 * 表名: hou_mall_distributor_level
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_distributor_level")
public class DistributorLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 等级名称
     */
    private String name;

    /**
     * 等级图标
     */
    private String icon;

    /**
     * 等级排序
     */
    private Integer sort;

    /**
     * 一级佣金比例(%)
     */
    private BigDecimal level1Rate;

    /**
     * 二级佣金比例(%)
     */
    private BigDecimal level2Rate;

    /**
     * 升级条件-累计销售额
     */
    private BigDecimal upgradeAmount;

    /**
     * 升级条件-累计订单数
     */
    private Integer upgradeOrderCount;

    /**
     * 升级条件-下级分销员数
     */
    private Integer upgradeSubCount;

    /**
     * 是否默认等级 0-否 1-是
     */
    private Integer isDefault;

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
