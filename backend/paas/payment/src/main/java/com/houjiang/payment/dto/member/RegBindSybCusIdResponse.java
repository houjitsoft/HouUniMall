package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员绑定收银宝商户号开户响应DTO
 * 接口：15.会员绑定收银宝商户号开户
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegBindSybCusIdResponse extends PaymentBaseResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 会员编号(必填)
     */
    private String bizUserId;
}
