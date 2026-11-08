package com.houjiang.merchant.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class FinanceRefundLogVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "流水号")
    private String sn;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退款状态: [0=退款中, 1=退款成功, 2=退款失败]")
    private Integer refundStatus;

    @Schema(description = "退款状态描述")
    private String refundStatusText;

    @Schema(description = "退款信息")
    private String refundMsg;

    @Schema(description = "操作人")
    private String handler;

    @Schema(description = "创建时间")
    private String createTime;

}
