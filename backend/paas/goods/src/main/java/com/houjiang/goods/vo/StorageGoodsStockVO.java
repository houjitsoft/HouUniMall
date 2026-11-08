package com.houjiang.goods.vo;


import lombok.Data;

import java.io.Serializable;

/**
 * 仓库库存流水商品表(StorageInventoryFlowGoods)表实体类
 *
 * @author Amos
 * @since 2024-09-20 16:51:54
 */
@Data
public class StorageGoodsStockVO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 入库仓库库存
     */
    private Integer putStorageStock;

    /**
     * 出库仓库库存
     */
    private Integer outStorageStock;

}
