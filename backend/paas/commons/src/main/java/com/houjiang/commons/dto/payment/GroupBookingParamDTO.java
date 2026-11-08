package com.houjiang.commons.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName GroupBookingParamDTO
 * @Description 拼团所需参数
 * @Author Amos
 * @Date 2023/7/26 16:00
 * @Version 1.0
 * @author Amos
 */
@Data
public class GroupBookingParamDTO {

    /**
     * 用户id
     */
    private String userId;

    /**
     *订单id
     */
    private String orderId;

    /**
     *用户类型
     */
    private Integer userType;

    /**
     *用户昵称
     */
    private String userNickname;

    /**
     * 金额
     */
    private BigDecimal recordMoney;

    /**
     * 实际支付单号
     */
    private String recordPayorder;
}
