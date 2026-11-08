package com.houjiang.goods.vo;


import lombok.Data;

import java.io.Serializable;

/**
 * 仓库中商品信息表(StorageGoodsBase)表实体类
 *
 * @author Amos
 * @since 2024-09-19 14:06:57
 */
@Data
public class StorageGoodsBaseVO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 商品skuid
     */
    private Integer goodsId;
    
    /**
     * 商品spuid
     */
    private Integer commonId;
    
    /**
     * 仓库商品库存
     */
    private Integer storageStock;
    
    /**
     * 仓库id
     */
    private Integer storageId;

    /**
     * 仓库名称
     */
    private String storageName;

}
