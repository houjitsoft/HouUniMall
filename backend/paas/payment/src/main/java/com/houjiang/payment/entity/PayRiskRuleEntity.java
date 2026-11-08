package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付风控规则表实体（hou_pay_risk_rule）
 * 对标通企付风控配置能力，支持金额限额 / 频次控制 / 黑白名单 / 自定义 4 大类规则
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_risk_rule")
public class PayRiskRuleEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则编号（唯一业务标识） */
    private String ruleNo;

    /** 规则名称 */
    private String ruleName;

    /** 规则类型：1-金额限额 2-频次控制 3-黑白名单 4-自定义 */
    private Integer ruleType;

    /** 业务类型：0-全部 1-收银台下单 2-退款 3-提现 4-转账 5-充值 */
    private Integer bizType;

    /** 作用域：1-全局 2-商户 3-用户 4-支付方式 */
    private Integer scope;

    /** 作用域取值 */
    private String scopeValue;

    /** 触发字段：amount/count/sumAmount/ip/userId */
    private String triggerField;

    /** 比较运算符：>= <= = != IN NOT_IN */
    private String operator;

    /** 阈值 */
    private BigDecimal thresholdValue;

    /** 时间窗口（秒） */
    private Integer timeWindow;

    /** 命中动作：1-拦截 2-告警 3-人工审核 4-仅记录 */
    private Integer action;

    /** 优先级（数字越小优先级越高） */
    private Integer priority;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    /** 生效时间 */
    private Date effectiveTime;

    /** 失效时间 */
    private Date expireTime;

    /** 备注 */
    private String remark;

    /** 创建人ID */
    private String creatorId;

    /** 更新人ID */
    private String updaterId;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
