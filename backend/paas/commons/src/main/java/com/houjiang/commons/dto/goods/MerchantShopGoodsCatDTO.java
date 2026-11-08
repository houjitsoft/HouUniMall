package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName MerchantShopGoodsCatDTO
 * @Author Amos
 * @Date 2023/6/26 15:15
 * @Version 1.0
 * @author Amos
 */
@Data
public class MerchantShopGoodsCatDTO {
    /**
     * id
     */
    private Integer id;

    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 店铺商品分类id  -- json
     */
    private Integer shopGoodsCatId;

    /**
     * 店铺商品分类名称
     */
    private String shopGoodsCatName;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 父类id
     */
    private Integer parentId;
}
