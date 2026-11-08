package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName GateGoodsBaseVO
 * @Description
 * @Author Amos
 * @Date 2023/8/2 10:10
 * @Version 1.0
 * @author Amos
 */
@Data
public class GateGoodsBaseVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 门店id
     */
    private Integer shopGateId;

    /**
     * 商品规格id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 门店商品 规格 库存
     */
    private Integer shopGateGoodsStock;
}
