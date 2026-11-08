package com.houjiang.order.dto;

import lombok.Data;

import java.util.List;

/**
 * 应用退货订单
 * @author Amos
 */
@Data
public class ApplyReturnOrderDTO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 退款理由id
     */
    private String returnReasonId;

    /**
     * 退款理由
     */
    private String returnReason;

    /**
     * 退货备注
     */
    private String returnMessage;

    /**
     * 退款金额
     */
    private Double returnCash;

    /**
     * 退款凭证
     */
    private List<String> returnVoucher;

    /**
     * token
     */
    private String token;

    /**
     * 退款商品返回集合
     */
    private List<ApplyReturnGoodDTO> applyReturnGoods;

    /**
     * 是否供应商
     */
    private Integer isSupplier;

    @Override
    public String toString() {
        return "ApplyReturnOrderDTO [orderId=" + orderId + ", returnReasonId=" + returnReasonId + ", returnReason="
                + returnReason + ", returnMessage=" + returnMessage + ", token=" + token + ", applyReturnGoods="
                + applyReturnGoods + "]";
    }

}
