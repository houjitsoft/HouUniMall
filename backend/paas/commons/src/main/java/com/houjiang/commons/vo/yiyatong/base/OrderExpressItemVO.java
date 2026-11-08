package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

/**
 * @ClassName OrderExpressItemVO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 9:36
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderExpressItemVO {

    /**
     * 待发送商品的数量，用于记录即将发出的商品总数
     */
    private String sendGoodsQuantity;

    /**
     * 商品ID，唯一标识一个商品
     */
    private String goodsId;

    /**
     * 商品规格的JSON字符串，用于详细描述商品的规格信息
     */
    private String goodsSpecJson;

    /**
     * 商品数量，表示当前商品的库存或可用数量
     */
    private String goodsQuantity;

    /**
     * 商品规格ID，用于标识商品的具体规格，在存在多种规格时使用
     */
    private String goodsSpecId;

    /**
     * 商品名称，记录商品的名称信息
     */
    private String goodsName;

    /**
     * 商品图片URL，存储商品图片的网络地址
     */
    private String goodsPhotoUrl;


}
