package com.houjiang.commons.vo.goods;


import lombok.Data;

import java.io.Serializable;

/**
 * 仓库库存流水商品表(StorageInventoryFlowGoods)表实体类
 *
 * @author Amos
 * @since 2024-09-20 16:51:54
 */
@Data
public class StorageInventoryFlowGoodsVO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 流水商品id
     */
    private Integer inventoryFlowGoodsId;
    
    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品id
     */
    private Integer commonId;
    
    /**
     * 店铺id
     */
    private Integer shopId;
    
    /**
     * 入库数量/出库数量/调拨数量
     */
    private Integer goodsNum;
    
    /**
     * 采购数量
     */
    private Integer buyNum;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 单位
     */
    private String dw;

    /**
     * 入库仓库库存
     */
    private Integer putStorageStock;

    /**
     * 出库仓库库存
     */
    private Integer outStorageStock;

    /**
     * 入库仓库id
     */
    private Integer putStorageId;

    /**
     * 出库仓库id
     */
    private Integer outStorageId;

}
