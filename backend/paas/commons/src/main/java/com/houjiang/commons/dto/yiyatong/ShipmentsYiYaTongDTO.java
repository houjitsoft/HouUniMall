package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ShipmentsGoodsDTO
 * @Description
 * @Author Amos
 * @Date 2024/12/10 18:58
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShipmentsYiYaTongDTO {

    /**
     * 快递公司编号
     * 用于标识与订单相关的快递服务提供商
     */
    private String deliveryCorpSn;

    /**
     * 快递单号
     * 用于跟踪和管理快递包裹的唯一编号
     */
    private String deliverySn;

    /**
     * 订单编号
     * 用于唯一标识一个订单，便于查询和管理
     */
    private String orderSn;

    /**
     * SKU ID 列表
     * 包含订单中所有商品的SKU ID，用于库存管理和订单处理
     */
    private List<String> skuIdList;
}
