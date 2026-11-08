package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.transfer.TransferOrderRequest;
import com.houjiang.payment.dto.transfer.TransferOrderResponse;
import com.houjiang.payment.service.api.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 转账模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/transfer")
@RequiredArgsConstructor
public class TransferController {
    
    private final TransferService transferService;
    
    /**
     * 20. 转账接口
     * 用于商户之间余额转账
     * 
     * 使用场景:
     * 1. 商户之间资金划转
     * 2. 收银宝余额转账
     * 3. 平台商户资金管理
     * 
     * 注意事项:
     * 1. 同步返回转账结果
     * 2. 若未返回终态(状态2或3),请通过统一订单查询接口查询转账结果
     * 3. 目前仅支持allinpay接口
     * 4. 仅支持收银宝余额(SYB_CASH)入账方式
     * 5. 转账账户和收款账户不能相同
     * 6. 金额单位为分
     * 
     * @param request 转账请求
     * @return 转账响应
     */
    @PostMapping("/order")
    public TransferOrderResponse transferOrder(@Valid @RequestBody TransferOrderRequest request) {
        log.info("转账请求, mchOrderNo:{}, amount:{}, sendAccountNo:{}, accountNo:{}", 
                request.getMchOrderNo(), request.getAmount(), 
                request.getSendAccountNo(), request.getAccountNo());
        return transferService.transferOrder(request);
    }
}
