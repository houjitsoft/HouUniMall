package com.houjiang.payment.dto.member;

import lombok.Data;

import java.io.Serializable;

/**
 * 抽佣协议信息DTO
 * @author Amos
 */
@Data
public class CouponInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 抽佣方式(必填)
     * 1：分账比例
     */
    private String couponType;

    /**
     * 具体抽佣比例(必填)
     * 最多4位小数，如分账比例为5.55%，填写0.0555
     */
    private String couponRate;
}
