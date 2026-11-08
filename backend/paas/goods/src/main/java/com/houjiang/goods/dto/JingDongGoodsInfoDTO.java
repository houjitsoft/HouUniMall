package com.houjiang.goods.dto;

import lombok.Data;

/**
 * @ClassName JingDongGoodsInfoDTO
 * @Author Amos
 * @Date 2025/5/29 17:27
 * @Version 1.0
 * @author Amos
 */
@Data
public class JingDongGoodsInfoDTO {
    /**
     * 京东skuId
     */
    private String skuId;
    /**
     * 商品数量
     */
    private Integer skuNum;
}
