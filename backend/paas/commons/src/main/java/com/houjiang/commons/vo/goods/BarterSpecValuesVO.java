package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class BarterSpecValuesVO {

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品规格值
     */
    private List<GoodBaseSpecValuesVO> specValues;

    /**
     * 商品价格
     */
    private Integer goodsStock;

}
