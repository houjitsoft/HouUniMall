package com.houjiang.order.vo;

import lombok.Data;

/**
 * @ClassName ShippingGoodsMessageVO
 * @Description
 * @Author Amos
 * @Date 2023/8/14 9:38
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShippingGoodsMessageVO {

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品采购数量
     */
    private String orderGoodsNum;

    /**
     * 发货数量
     */
    private Integer shipmentNum;

    /**
     * 总发货数量
     */
    private Integer shipmentQuantity;
}
