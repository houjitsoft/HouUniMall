package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName ManageGoodsSelfCatVO
 * @Author Amos
 * @Date 2023/6/27 14:36
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGoodsSelfCatVO {
    /**
     * 店铺商品分类id  -- json
     */
    private Integer shopGoodsCatId;

    /**
     * 店铺商品分类名称
     */
    private String shopGoodsCatName;

    /**
     * 父id
     */
    private Integer parentId;


}
