package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员资料补录响应DTO
 * 接口：4.会员资料补录
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegSupplementResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商户会员编号
     */
    private String bizUserId;
}
