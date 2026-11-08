package com.houjiang.goods.dto;

import lombok.Data;

/**
 * @ClassName GdbMerchantGoodsSpecValueDTO
 * @Author Amos
 * @Date 2023/6/27 17:29
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbMerchantGoodsSpecValueDTO {

    private Integer specValueId;

    /**
     * 规格值名称
     */
    private String specValueName;

    /**
     * 所属规格id
     */
    private Integer specId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 排序
     */
    private Integer specValueDisplayorder;

    /**
     * 分类ID
     */
    private Integer catId;

    /**
     * 商品分类id
     */
    private String catIds;

    /**
     * 类型ID
     */
    private Integer typeId;
}
