package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员绑定手机号申请响应DTO
 * 接口：6.会员绑定手机号申请
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BindPhoneAppleResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商户会员编号
     */
    private String bizUserId;
    
    /**
     * 响应流水号
     */
    private String applyRespTraceNum;
    
    /**
     * 授权手机号签约链接(可选)
     * 只有绑定类型为2且授权类型是1返回
     */
    private String authSignLink;
}
