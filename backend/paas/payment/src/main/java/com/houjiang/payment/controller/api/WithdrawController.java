package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.withdraw.WithdrawApplyRequest;
import com.houjiang.payment.dto.withdraw.WithdrawApplyResponse;
import com.houjiang.payment.service.api.WithdrawService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 提现模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/withdraw")
@RequiredArgsConstructor
public class WithdrawController {
    
    private final WithdrawService withdrawService;
    
    /**
     * 13. 提现申请
     * 发起提现申请，支持多个渠道
     * 
     * @param request 提现申请请求
     * @return 提现申请响应
     */
    @PostMapping("/apply")
    public WithdrawApplyResponse applyWithdraw(@Valid @RequestBody WithdrawApplyRequest request) {
        log.info("提现申请请求, mchOrderNo:{}, chanNo:{}, amount:{}", 
                request.getMchOrderNo(), request.getChanNo(), request.getAmount());
        return withdrawService.applyWithdraw(request);
    }
    
    /**
     * 14. 提现结果通知
     * 接收第三方支付平台的提现异步通知
     * 
     * 注意事项:
     * 1. 提现成功(2)或失败(3)均会发起通知
     * 2. allinpay渠道不发异步通知,需通过统一订单查询接口查询提现结果
     * 3. 通知失败会重试5次(间隔:30s、90s、180s、300s、450s)
     * 4. 必须返回小写"success"确认收到通知
     * 
     * @param httpRequest HTTP请求对象
     * @return 通知处理结果 success/fail
     */
    @PostMapping("/notify")
    public String withdrawNotify(HttpServletRequest httpRequest) {
        log.info("收到提现结果通知");
        return withdrawService.handleWithdrawNotify(httpRequest);
    }
}
