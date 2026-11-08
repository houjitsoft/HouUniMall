package com.houjiang.merchant.vo.pt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class PtCUSkuListedVo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "秒杀id")
    private Integer goodsSkuId;
    @Schema(description = "秒杀价格")
    private BigDecimal price;

}
