package com.houjiang.payment.dto.member;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员注册邀请码生成响应DTO
 * 接口：13.会员注册邀请码生成
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GenerateRegInviteLinkResponse extends PaymentBaseResponse {

    private static final long serialVersionUID = 1L;

    /**
     * 注册邀请码链接(必填)
     */
    private String regInviteLink;

    /**
     * 注册邀请码链接到期时间(必填)
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    private String regInviteLinkEndTime;
}
