package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName GoodsInfoDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/17 14:38
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodsInfoDTO {

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品主图
     */
    private String commonImage;

    /**
     * 商品价格
     */
    private Double commonPrice;

    /**
     * 商品spu名称
     */
    private String commonName;
}
