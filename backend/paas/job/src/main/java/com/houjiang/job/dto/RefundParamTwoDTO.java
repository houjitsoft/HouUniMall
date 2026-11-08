package com.houjiang.job.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName RefundParamTwoDTO
 * @Description 退款参数
 * @Author Amos
 * @Date 2023/5/17 17:08
 * @Version 1.0
 * @author Amos
 */
@Data
public class RefundParamTwoDTO {

    /**
     * 商户订单id
     */
    private String orderId;

    /**
     * 支付方式
     */
    private Integer payCode;

    /**
     * 订单来源
     */
    private Integer orderFrom;

    /**
     * 退货编号
     */
    private String returnCode;

    /**
     * 订单交易状态
     */
    private List<Integer> orderTradeStatus;

    /**
     * 第三方支付平台交易号
     */
    private String paymentOtherNumber;
}
