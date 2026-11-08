package com.houjiang.payment.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName BalanceWithdrawDTO
 * @Author Amos
 * @Date 2024/9/23 13:38
 * @Version 1.0
 * @author Amos
 */
@Data
public class BalanceWithdrawDTO extends UserTokenBeanDTO {
    /**
     * 微信用户的 openId
     */
    private String openId;
    /**
     * 提现金额
     */
    private BigDecimal money;
    /**
     * 支付宝登录号
     */
    private String userMobile;
    /**
     * 支付宝真实姓名
     */
    private String name;
    /**
     * 支付密码
     */
    private String passwd;
}
