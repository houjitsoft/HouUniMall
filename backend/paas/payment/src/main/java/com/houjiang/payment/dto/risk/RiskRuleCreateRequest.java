package com.houjiang.payment.dto.risk;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 风控规则创建请求
 *
 * @author Amos
 */
@Data
public class RiskRuleCreateRequest {

    /** 规则编号（唯一，建议英文大写+下划线） */
    @NotBlank(message = "规则编号不能为空")
    private String ruleNo;

    /** 规则名称 */
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    /** 规则类型：1-金额限额 2-频次控制 3-黑白名单 4-自定义 */
    @NotNull(message = "规则类型不能为空")
    private Integer ruleType;

    /** 业务类型：0-全部 1-下单 2-退款 3-提现 4-转账 5-充值 */
    private Integer bizType = 0;

    /** 作用域：1-全局 2-商户 3-用户 4-支付方式 */
    @NotNull(message = "作用域不能为空")
    private Integer scope;

    /** 作用域取值 */
    private String scopeValue;

    /** 触发字段 */
    private String triggerField;

    /** 比较运算符 */
    private String operator;

    /** 阈值 */
    private BigDecimal thresholdValue;

    /** 时间窗口（秒） */
    private Integer timeWindow;

    /** 命中动作：1-拦截 2-告警 3-人工审核 4-仅记录 */
    @NotNull(message = "命中动作不能为空")
    private Integer action;

    /** 优先级 */
    private Integer priority = 100;

    /** 状态：0-禁用 1-启用 */
    private Integer status = 1;

    /** 生效时间 */
    private Date effectiveTime;

    /** 失效时间 */
    private Date expireTime;

    /** 备注 */
    private String remark;
}
