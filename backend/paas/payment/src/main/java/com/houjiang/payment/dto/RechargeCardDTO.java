package com.houjiang.payment.dto;


import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * 检查卡
 * @author Amos
 */
@Data
public class RechargeCardDTO extends UserTokenBeanDTO {

    /**
     * 面额
     */
    private String totalPrice;

    /**
     * 开卡
     */
    private String startCard;

    /**
     * 关卡
     */
    private String finishCard;

    /**
     * 结束时间
     */
    private String finishTime;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 卡描述
     */
    private String cardDesc;

    /**
     * 时间类型
     */
    private Integer timeType;
}
