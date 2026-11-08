package com.houjiang.payment.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * 卡用户详情
 * @author Amos
 */
@Data
public class CardMemberDetailDTO extends UserTokenBeanDTO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 代金券金额
     */
    private String voucherMoney;

    /**
     * 代金券类型：1-单个店铺，2-多店铺参与
     */
    private Double voucherType;

    /**
     * 描述
     */
    private String recordDesc;

}
