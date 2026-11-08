package com.houjiang.commons.dto.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退款所需参数
 * @author Amos
 */
@Data
public class RefundParamDTO {

    /**
     * 订单id
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
     * 第三方支付平台交易号
     */
    private String paymentOtherNumber;

    /**
     * 订单交易状态
     */
    private List<Integer> orderTradeStatus;

    /**
     * 退款金额
     */
    private BigDecimal returnCash;

    /**
     * 支付单号
     */
    private String paymentNumber;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺类型
     */
    private Integer shopType;
}
