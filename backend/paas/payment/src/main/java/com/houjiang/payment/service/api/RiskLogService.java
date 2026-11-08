package com.houjiang.payment.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.risk.RiskLogAuditRequest;
import com.houjiang.payment.dto.risk.RiskLogListRequest;
import com.houjiang.payment.entity.PayRiskLogEntity;

import java.util.Map;

/**
 * 风控日志查询与审核服务
 *
 * @author Amos
 */
public interface RiskLogService {

    /** 分页查询风控日志 */
    /**
     * @param request 请求
     * @return 处理结果
     */
    IPage<PayRiskLogEntity> list(RiskLogListRequest request);

    /** 详情（按 logNo） */
    /**
     * @param logNo 日志编号
     * @return 处理结果
     */
    PayRiskLogEntity detail(String logNo);

    /** 人工审核（通过/驳回） */
    /**
     * @param request 请求
     * @param operatorId operator主键
     * @return 处理结果
     */
    boolean audit(RiskLogAuditRequest request, String operatorId);

    /** W4 P0 提交人工审核（reviewStatus 置 0 待审） */
    /**
     * @param request 请求
     * @param operatorId operator主键
     * @return 处理结果
     */
    boolean submitReview(Map<String, Object> request, String operatorId);

    /** W4 P0 人工审核通过/驳回（reviewStatus 置 1/2） */
    /**
     * @param request 请求
     * @param operatorId operator主键
     * @return 处理结果
     */
    boolean approve(Map<String, Object> request, String operatorId);
}
