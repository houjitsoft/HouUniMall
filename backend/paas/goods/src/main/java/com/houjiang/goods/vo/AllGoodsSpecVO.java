package com.houjiang.goods.vo;

import lombok.Data;

/**
 * 所有商品规格
 * @author Amos
 */
@Data
public class AllGoodsSpecVO {

    /**
     * 规格id
     */
    private Integer specId;

    /**
     * 规格名称
     */
    private String specName;

    /**
     * 规格类型id
     */
    private Integer typeSpecId;


}
