package com.houjiang.goods.dto;


import com.baomidou.mybatisplus.annotation.TableField;
import com.houjiang.commons.group.Delete;
import com.houjiang.commons.group.Select;
import com.houjiang.commons.group.Update;
import com.houjiang.commons.util.querypage.PageHelper;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class GoodsAttributeGroupDTO extends PageHelper implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * ID
     */
    @NotNull(message = "ID不能为空", groups = {Delete.class})
    private Integer groupId;
    
    /**
     * 属性组名称
     */
    @NotEmpty(message = "属性名称不能为空", groups = {Update.class})
    private String groupName;
    
    /**
     * 关联属性
     */
    @NotEmpty(message = "关联属性不能为空", groups = {Update.class})
    private List<GoodsAttributeDTO> goodsAttributeDTOList;

    /**
     * 商品分类id
     */
    @NotNull(message = "商品分类id不能为空", groups = {Select.class})
    private Integer catId;

    /**
     * 属性组备注
     */
    private String groupNotes;

}
