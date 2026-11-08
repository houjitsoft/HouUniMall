package com.houjiang.payment.service.api;

import com.houjiang.payment.dto.statement.StatementDownloadRequest;
import com.houjiang.payment.dto.statement.StatementDownloadResponse;

/**
 * 对账服务接口
 * 
 * @author Amos
 */
public interface StatementService {
    
    /**
     * 11. 对账文件下载
     * 支持核心企业按天下载对账文件，D+1日11点之后可下载D日的对账文件
     * 
     * @param request 对账文件下载请求
     * @return 对账文件下载响应
     */
    StatementDownloadResponse downloadStatement(StatementDownloadRequest request);
}
