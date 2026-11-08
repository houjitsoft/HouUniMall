package com.houjiang.merchant.vo.ms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MsOrderCountVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "秒杀订单")
    private Long count;

    @Schema(description = "秒杀销售量")
    private Long saleNum;

    @Schema(description = "秒杀销售额")
    private BigDecimal saleMoney;

    @Schema(description = "秒杀ID")
    private Integer msId;
}
