package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 个人会员实名及绑卡响应DTO
 * 接口：9.个人会员实名及绑卡
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegPersonResponse extends PaymentBaseResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 会员编号
     */
    private String bizUserId;
}
