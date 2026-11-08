package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.risk.RiskCheckRequest;
import com.houjiang.payment.dto.risk.RiskCheckResponse;

/**
 * 核心风控引擎服务
 * 业务层（下单/退款/提现/转账/充值）调用 {@link #check(RiskCheckRequest)} 做预校验
 *
 * @author Amos
 */
public interface RiskCheckService {

    /**
     * 风控评估：
     * 1) 黑白名单优先：白名单放行，黑名单直接拒绝
     * 2) 遍历启用规则，按 priority 排序依次评估
     *    - 金额限额 (ruleType=1)：按 trigger_field = amount / sumAmount 判断
     *    - 频次控制 (ruleType=2)：按 timeWindow 内 count 统计
     * 3) 命中任一 action=拦截 则立即中断
     * 4) 汇总命中规则和最终动作，落 risk_log 流水，返回决策
     */
    RiskCheckResponse check(RiskCheckRequest request);
}
