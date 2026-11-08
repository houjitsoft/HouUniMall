package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName GateUpShopGoodsStockDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/2 10:32
 * @Version 1.0
 * @author Amos
 */
@Data
public class GateUpShopGoodsStockDTO {

    /**
     * token
     */
    private String token;

    /**
     * 商品规格id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 门店库存总和最大值
     */
    private Integer goodsShopGateStock;

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
     * 门店商品 规格 库存
     */
    private Integer shopGateGoodsStock;

}
