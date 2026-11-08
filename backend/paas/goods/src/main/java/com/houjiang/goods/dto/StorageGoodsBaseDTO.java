package com.houjiang.goods.dto;


import com.houjiang.commons.group.Select;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 仓库中商品信息表(StorageGoodsBase)表实体类
 *
 * @author Amos
 * @since 2024-09-19 14:06:57
 */
@Data
public class StorageGoodsBaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 商品skuid
     */
    @NotNull(message = "商品不能为空", groups = {Select.class})
    private Integer goodsId;
    
    /**
     * 店铺id
     */
    private Integer shopId;
    
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
     * 商品id集合
     */
    @NotEmpty(message = "商品集合不能为空", groups = {Select.selectGoods.class})
    private List<Integer> goodsIdList;

    /**
     * token
     */
    private String token;

}
