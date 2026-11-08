package com.houjiang.goods.vo;


import lombok.Data;

/**
 * @author Amos
 */
@Data
public class GoodCommonSpecValueJsonBeanVO {
    /** 商品id */
    private Integer commonId;
    /** 规格id */
    private Integer goodsId;

    public GoodCommonSpecValueJsonBeanVO(Integer commonId, Integer goodsId) {
        this.commonId = commonId;
        this.goodsId = goodsId;
    }
}
