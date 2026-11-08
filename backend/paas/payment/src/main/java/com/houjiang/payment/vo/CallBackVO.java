package com.houjiang.payment.vo;

import lombok.Data;

/**
 * 回调
 * @author Amos
 */
@Data
public class CallBackVO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 订单满额
     */
    private String orderFullAmount;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 优惠次数
     */
    private Integer numberOfGifts;

}
