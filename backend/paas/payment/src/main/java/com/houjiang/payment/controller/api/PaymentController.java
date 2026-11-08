package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.pay.SmsConfirmRequest;
import com.houjiang.payment.dto.pay.SmsConfirmResponse;
import com.houjiang.payment.dto.pay.SmsResentRequest;
import com.houjiang.payment.dto.pay.SmsResentResponse;
import com.houjiang.payment.service.api.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 支付模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/pay")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * 23. 短信支付确认
     * 用于快捷支付的短信确认
     * 
     * 使用场景:
     * 1. 统一下单时支付方式选快捷支付
     * 2. 下单调用成功后会发送短信验证码
     * 3. 用户输入短信验证码后调用本接口确认支付
     * 
     * 流程说明:
     * 第一步: 调用统一下单接口,wayCode选择快捷支付方式
     *        - 系统会向用户银行预留手机号发送短信验证码
     *        - 可能返回payData,需要保存
     * 
     * 第二步: 用户输入短信验证码后,调用本接口完成支付
     *        - 必须携带agreeId(快捷支付签约时返回的协议编号)
     *        - 必须携带用户输入的短信验证码verifyCode
     *        - 如果下单返回了payData,需要原样上传
     * 
     * 注意事项:
     * 1. 短信有效期为2分钟
     * 2. 超过2分钟未确认,需要调用重新获取支付确认短信接口
     * 3. 建议使用支付订单号(payOrderId),更加准确
     * 4. 商户订单号和支付订单号二选一即可
     * 5. 如果下单返回了payData,必须原样带上
     * 6. 返回state=2表示支付成功
     * 
     * @param request 短信支付确认请求
     * @return 短信支付确认响应
     */
    @PostMapping("/smsconfirm")
    public SmsConfirmResponse smsConfirm(@Valid @RequestBody SmsConfirmRequest request) {
        log.info("短信支付确认请求, mchOrderNo:{}, payOrderId:{}", 
                request.getMchOrderNo(), request.getPayOrderId());
        return paymentService.smsConfirm(request);
    }
    
    /**
     * 24. 重新获取支付确认短信
     * 用于快捷支付短信验证码超时后重新发送
     * 
     * 使用场景:
     * 1. 短信验证码2分钟有效期超时
     * 2. 用户还未输入短信验证码
     * 3. 需要重新发送验证码给用户
     * 
     * 流程说明:
     * 第一步: 用户调用统一下单接口,选择快捷支付
     *        - 系统发送短信验证码到用户银行预留手机
     *        - 可能返回payData,需要保存
     * 
     * 第二步: 2分钟内用户未输入验证码,验证码失效
     *        - 调用本接口重新发送短信验证码
     *        - 必须携带agreeId(签约时返回的协议编号)
     *        - 如果下单返回了payData,需要透传上传
     *        - 系统重新发送短信验证码
     *        - 返回新的payData(如果存在)
     * 
     * 第三步: 用户输入新的验证码,调用短信支付确认接口
     *        - 携带最新的payData(如果存在)
     *        - 输入短信验证码完成支付
     * 
     * 注意事项:
     * 1. 短信验证码有效期2分钟
     * 2. 建议使用支付订单号(payOrderId),更加准确
     * 3. 商户订单号和支付订单号二选一即可
     * 4. 如果下单返回了payData,必须透传上传
     * 5. 返回的payData需要保存,下次确认支付时使用
     * 6. 建议限制短信重发次数(例如3次),防止滥用
     * 7. 超过重发次数限制,建议用户重新下单
     * 
     * @param request 重新获取支付确认短信请求
     * @return 重新获取支付确认短信响应
     */
    @PostMapping("/smsresent")
    public SmsResentResponse smsResent(@Valid @RequestBody SmsResentRequest request) {
        log.info("重新获取支付确认短信请求, mchOrderNo:{}, payOrderId:{}", 
                request.getMchOrderNo(), request.getPayOrderId());
        return paymentService.smsResent(request);
    }
}
