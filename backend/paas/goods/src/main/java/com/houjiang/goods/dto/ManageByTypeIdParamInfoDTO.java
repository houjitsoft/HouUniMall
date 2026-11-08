package com.houjiang.goods.dto;

import lombok.Data;

/**
 * @ClassName ManageByTypeIdParamInfo
 * @Description
 * @Author Amos
 * @Date 2023/4/12 12:05
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageByTypeIdParamInfoDTO {

    /**
     * 分类id
     */
    private String catIds;

    /**
     * 类型id
     */
    private String typeId;

    /**
     * 商品分类id
     */
    private Integer catId;

    /**
     * 商家id
     */
    private Integer shopId;

    /**
     * 商品销量
     */
    private Integer sales;

    /**
     * 销售数量
     */
    private Integer goodsSalenum;

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 规格类型 多规格
     */
    private String specId;
}
