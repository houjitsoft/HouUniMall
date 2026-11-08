package com.houjiang.mall.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 店铺等级实体类
 * 表名: hou_mall_shop_level
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_shop_level")
public class ShopLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 等级值
     */
    private Integer level;

    /**
     * 最低销售额要求
     */
    private BigDecimal minSales;

    /**
     * 佣金比例%
     */
    private BigDecimal commissionRate;

    /**
     * 特殊权益JSON
     */
    private String specialRights;

    /**
     * 等级图标
     */
    private String icon;

    /**
     * 等级说明
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态: 0-禁用 1-启用
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
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}
