package com.houjiang.goods.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 仓库中商品信息表(StorageGoodsBase)表实体类
 *
 * @author Amos
 * @since 2024-09-19 14:06:57
 */
@Data
public class StorageGoodsVO implements Serializable {

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

    /**
     * 仓库所在地-省
     */
    private Integer storageProvince;

    /**
     * 仓库所在地-市
     */
    private Integer storageCity;

    /**
     * 仓库所在地-区
     */
    private Integer storageArea;

    /**
     * 仓库所在地-省
     */
    private String storageProvinceName;

    /**
     * 仓库所在地-市
     */
    private String storageCityName;

    /**
     * 仓库所在地-区
     */
    private String storageAreaName;

    /**
     * 仓库所在地-详细地址
     */
    private String storageAddress;

    /**
     * 仓库联系人
     */
    private String storageContacts;

    /**
     * 仓库联系电话
     */
    private String storageMobile;

    /**
     * 仓库商品列表
     */
    private List<StorageGoodsBaseVO> storageGoodsList;

}
