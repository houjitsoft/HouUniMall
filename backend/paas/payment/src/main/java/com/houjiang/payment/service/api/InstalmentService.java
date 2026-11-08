package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.instalment.CalFeeRequest;
import com.houjiang.payment.dto.instalment.CalFeeResponse;

/**
 * 分期服务接口
 * 
 * @author Amos
 */
public interface InstalmentService {
    
    /**
     * 19. 分期利息计算
     * 用于分期交易的每期还款和利息的计算
     * 目前仅支持京东白条分期
     * 
     * @param request 分期利息计算请求
     * @return 分期利息计算响应
     */
    CalFeeResponse calculateFee(CalFeeRequest request);
}
