package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.balance.BalanceQueryRequest;
import com.houjiang.payment.dto.balance.BalanceQueryResponse;
import com.houjiang.payment.service.api.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 余额模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/balance")
@RequiredArgsConstructor
public class BalanceController {
    
    private final BalanceService balanceService;
    
    /**
     * 12. 统一余额查询
     * 查询不同渠道的账户余额
     * 
     * @param request 余额查询请求
     * @return 余额查询响应
     */
    @PostMapping("/query")
    public BalanceQueryResponse queryBalance(@Valid @RequestBody BalanceQueryRequest request) {
        log.info("余额查询请求, chanNo:{}", request.getChanNo());
        return balanceService.queryBalance(request);
    }
}
