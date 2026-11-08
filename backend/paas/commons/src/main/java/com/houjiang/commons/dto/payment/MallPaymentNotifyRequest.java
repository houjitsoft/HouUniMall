package com.houjiang.commons.dto.payment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Amos
 */
@Data
public class MallPaymentNotifyRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String cashierNo;
    private String mallOrderNo;
    private String requestNo;
    private String channelOrderNo;
    private BigDecimal amount;
    private String status;
    private LocalDateTime paidTime;
}
