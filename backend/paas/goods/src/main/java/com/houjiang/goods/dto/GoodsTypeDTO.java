package com.houjiang.goods.dto;

import com.houjiang.goods.entity.GoodsTypeBrandEntity;
import com.houjiang.goods.entity.GoodsTypeSpecEntity;
import lombok.Data;

import java.util.List;

/**
 * 商品类型
 * @author Amos
 */
@Data
public class GoodsTypeDTO {

    /**
     * token
     */
    private String token;

    /**
     * 类型id
     */
    private Integer typeId;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 排序
     */
    private Integer typeDisplayorder;

    /**
     * 品牌列表
     */
    private List<GoodsTypeBrandEntity> typeBrandList;


    /**
     * 规格列表
     */
    private List<GoodsTypeSpecEntity> typeSpecList;


}
