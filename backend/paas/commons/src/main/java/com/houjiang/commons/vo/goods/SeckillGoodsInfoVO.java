package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName DiscountDataEntity
 * @Description
 * @Author Amos
 * @Date 2022/11/2 15:33
 * @Version 1.0
 * @author Amos
 */

@Data
public class SeckillGoodsInfoVO {
    /**
     * 付款商品件数
     */
    private Integer goodsNum;
    /**
     * 总成交额
     */
    private BigDecimal dealAmount;

}
