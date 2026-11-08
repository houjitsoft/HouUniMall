package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 企业会员实名开户响应DTO
 * 接口：2.企业会员实名开户
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegEnterpriseResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 会员编号
     */
    private String bizUserId;
}
