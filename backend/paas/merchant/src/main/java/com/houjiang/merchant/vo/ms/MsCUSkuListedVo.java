package com.houjiang.merchant.vo.ms;

import com.houjiang.merchant.vo.mall.MallGoodsSkuDetailVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MsCUSkuListedVo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "秒杀id")
    private Integer goodsSkuId;
    @Schema(description = "秒杀价格")
    private BigDecimal price;
    @Schema(description = "总库存")
    private Integer totalStock;

}
