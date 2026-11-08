package com.houjiang.job.vo;

import lombok.Data;

/**
 * @ClassName AppJiPushOrderVO
 * @Description 订单签收推送
 * @Author Amos
 * @Date 2023/5/17 14:05
 * @Version 1.0
 * @author Amos
 */
@Data
public class AppJiPushOrderVO {

    /**
     * 买家id
     */
    private String buyerUserId;

    /**
     * 商户订单id
     */
    private String orderId;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 物流
     */
    private String expressPinyin;
}
