package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.withdraw.WithdrawApplyRequest;
import com.houjiang.payment.dto.withdraw.WithdrawApplyResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 提现服务接口
 * 
 * @author Amos
 */
public interface WithdrawService {
    
    /**
     * 13. 提现申请
     * 发起提现申请，支持多个渠道
     * 
     * @param request 提现申请请求
     * @return 提现申请响应
     */
    WithdrawApplyResponse applyWithdraw(WithdrawApplyRequest request);
    
    /**
     * 14. 提现结果通知处理
     * 接收第三方支付平台的提现异步通知
     * 
     * @param httpRequest HTTP请求对象
     * @return 通知处理结果 success/fail
     */
    String handleWithdrawNotify(HttpServletRequest httpRequest);
}
