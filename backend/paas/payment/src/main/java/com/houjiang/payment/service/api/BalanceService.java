package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.balance.BalanceQueryRequest;
import com.houjiang.payment.dto.balance.BalanceQueryResponse;

/**
 * 余额服务接口
 * 
 * @author Amos
 */
public interface BalanceService {
    
    /**
     * 12. 统一余额查询
     * 查询不同渠道的账户余额
     * 
     * @param request 余额查询请求
     * @return 余额查询响应
     */
    BalanceQueryResponse queryBalance(BalanceQueryRequest request);
}
