package com.houjiang.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusDTO {

    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 红包编码
     */
    private String redpacketCode;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 代金券编码
     */
    private String voucherCode;

    /**
     * 跨店满减券id(购物津贴)
     */
    private String platformCouponCode;
    /**
     * 1.线上支付  2.线下支付 3.货到付款
     */
    private Integer paymentCode;


}
