package com.houjiang.payment.dto.bill;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单信息DTO
 * 用于账单导入和响应
 * 
 * @author Amos
 */
@Data
public class BillInfoDTO {
    
    /**
     * 账单金额（必填）
     * 单位：元（注意：此接口使用元，而非分）
     */
    private BigDecimal amount;
    
    /**
     * 账单生效时间（必填）
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String billEffTime;
    
    /**
     * 账单失效时间（必填）
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String billExpTime;
    
    /**
     * 业务员手机号（可选）
     */
    private String payeePhone;
    
    /**
     * 模板自定义字段1（可选）
     */
    private String field1;
    
    /**
     * 模板自定义字段2（可选）
     */
    private String field2;
    
    /**
     * 模板自定义字段3（可选）
     */
    private String field3;
    
    /**
     * 模板自定义字段4（可选）
     */
    private String field4;
    
    /**
     * 模板自定义字段5（可选）
     */
    private String field5;
    
    /**
     * 账单校验异常原因（响应时返回）
     */
    private String failMsg;
}
