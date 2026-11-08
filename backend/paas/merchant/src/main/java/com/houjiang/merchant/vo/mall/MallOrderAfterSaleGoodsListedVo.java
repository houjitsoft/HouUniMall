package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallOrderAfterSaleGoodsListedVo implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "id")
    private Integer afterSaleId;
    @Schema(description = "商品名称")
    private String goodsName;
    @Schema(description = "商品名称")
    private Integer afterSale;

}
