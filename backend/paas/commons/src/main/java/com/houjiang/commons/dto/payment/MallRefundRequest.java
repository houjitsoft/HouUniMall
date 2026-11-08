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
public class MallRefundRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank
    private String refundNo;
    @NotBlank
    private String cashierNo;
    @NotBlank
    private String mallOrderNo;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    private String reason;
    private String notifyUrl;
}
