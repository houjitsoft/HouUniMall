package com.houjiang.payment.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.risk.RiskRuleCreateRequest;
import com.houjiang.payment.dto.risk.RiskRuleListRequest;
import com.houjiang.payment.dto.risk.RiskRuleUpdateRequest;
import com.houjiang.payment.entity.PayRiskRuleEntity;

/**
 * 风控规则 CRUD 服务
 *
 * @author Amos
 */
public interface RiskRuleService {

    /** 创建规则 */
    /**
     * @param request 请求
     * @param operatorId operator主键
     * @return 处理结果
     */
    PayRiskRuleEntity create(RiskRuleCreateRequest request, String operatorId);

    /** 更新规则（按 ruleNo） */
    /**
     * @param request 请求
     * @param operatorId operator主键
     * @return 处理结果
     */
    boolean update(RiskRuleUpdateRequest request, String operatorId);

    /** 变更规则状态（0-禁用 1-启用） */
    /**
     * @param ruleNo rule编号
     * @param status 状态
     * @param operatorId operator主键
     * @return 处理结果
     */
    boolean changeStatus(String ruleNo, Integer status, String operatorId);

    /** 删除规则（逻辑删除） */
    /**
     * @param ruleNo rule编号
     * @param operatorId operator主键
     * @return 处理结果
     */
    boolean delete(String ruleNo, String operatorId);

    /** 分页列表 */
    /**
     * @param request 请求
     * @return 处理结果
     */
    IPage<PayRiskRuleEntity> list(RiskRuleListRequest request);

    /** 详情 */
    /**
     * @param ruleNo rule编号
     * @return 处理结果
     */
    PayRiskRuleEntity detail(String ruleNo);
}
