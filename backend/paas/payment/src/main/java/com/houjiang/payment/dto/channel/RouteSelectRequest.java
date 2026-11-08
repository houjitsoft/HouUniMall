package com.houjiang.payment.dto.channel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 路由选择请求（风控检查通过后，调用此接口选出实际要调用的渠道）
 * @author Amos
 */
@Data
public class RouteSelectRequest {

    @NotNull(message = "业务类型必填")
    private Integer bizType;

    @NotBlank(message = "支付方式必填")
    private String payMethod;

    private String terminal;
    private String merchantNo;

    @NotNull(message = "金额必填")
    private BigDecimal amount;

    /** 业务订单号（cashierNo/refundNo） */
    private String bizNo;

    /**
     * 原渠道编码（W5+ P2-1退款路由专用）
     * <p>退款场景下必填，表示原支付走的渠道 channelCode；
     * 路由引擎需保证选中的退款渠道与原渠道一致（原路退回限制）。</p>
     */
    private String originChannelCode;
}
