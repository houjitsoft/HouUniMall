package com.houjiang.goods.vo;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 仓库库存流水商品表(StorageInventoryFlowGoods)表实体类
 *
 * @author Amos
 * @since 2024-09-20 16:51:54
 */
@Data
public class InventoryFlowVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 流水商品id
     */
    private String inventoryFlowId;
    
    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;
    
    /**
     * 出入库数量
     */
    private String goodsNum;
    
    /**
     * 采购数量
     */
    private Integer typeDetails;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 在仓总库存
     */
    private Integer totalStorageStock;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 入库仓库id
     */
    private Integer putStorageId;

    /**
     * 出库仓库id
     */
    private Integer outStorageId;

    /**
     * 入库仓库名称
     */
    private String putStorageName;

    /**
     * 出库仓库名称
     */
    private String outStorageName;
}
