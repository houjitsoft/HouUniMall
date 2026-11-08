package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员绑定手机号确认响应DTO
 * 接口：7.会员绑定手机号确认
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BindPhoneConfirmResponse extends PaymentBaseResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 商户会员编号
     */
    private String bizUserId;

    /**
     * 手机号
     */
    private String phone;
}
