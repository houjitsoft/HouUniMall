package com.houjiang.order.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class OdbPaymentVocherVO {

    private Integer id;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 支付凭证 图片
     */
    private String orderPaymentVoucherImgs;

    /**
     * 审核状态
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 审核时间
     */
    private String reviewTime;

}
