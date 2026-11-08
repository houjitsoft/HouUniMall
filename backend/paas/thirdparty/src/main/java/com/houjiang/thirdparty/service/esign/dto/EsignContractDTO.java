package com.houjiang.thirdparty.service.esign.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 电子签名合同DTO
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Data
public class EsignContractDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 合同标题
     */
    private String title;
    
    /**
     * 合同内容（HTML或PDF Base64）
     */
    private String content;
    
    /**
     * 合同文件URL（如果已有合同文件）
     */
    private String fileUrl;
    
    /**
     * 签署人列表
     */
    private List<EsignSignerDTO> signers;
    
    /**
     * 合同有效期（天数）
     */
    private Integer expireDays;
    
    /**
     * 回调地址
     */
    private String callbackUrl;
    
    /**
     * 其他参数
     */
    private String extraParams;
}

