package com.houjiang.payment.dto.common;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件上传响应DTO
 * 接口：文件上传
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UploadFileResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 文件标识
     * 长期有效
     */
    private String fileId;
}
