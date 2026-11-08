package com.houjiang.goods.vo;

import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.goods.entity.GoodsGroupBindAttributeEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 商品属性组表(MerchantGoodsAttributeGroup)表实体类
 *
 * @author Amos
 * @since 2024-10-31 14:09:21
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GoodsAttributeGroupVO extends PageHelper implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * ID
     */
    private Integer groupId;
    
    /**
     * 属性组名称
     */
    private String groupName;
    
    /**
     * 关联属性
     */
    private List<GoodsGroupBindAttributeEntity> goodsAttributeDTOList;

    /**
     * 关联属性类型
     */
    private String bindGoodsAttribute;

    /**
     * 属性组备注
     */
    private String groupNotes;

}
