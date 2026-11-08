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
 * 风控预警实体类
 * 表名: hou_mall_risk_alert
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_risk_alert")
public class RiskAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预警编号
     */
    private String alertNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 关联规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 预警类型 1-库存预警 2-价格异常 3-订单异常 4-交易风险 5-账户安全
     */
    private Integer alertType;

    /**
     * 预警等级 1-低 2-中 3-高 4-紧急
     */
    private Integer alertLevel;

    /**
     * 预警对象ID
     */
    private Long targetId;

    /**
     * 预警对象类型
     */
    private String targetType;

    /**
     * 预警对象名称
     */
    private String targetName;

    /**
     * 预警内容
     */
    private String alertContent;

    /**
     * 当前值
     */
    private BigDecimal currentValue;

    /**
     * 阈值
     */
    private BigDecimal threshold;

    /**
     * 状态 0-待处理 1-处理中 2-已处理 3-已忽略
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人姓名
     */
    private String handlerName;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理结果
     */
    private String handleResult;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 通知状态 0-未通知 1-已通知
     */
    private Integer notifyStatus;

    /**
     * 通知时间
     */
    private LocalDateTime notifyTime;

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
