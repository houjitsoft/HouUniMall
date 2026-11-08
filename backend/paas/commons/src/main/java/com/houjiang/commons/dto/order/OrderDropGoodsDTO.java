package com.houjiang.commons.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDropGoodsDTO {

    /**
     * 最低价
     */
    private BigDecimal minPrice;

    /**
     * 最高价
     */
    private BigDecimal maxPrice;

    /**
     * 供应商商品spuid
     */
    private Integer supplyCommonId;

    /**
     * 供应商商品skuid
     */
    private Integer supplyGoodsId;

    /**
     * 协议价
     */
    private BigDecimal agreementPrice;

}
