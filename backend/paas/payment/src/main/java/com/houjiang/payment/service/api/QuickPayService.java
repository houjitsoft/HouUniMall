package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.quickpay.QuickPayAgreeRequest;
import com.houjiang.payment.dto.quickpay.QuickPayAgreeResponse;
import com.houjiang.payment.dto.quickpay.QuickPayUnbindRequest;
import com.houjiang.payment.dto.quickpay.QuickPayUnbindResponse;

/**
 * 快捷支付服务接口
 * 
 * @author Amos
 */
public interface QuickPayService {
    
    /**
     * 22. 快捷支付绑卡签约
     * 用于客户发起签约申请和签约确认
     * 
     * 使用场景:
     * 1. 签约申请(agreeType=1): 发起签约申请,获取银行签约短信
     * 2. 签约确认(agreeType=2): 短信确认,完成绑卡签约
     * 
     * @param request 快捷支付绑卡签约请求
     * @return 快捷支付绑卡签约响应
     */
    QuickPayAgreeResponse quickPayAgree(QuickPayAgreeRequest request);
    
    /**
     * 25. 快捷解绑
     * 解绑用户绑定的某一个银行卡
     * 
     * @param request 快捷解绑请求
     * @return 快捷解绑响应
     */
    QuickPayUnbindResponse quickPayUnbind(QuickPayUnbindRequest request);
}
