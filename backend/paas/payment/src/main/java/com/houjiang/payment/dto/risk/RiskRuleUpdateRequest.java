package com.houjiang.payment.dto.risk;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 风控规则更新请求（按 ruleNo 定位）
 *
 * @author Amos
 */
@Data
public class RiskRuleUpdateRequest {

    /** 规则编号（必填） */
    @NotBlank(message = "规则编号不能为空")
    private String ruleNo;

    private String ruleName;

    private Integer ruleType;

    private Integer bizType;

    private Integer scope;

    private String scopeValue;

    private String triggerField;

    private String operator;

    private BigDecimal thresholdValue;

    private Integer timeWindow;

    private Integer action;

    private Integer priority;

    /** 0-禁用 1-启用（支持通过 update 变更状态，也可调 /changeStatus） */
    private Integer status;

    private Date effectiveTime;

    private Date expireTime;

    private String remark;
}
