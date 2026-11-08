package com.houjiang.order.dto;

import lombok.Data;

import java.util.List;

/**
 * 添加支付凭证接受对象
 * @author Amos
 */
@Data
public class PaymentVocherDTO {

    /**
     * 用户token
     */
    private String token;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 图片数组
     */
    private List<String> orderPaymentVoucherImgs;

    /**
     * 图片json字符串
     */
    private String jsonImgs;

    /**
     * 状态
     */
    private Integer status;


}
