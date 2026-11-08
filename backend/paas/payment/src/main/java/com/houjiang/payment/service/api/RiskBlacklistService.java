package com.houjiang.payment.service.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.payment.dto.risk.BlacklistAddRequest;
import com.houjiang.payment.dto.risk.BlacklistListRequest;
import com.houjiang.payment.entity.PayRiskBlacklistEntity;

/**
 * 风控黑白名单服务
 *
 * @author Amos
 */
public interface RiskBlacklistService {

    /** 新增名单（存在则刷新） */
    /**
     * @param request 请求
     * @param operatorId operator主键
     * @return 处理结果
     */
    PayRiskBlacklistEntity add(BlacklistAddRequest request, String operatorId);

    /** 删除（逻辑删除并失效） */
    /**
     * @param id 主键
     * @param operatorId operator主键
     * @return 处理结果
     */
    boolean remove(Long id, String operatorId);

    /** 分页查询 */
    /**
     * @param request 请求
     * @return 处理结果
     */
    IPage<PayRiskBlacklistEntity> list(BlacklistListRequest request);

    /**
     * 快速校验某个目标是否命中名单
     * @return 名单实体或 null
     */
    PayRiskBlacklistEntity matchTarget(Integer listType, Integer targetType, String targetValue, Integer bizScope);
}
