package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.pay.SmsConfirmRequest;
import com.houjiang.payment.dto.pay.SmsConfirmResponse;
import com.houjiang.payment.dto.pay.SmsResentRequest;
import com.houjiang.payment.dto.pay.SmsResentResponse;

/**
 * 支付服务接口
 * 
 * @author Amos
 */
public interface PaymentService {
    
    /**
     * 23. 短信支付确认
     * 用于快捷支付的短信确认
     * 统一下单时支付方式选快捷支付,调用成功后会发送短信
     * 此时需要用户调用该接口确认支付
     * 
     * @param request 短信支付确认请求
     * @return 短信支付确认响应
     */
    SmsConfirmResponse smsConfirm(SmsConfirmRequest request);
    
    /**
     * 24. 重新获取支付确认短信
     * 用于快捷支付短信验证码超时后重新发送
     * 当短信验证码2分钟有效期过后,用户还未输入验证码,需要调用本接口重新发送
     * 
     * @param request 重新获取支付确认短信请求
     * @return 重新获取支付确认短信响应
     */
    SmsResentResponse smsResent(SmsResentRequest request);
}
