package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName ImQueryUserOrderInfoDTO
 * @Author Amos
 * @Date 2024/11/6 14:30
 * @Version 1.0
 * @author Amos
 */
@Data
public class ImQueryUserOrderInfoDTO {
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 订单实付金额
     */
    private BigDecimal orderPaymentAmount;
    /**
     * 订单状态 订单状态 1.待付款  3.待发货 4.已发货  6.确认收货 7.取消 8.退款中 9.已退款 11.待自提
     * 12 虚拟订单已过期  16 部分发货
     */
    private Integer orderStatus;
    /**
     * 商品数量
     */
    private Integer goodsNum;
    /**
     * 商品图片
     */
    private String goodsImage;
    /**
     * 订单创建时间
     */
    private Date orderCreateTime;
}
