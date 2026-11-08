package com.houjiang.commons.vo.goods;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 商品属性组表(MerchantGoodsAttributeGroup)表实体类
 *
 * @author Amos
 * @since 2024-10-31 14:09:21
 */
@Data
public class GoodsAttrVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 属性组名称
     */
    private String groupName;
    
    /**
     * 关联属性
     */
    private List<GoodsCategoryAttributesVO> goodsAttributeList;

    /**
     * 排序
     */
    private Integer displayOrder;

}
