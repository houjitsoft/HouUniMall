package com.houjiang.commons.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallCashierCreateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String mallOrderNo;
    @NotBlank
    private String requestNo;
    @NotBlank
    private String userId;
    private String merchantNo;
    private String shopId;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    private String currency = "CNY";
    @NotBlank
    private String payMethod;
    @NotBlank
    private String terminal;
    private String channelCode;
    private String openId;
    private String clientIp;
    private String notifyUrl;
    private String returnUrl;
    private String subject;
}
