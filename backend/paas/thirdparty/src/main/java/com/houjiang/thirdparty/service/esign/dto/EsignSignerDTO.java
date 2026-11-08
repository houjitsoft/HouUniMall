package com.houjiang.thirdparty.service.esign.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 电子签名签署人DTO
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Data
public class EsignSignerDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 签署人ID（第三方平台返回的签署人ID）
     */
    private String signerId;
    
    /**
     * 签署人姓名
     */
    private String name;
    
    /**
     * 签署人手机号
     */
    private String phone;
    
    /**
     * 签署人邮箱
     */
    private String email;
    
    /**
     * 签署人身份证号（可选）
     */
    private String idCard;
    
    /**
     * 签署顺序（从1开始）
     */
    private Integer signOrder;
    
    /**
     * 签署位置（X坐标）
     */
    private Integer signX;
    
    /**
     * 签署位置（Y坐标）
     */
    private Integer signY;
    
    /**
     * 签署页码
     */
    private Integer pageNum;
}

