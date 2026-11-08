package com.houjiang.payment.dto.bill;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账单导入响应DTO
 * 接口：10.账单导入
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BillImportResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 导入批次号（可选）
     */
    private String importBatch;
    
    /**
     * 账单信息（可选）
     * 账单校验异常时返回，为JSONArray字符串
     */
    private String billsInfo;
}
