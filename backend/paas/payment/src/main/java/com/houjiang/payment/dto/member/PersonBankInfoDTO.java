package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 个人会员银行卡信息DTO
 * @author Amos
 */
@Data
public class PersonBankInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 银行卡号(必填)
     * aes加密
     */
    private String acctNo;

    /**
     * 银行卡绑定手机号(必填)
     */
    private String bankPhoneNo;

    /**
     * 有效期(可选)
     * 格式为月年；如0321，2位月2位年
     * 针对信用卡，若商户送，则透传渠道
     */
    private String validDate;

    /**
     * CVV2(可选)
     * 3位数字
     * 针对信用卡，若商户送，则透传渠道
     */
    private String cvv2;
}
