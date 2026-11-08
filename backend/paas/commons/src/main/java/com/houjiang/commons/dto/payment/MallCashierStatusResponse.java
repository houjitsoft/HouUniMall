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
public class MallCashierStatusResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String cashierNo;
    private String mallOrderNo;
    private String payOrderId;
    private String payMethod;
    private String terminal;
    private BigDecimal amount;
    private String status;
    private Integer statusCode;
    private String statusDesc;
    private LocalDateTime paidTime;
    private String errCode;
    private String errMsg;
}
