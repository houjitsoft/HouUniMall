package com.houjiang.goods.dto;

import lombok.Data;

/**
 * 商品
 * @author Amos
 */
@Data
public class GoodsEntityDTO {

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;
}
