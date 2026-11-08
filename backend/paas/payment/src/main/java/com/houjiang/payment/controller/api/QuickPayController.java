package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.quickpay.QuickPayAgreeRequest;
import com.houjiang.payment.dto.quickpay.QuickPayAgreeResponse;
import com.houjiang.payment.dto.quickpay.QuickPayUnbindRequest;
import com.houjiang.payment.dto.quickpay.QuickPayUnbindResponse;
import com.houjiang.payment.service.api.QuickPayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 快捷支付模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/quickpay")
@RequiredArgsConstructor
public class QuickPayController {
    
    private final QuickPayService quickPayService;
    
    /**
     * 22. 快捷支付绑卡签约
     * 用于客户发起签约申请和签约确认
     * 
     * 使用场景:
     * 1. 签约申请(agreeType=1): 客户发起签约申请,获取银行签约短信
     * 2. 签约确认(agreeType=2): 签约绑卡短信确认,完成绑卡
     * 
     * 流程说明:
     * 第一步: 调用本接口,agreeType=1,发起签约申请
     *        - 系统会向用户银行预留手机号发送短信验证码
     *        - 返回thpInfo,需要保存用于第二步确认
     * 
     * 第二步: 用户输入短信验证码后,再次调用本接口,agreeType=2,完成签约确认
     *        - 必须携带第一步返回的thpInfo
     *        - 必须携带用户输入的短信验证码verifyCode
     *        - 返回agreeId(协议编号),后续快捷支付交易时需要使用
     * 
     * 注意事项:
     * 1. 目前仅支持收银宝渠道(allinpay)
     * 2. 卡类型: 00-借记卡, 02-准贷记卡/贷记卡
     * 3. 证件类型: 0-身份证, 2-护照, 5-港澳通行证, 6-台湾通行证
     * 4. 证件号末位X必须大写
     * 5. 签约确认时短信验证码(verifyCode)必填
     * 6. 签约确认时必须携带签约申请返回的thpInfo
     * 
     * @param request 快捷支付绑卡签约请求
     * @return 快捷支付绑卡签约响应
     */
    @PostMapping("/agree")
    public QuickPayAgreeResponse quickPayAgree(@Valid @RequestBody QuickPayAgreeRequest request) {
        log.info("快捷支付绑卡签约请求, agreeType:{}, merUserId:{}", 
                request.getAgreeType(), request.getMerUserId());
        return quickPayService.quickPayAgree(request);
    }
    
    /**
     * 25. 快捷解绑
     * 解绑用户绑定的某一个银行卡
     * 
     * 使用场景:
     * 1. 用户不再使用某张银行卡进行快捷支付
     * 2. 用户需要解除银行卡与快捷支付的绑定关系
     * 3. 出于安全考虑,用户主动解绑银行卡
     * 
     * 流程说明:
     * 第一步: 用户之前已经通过快捷支付绑卡签约绑定了银行卡
     *        - 获得了agreeId(协议编号)
     * 
     * 第二步: 用户不再需要这张银行卡,调用本接口解绑
     *        - 必须携带agreeId(签约时返回的协议编号)
     *        - 必须携带chanNo(渠道,目前仅支持allinpay)
     *        - 系统解除银行卡的签约关系
     *        - 返回merUserId(商户用户号)
     * 
     * 第三步: 解绑成功后,该银行卡不能再用于快捷支付
     *        - 如果要再次使用,需要重新签约绑卡
     * 
     * 注意事项:
     * 1. 解绑后银行卡不能再用于快捷支付
     * 2. 如果要再次使用,必须重新签约绑卡
     * 3. 目前仅支持收银宝渠道(allinpay)
     * 4. agreeId为签约时返回的协议编号,必须保存
     * 5. 建议在解绑前确认用户意图,防止误操作
     * 6. 解绑后应通知用户解绑成功
     * 7. 建议记录解绑日志,便于问题排查
     * 
     * @param request 快捷解绑请求
     * @return 快捷解绑响应
     */
    @PostMapping("/unbind")
    public QuickPayUnbindResponse quickPayUnbind(@Valid @RequestBody QuickPayUnbindRequest request) {
        log.info("快捷解绑请求, agreeId:{}, chanNo:{}", 
                request.getAgreeId(), request.getChanNo());
        return quickPayService.quickPayUnbind(request);
    }
}
