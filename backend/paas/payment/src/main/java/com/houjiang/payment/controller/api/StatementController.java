package com.houjiang.payment.controller.api;

import com.houjiang.payment.dto.statement.StatementDownloadRequest;
import com.houjiang.payment.dto.statement.StatementDownloadResponse;
import com.houjiang.payment.service.api.StatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 对账模块Controller
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/statement")
@RequiredArgsConstructor
public class StatementController {
    
    private final StatementService statementService;
    
    /**
     * 11. 对账文件下载
     * 支持核心企业按天下载对账文件
     * D+1日11点之后可下载D日的对账文件
     * 
     * @param request 对账文件下载请求
     * @return 对账文件下载响应
     */
    @PostMapping("/download")
    public StatementDownloadResponse downloadStatement(@Valid @RequestBody StatementDownloadRequest request) {
        log.info("对账文件下载请求, day:{}, billType:{}", request.getDay(), request.getBillType());
        return statementService.downloadStatement(request);
    }
}
