package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 个人会员信息DTO
 * @author Amos
 */
@Data
public class PersonInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 姓名(必填)
     */
    private String idenName;

    /**
     * 证件号码(必填)
     * aes加密
     */
    private String idenNo;

    /**
     * 证件类型(必填)
     * 1-身份证
     */
    private String identityType;
}
