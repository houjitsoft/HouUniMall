package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.instalment.CalFeeRequest;
import com.houjiang.payment.dto.instalment.CalFeeResponse;
import com.houjiang.payment.service.api.InstalmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 分期模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/instalment")
@RequiredArgsConstructor
public class InstalmentController {
    
    private final InstalmentService instalmentService;
    
    /**
     * 19. 分期利息计算
     * 用于分期交易的每期还款和利息的计算
     * 
     * 使用场景:
     * 1. 用户选择分期支付前,展示各期数的还款方案
     * 2. 展示每期的还款金额和利息明细
     * 3. 帮助用户选择合适的分期方案
     * 
     * 注意事项:
     * 1. 目前仅支持京东白条分期(JDBT_PAY)
     * 2. 金额单位为分
     * 3. 返回多个分期方案(如3期、6期、12期、24期)
     * 
     * @param request 分期利息计算请求
     * @return 分期利息计算响应
     */
    @PostMapping("/calFee")
    public CalFeeResponse calculateFee(@Valid @RequestBody CalFeeRequest request) {
        log.info("分期利息计算请求, amount:{}, wayCode:{}", 
                request.getAmount(), request.getWayCode());
        return instalmentService.calculateFee(request);
    }
}
