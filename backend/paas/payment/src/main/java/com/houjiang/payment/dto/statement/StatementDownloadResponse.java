package com.houjiang.payment.dto.statement;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对账文件下载响应DTO
 * 接口：11.对账文件下载
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StatementDownloadResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 对账文件链接（必填）
     * 下载文件格式为csv或excel
     */
    private String fileUrl;
}
