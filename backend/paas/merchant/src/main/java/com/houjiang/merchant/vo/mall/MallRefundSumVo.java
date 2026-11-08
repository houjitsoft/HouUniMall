package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallRefundSumVo implements Serializable {
    @Schema(description = "累计退款金额(元)")
    private BigDecimal all;
    @Schema(description = "退款失败金额(元)")
    private BigDecimal failed;
    @Schema(description = "退款成功金额(元)")
    private BigDecimal success;
    @Schema(description = "退款中金额(元)")
    private BigDecimal wait;
}
