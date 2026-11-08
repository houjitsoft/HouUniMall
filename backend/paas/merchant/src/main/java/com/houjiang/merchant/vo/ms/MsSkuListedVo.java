package com.houjiang.merchant.vo.ms;

import com.houjiang.merchant.vo.mall.MallGoodsDetailVo;
import com.houjiang.merchant.vo.mall.MallGoodsSkuDetailVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MsSkuListedVo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "秒杀id")
    private Integer msId;
    @Schema(description = "商品id")
    private Integer goodsId;

    @Schema(description = "商品sku id")
    private Integer goodsSkuId;
    @Schema(description = "秒杀价格")
    private BigDecimal price;
    @Schema(description = "总库存")
    private Integer totalStock;

    @Schema(description = "剩余库存")
    private Integer stock;

    @Schema(description = "销量")
    private Integer saleNum;

    @Schema(description = " sku信息")
    private MallGoodsSkuDetailVo skuInfo;
}
