package com.houjiang.order.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class OrderRemindDeliveryDTO {

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 配送时间
     */
    private String deliveryTime;

    /**
     * 令牌
     */
    private String token;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 页数，用于分页查询
     */
    private Integer page;

    /**
     * 每页显示的数量，用于分页查询
     */
    private Integer limit;

    /**
     * 总数量，通常用于统计或分页查询时使用
     */
    private Integer totalCount;

}
