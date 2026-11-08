package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 银行卡信息DTO
 * 
 * @author Amos
 */
@Data
public class BankInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 账户类型(必填)
     * 0-对私
     * 1-对公
     */
    private String acctType;
    
    /**
     * 银行卡号(必填)
     * aes加密
     */
    private String acctNo;
    
    /**
     * 银行卡绑定手机号(可选)
     * 账户类型=0-对私时,则必填
     */
    private String bankPhoneNo;
    
    /**
     * 开户行名称(可选)
     */
    private String parentBankName;
    
    /**
     * 银行代码(可选)
     * 账户类型=1-对公时,则必填
     * 参见附录3. 银行代码及支付行号
     */
    private String openBankNo;
    
    /**
     * 开户行支行名称(可选)
     * 账户类型=1-对公时,则必填
     * 参见附录3
     */
    private String bankName;
    
    /**
     * 支付行号(可选)
     * 账户类型=1-对公时,则必填
     * 参见附录3
     */
    private String unionBank;
    
    /**
     * 开户行所在省(必填)
     * 例 江苏省,上送 江苏
     */
    private String openBankProvince;
    
    /**
     * 开户行所在市(必填)
     * 例 南京市,上送 南京
     */
    private String openBankCity;
}
