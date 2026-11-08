package com.houjiang.commons.dto.order;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class CancelParaEntityDTO {
    private String paymentNumber;
    private String redpacketCode;
    private String voucherCode;
    private String platformCouponCode;
    private String orderBuyId;


}
