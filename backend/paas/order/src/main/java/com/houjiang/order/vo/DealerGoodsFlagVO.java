package com.houjiang.order.vo;

import lombok.Data;

/**
 * @ClassName DealerGoodsFlagVO
 * @Description
 * @Author Amos
 * @Date 2024/7/26 15:00
 * @Version 1.0
 * @author Amos
 */
@Data
public class DealerGoodsFlagVO {

    /**
     * 是否经销商品  0普通商品  1 一键代发(商家一键代发供应商商品)  2商家购买供应商商品 3 一键铺货
     */
    private Integer dealerGoodsFlag;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品名
     */
    private String goodsName;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    private Integer supplierShopId;
}
