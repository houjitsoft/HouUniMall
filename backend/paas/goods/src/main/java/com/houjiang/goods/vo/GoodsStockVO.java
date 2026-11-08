package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class GoodsStockVO {
    /**
     * 市场价
     */
    private Integer goodsStock;
    /**
     * 库存预警值
     */
    private Integer goodsAlarm;
}
