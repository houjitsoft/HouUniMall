package com.houjiang.payment.dto.mch;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商户进件响应DTO
 * 接口：商户进件
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MchRegResponse extends PaymentBaseResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 核心企业商户号(必填)
     */
    private String mchNo;

    /**
     * 核心企业appId(必填)
     */
    private String appId;
}
