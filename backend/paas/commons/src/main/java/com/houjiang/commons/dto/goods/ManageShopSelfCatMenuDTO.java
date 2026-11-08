package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.util.List;

/**
 * 店铺自定义商品分类表
 *
 * @author Amos
 */
@Data
public class ManageShopSelfCatMenuDTO {

    /**
     * 物品类目id
     */
    private Integer shopGoodsCatId;

    /**
     * 物品种类
     */
    private String shopGoodsCatName;

    /**
     * 类目父id
     */
    private Integer parentId;

    /**
     * 类目状态
     */
    private Integer shopGoodsCatStatus;

    /**
     * 排序
     */
    private Integer shopGoodsCatDisplayorder;

    /**
     * 商品子集
     */
    private List<ManageShopSelfCatMenuDTO> children;

    /**
     * 是否称重分类 0:否 1是
     */
    private Integer weight;

}
