package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName SeckillGoodsDTO
 * @Description SeckillGoodsDTO
 * @Author Amos
 * @Date 2023/11/5 9:45
 * @Version 1.0
 * @author Amos
 */
@Data
public class SeckillGoodsDTO {
    /**
     * spuId
     */
    private Integer commonId;
    /**
     * skuId
     */
    private Integer goodsId;
    /**
     * 秒杀库存
     */
    private Integer seckillNum;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 商品原价
     */
    private BigDecimal goodsPrice;
}
