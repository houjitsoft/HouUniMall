package com.houjiang.payment.dto.risk;

import lombok.Data;

/**
 * 风控规则分页查询请求
 *
 * @author Amos
 */
@Data
public class RiskRuleListRequest {

    /** 规则编号（模糊） */
    private String ruleNo;

    /** 规则名称（模糊） */
    private String ruleName;

    /** 规则类型 */
    private Integer ruleType;

    /** 业务类型 */
    private Integer bizType;

    /** 作用域 */
    private Integer scope;

    /** 作用域取值 */
    private String scopeValue;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}
