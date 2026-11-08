package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * 商品规格值
 * @author Amos
 */
@Data
public class ManageGoodsSpecValueVO {

    /**
     * 规格名称值id
     */
    private Integer specValueId;

    /**
     * 规格名称值
     */
    private String specValueName;

    /**
     * 分类id，前台传入
     */
    private String catIds;

}
