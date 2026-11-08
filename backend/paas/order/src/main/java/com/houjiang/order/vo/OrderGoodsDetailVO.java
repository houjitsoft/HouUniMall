package com.houjiang.order.vo;

import lombok.Data;

/**
 * @ClassName OrderGoodsDetailVO
 * @Description 订单商品详情
 * @Author Amos
 * @Date 2023/7/5 13:21
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderGoodsDetailVO {
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品commonId
     */
    private Integer commonId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品图片
     */
    private String goodsImage;
    /**
     * 商品数量
     */
    private Integer orderGoodsNum;
}
