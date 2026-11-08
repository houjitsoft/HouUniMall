package com.houjiang.thirdparty.service.esign.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 电子签名操作结果DTO
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Data
public class EsignResultDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 合同ID（第三方平台返回的合同ID）
     */
    private String contractId;
    
    /**
     * 签署链接
     */
    private String signUrl;
    
    /**
     * 合同状态（draft-草稿, pending-待签署, signed-已签署, revoked-已撤销, expired-已过期）
     */
    private String status;
    
    /**
     * 其他数据
     */
    private Map<String, Object> data;
    
    /**
     * 创建成功结果
     */
    public static EsignResultDTO success(String contractId) {
        EsignResultDTO result = new EsignResultDTO();
        result.setSuccess(true);
        result.setContractId(contractId);
        return result;
    }
    
    /**
     * 创建成功结果（带签署链接）
     */
    public static EsignResultDTO success(String contractId, String signUrl) {
        EsignResultDTO result = new EsignResultDTO();
        result.setSuccess(true);
        result.setContractId(contractId);
        result.setSignUrl(signUrl);
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static EsignResultDTO fail(String errorCode, String errorMessage) {
        EsignResultDTO result = new EsignResultDTO();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMessage(errorMessage);
        return result;
    }
}

