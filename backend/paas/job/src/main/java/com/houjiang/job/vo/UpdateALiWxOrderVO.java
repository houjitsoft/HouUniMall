package com.houjiang.job.vo;

import lombok.Data;

/**
 * @ClassName UpdateALiWxOrderVO
 * @Description TODO
 * @Author Amos
 * @Date 2024/4/16 11:52
 * @Version 1.0
 * @author Amos
 */
@Data
public class UpdateALiWxOrderVO {
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 支付单号
     */
    private String paymentNumber;
    /**
     * plus 会员支付订单
     */
    private String plusOrderid;
}
