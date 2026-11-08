package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 被授权人信息DTO
 * 
 * @author Amos
 */
@Data
public class AuthPerInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 授权类型(必填)
     * 1-线上
     * 2-线下
     */
    private String authPerType;
    
    /**
     * 授权人名称(必填)
     */
    private String authPerName;
    
    /**
     * 授权人证件号(必填)
     * aes加密
     */
    private String authPerIdNo;
    
    /**
     * 授权人证件类型(必填)
     * 1-身份证
     */
    private String authPerIdType;
    
    /**
     * 授权协议文件fileId(可选)
     * 仅授权类型为2时传
     */
    private String authProcotolFileId;
}
