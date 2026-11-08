package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * (MerchantGoodsCommonAttributeAssociation)
 *
 * @since 2023-05-15 16:25:25
 * @author Amos
 */
@TableName("goods_common_attribute_association")
@Data
public class GoodsCommonAttributeAssociationEntity implements Serializable {

    private Integer id;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 属性id
     */
    private Integer attributeId;
    /**
     * 属性内容id
     */
    private Integer attributeContentId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

