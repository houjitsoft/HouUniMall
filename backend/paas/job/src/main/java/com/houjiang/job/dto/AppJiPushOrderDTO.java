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
public class AppJiPushOrderDTO {

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 快递
     */
    private String expressPinyin;


}
