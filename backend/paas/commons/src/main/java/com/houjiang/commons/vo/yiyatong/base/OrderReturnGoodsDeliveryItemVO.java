package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

import java.util.List;

/**
 * @ClassName OrderReturnGoodsDeliveryItemVO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 10:48
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderReturnGoodsDeliveryItemVO {


    /**
     * 商品ID，用于唯一标识一个商品
     */
    private String goodsId;

    /**
     * 商品数量，表示该商品的库存或购买数量
     */
    private Integer goodsQuantity;

    /**
     * 商品规格ID，用于标识商品的具体规格，如颜色、尺寸等
     */
    private String goodsSpecId;

    /**
     * 商品名称，描述商品的基本信息
     */
    private String goodsName;


    /**
     * 退货物流信息明细
     */
    private List<DeliveryUpdateVO> deliveryList;

    /**
     * 换货物流信息明细
     */
    private List<DeliveryUpdateVO> exchangeDeliveryList;
}
