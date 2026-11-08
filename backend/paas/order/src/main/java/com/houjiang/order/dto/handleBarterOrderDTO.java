package com.houjiang.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @ClassName BusinessOvertimeOrderDTO
 * @Description 业务超时订单
 * @Author Amos
 * @Date 2023/5/17 17:10
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class handleBarterOrderDTO {

    /**
     * 商户订单id
     */
    private String orderId;

    /**
     * 退货编号
     */
    private String returnCode;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 商家处理结果  1-待处理  2-卖家审核通过 3-卖家审核不通过(暂废弃，状态同return_state)
     */
    private Integer returnShopHandle;

    /**
     * 发货状态
     */
    private Integer orderBarterStatus;

    /**
     * 退款状态 1未退款，2已退款
     */
    private Integer returnState;

    /**
     * 备注
     */
    private String returnShopMessage;

    /**
     * 退货日期
     */
    private String returnFinishTime;

    /**
     * 订单完成时间
     */
    private String orderFinishedTime;

    /**
     * 商家处理时间
     */
    private String returnShopTime;

    /**
     * 退款金额
     */
    private BigDecimal returnCash;

    /**
     * 商家id
     */
    private Integer shopId;
}
