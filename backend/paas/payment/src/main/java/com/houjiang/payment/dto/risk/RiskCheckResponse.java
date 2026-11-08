package com.houjiang.payment.dto.risk;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 风控检查响应
 * pass=true 允许继续业务；pass=false 必须中断
 *
 * @author Amos
 */
@Data
public class RiskCheckResponse implements Serializable {

    /** 是否通过（true-放行 false-拦截/待审核） */
    private boolean pass;

    /** 本次最终动作：1-放行 2-拦截 3-告警 4-待审核 */
    private Integer action;

    /** 风险等级：0-正常 1-低 2-中 3-高 */
    private Integer riskLevel;

    /** 拦截/告警原因（用于前端展示） */
    private String rejectReason;

    /** 命中规则 */
    private List<HitRuleItem> hitRules = new ArrayList<>();

    /** 日志单号（用于前端查询审核） */
    private String logNo;

    /** 风控命中规则明细 */
    @Data
    public static class HitRuleItem implements Serializable {
        private String ruleNo;
        private String ruleName;
        private Integer ruleType;
        private Integer action;
        private String triggerField;
        private String operator;
        private String thresholdValue;
        private String actualValue;
        private String reason;
    }

    public static RiskCheckResponse pass(String logNo) {
        RiskCheckResponse r = new RiskCheckResponse();
        r.pass = true;
        r.action = 1;
        r.riskLevel = 0;
        r.logNo = logNo;
        return r;
    }

    public static RiskCheckResponse reject(String logNo, String reason) {
        RiskCheckResponse r = new RiskCheckResponse();
        r.pass = false;
        r.action = 2;
        r.riskLevel = 3;
        r.rejectReason = reason;
        r.logNo = logNo;
        return r;
    }
}
