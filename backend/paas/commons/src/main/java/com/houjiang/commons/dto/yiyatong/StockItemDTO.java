package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

/**
 * @ClassName StockItemDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 17:18
 * @Version 1.0
 * @author Amos
 */
@Data
public class StockItemDTO {

    /**
     * 商品编码
     */
    private String skuId;

    /**
     * sku数量
     */
    private String skuNum;
}
