package com.houjiang.goods.vo;


import lombok.Data;

import java.util.List;

/**
 * 商品类型
 * @author Amos
 */
@Data
public class GoodsAddTypeVO {

    /**
     * 商品规格集合
     */
    private List<AllGoodsSpecVO> goodsSpecList;

    /**
     * 推荐品牌集合
     */
    private List<GoodsBrandByCatIdVO> goodsBrandList;


}
