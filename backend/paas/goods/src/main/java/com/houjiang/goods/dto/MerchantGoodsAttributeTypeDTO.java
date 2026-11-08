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
 * 商品属性类型表(MerchantGoodsAttributeType)表实体类
 *
 * @author Amos
 * @since 2024-10-31 14:07:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MerchantGoodsAttributeTypeDTO extends PageHelper implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * ID
     */
    @NotNull(message = "ID不能为空", groups = {Delete.class})
    private Integer attributeId;
    
    /**
     * 属性名称
     */
    @NotEmpty(message = "属性名称不能为空", groups = {Update.class, Select.class})
    private String attributeName;
    
    /**
     * 属性值列
     */
    private String attributeItem;

    /**
     * 商品属性值
     */
    @NotEmpty(message = "商品属性值不能为空", groups = {Update.class})
    private List<MerchantGoodsAttributeValueDTO> goodsAttributeValueDTOList;

    /**
     * 属性名称
     */
    private String attributeValueName;

    /**
     * 属性备注
     */
    private String attributeNotes;

}
