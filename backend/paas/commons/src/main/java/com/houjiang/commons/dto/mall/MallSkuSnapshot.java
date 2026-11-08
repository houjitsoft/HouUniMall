package com.houjiang.commons.dto.mall;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallSkuSnapshot implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long skuId;
    private Long goodsId;
    private Long shopId;
    private String goodsName;
    private String goodsImage;
    private String skuSpec;
    private BigDecimal salePrice;
    private Integer saleableStock;
    private boolean saleable;
    private String invalidReason;
}
