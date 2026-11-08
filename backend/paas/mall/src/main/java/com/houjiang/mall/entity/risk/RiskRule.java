package com.houjiang.mall.entity.risk;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风控规则实体类
 * 表名: hou_mall_risk_rule
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_risk_rule")
public class RiskRule implements Serializable {

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
     * 规则类型 1-库存预警 2-价格异常 3-订单异常 4-交易风险 5-账户安全
     */
    private Integer ruleType;

    /**
     * 监控指标 如：stock_quantity, price_change_rate, order_frequency
     */
    private String monitorIndex;

    /**
     * 比较方式 1-大于 2-小于 3-等于 4-大于等于 5-小于等于 6-区间
     */
    private Integer compareType;

    /**
     * 阈值
     */
    private BigDecimal threshold;

    /**
     * 阈值上限（区间比较时使用）
     */
    private BigDecimal thresholdMax;

    /**
     * 预警等级 1-低 2-中 3-高 4-紧急
     */
    private Integer alertLevel;

    /**
     * 通知方式（JSON数组）1-站内信 2-短信 3-邮件 4-钉钉
     */
    private String notifyMethods;

    /**
     * 通知接收人（JSON数组）
     */
    private String notifyReceivers;

    /**
     * 检查频率（分钟）
     */
    private Integer checkInterval;

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
