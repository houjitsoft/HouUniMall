package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.bill.BillImportRequest;
import com.houjiang.payment.dto.bill.BillImportResponse;
import com.houjiang.payment.service.api.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 账单模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/bill")
@RequiredArgsConstructor
public class BillController {
    
    private final BillService billService;
    
    /**
     * 10. 账单导入
     * 通过接口导入账单，员工可通过通企付企业服务助手查询账单并分享账单码牌用于收款
     * 
     * @param request 账单导入请求
     * @return 账单导入响应
     */
    @PostMapping("/import")
    public BillImportResponse importBill(@Valid @RequestBody BillImportRequest request) {
        log.info("账单导入请求, termNo:{}", request.getTermNo());
        return billService.importBill(request);
    }
}
