package com.houjiang.commons.dto.payment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Amos
 */
@Data
public class MallCashierCreateResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String cashierNo;
    private String mallOrderNo;
    private String requestNo;
    private String payOrderId;
    private String payMethod;
    private String terminal;
    private BigDecimal amount;
    private String status;
    private Map<String, Object> payData;
    private LocalDateTime expireTime;
}
