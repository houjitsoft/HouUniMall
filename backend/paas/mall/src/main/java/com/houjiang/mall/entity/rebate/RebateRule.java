package com.houjiang.mall.entity.rebate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 返利规则实体类
 * 表名: hou_mall_rebate_rule
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_rebate_rule")
public class RebateRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 返利类型 1-会员返利 2-消费返利 3-推广返利 4-积分返利
     */
    private Integer rebateType;

    /**
     * 计算方式 1-固定金额 2-百分比
     */
    private Integer calcType;

    /**
     * 返利值（固定金额或百分比）
     */
    private BigDecimal rebateValue;

    /**
     * 最低消费金额
     */
    private BigDecimal minAmount;

    /**
     * 最高返利金额
     */
    private BigDecimal maxRebate;

    /**
     * 适用商品类型 0-全部商品 1-指定分类 2-指定商品
     */
    private Integer goodsType;

    /**
     * 适用商品/分类ID（JSON数组）
     */
    private String goodsIds;

    /**
     * 适用会员等级（JSON数组）
     */
    private String memberLevels;

    /**
     * 生效开始时间
     */
    private LocalDateTime startTime;

    /**
     * 生效结束时间
     */
    private LocalDateTime endTime;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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
