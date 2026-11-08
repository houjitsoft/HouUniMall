package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ModularizationdGoodsData
 * @Description ModularizationdGoodsDataDTO
 * @Author Amos
 * @Date 2023/6/8 16:32
 * @Version 1.0
 * @author Amos
 */
@Data
public class ModularizationdGoodsDataDTO {
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 商品价格
     */
    private BigDecimal commonPrice;
    /**
     * 商品图片
     */
    private String commonImage;
    /**
     * 分类id
     */
    private Integer catId;

}
