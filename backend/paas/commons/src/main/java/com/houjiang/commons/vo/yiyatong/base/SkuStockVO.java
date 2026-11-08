package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

/**
 * @ClassName SkuStockVO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 17:24
 * @Version 1.0
 * @author Amos
 */
@Data
public class SkuStockVO {

    /**
     * 消息变量，用于存储相关操作的提示信息
     */
    private String msg;

    /**
     * 是否可销售,1,可销售，0不可销售
     */
    private String sailState;

    /**
     * 是否有货,1有货，0无货
     */
    private String stockState;

    /**
     * 商品SKU ID变量，用于唯一标识商品的款式
     */
    private String skuId;
}
