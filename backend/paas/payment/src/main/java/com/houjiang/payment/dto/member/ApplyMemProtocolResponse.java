package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员协议签约申请响应DTO
 * 接口：11.会员协议签约申请
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplyMemProtocolResponse extends PaymentBaseResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 收款方签约链接(可选)
     */
    private String recSignLink;

    /**
     * 分账方签约链接(可选)
     * 分账协议返回
     */
    private String divSignLink;

    /**
     * 签约链接失效时间(可选)
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String protocolLinkEndTime;
}
