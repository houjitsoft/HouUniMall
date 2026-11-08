package com.houjiang.userms.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class VkGiftOrderDTO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userMobile;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 是否为礼包订单 1是 0否
     */
    private Integer isGiftOrder;


}
