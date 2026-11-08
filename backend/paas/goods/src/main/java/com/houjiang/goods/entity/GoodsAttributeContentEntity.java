package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
 /**
 * @ClassName
 * @Description  商品属性内容
 * @Author Amos
 * @Date 2023-05-25 18:10:08
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_attribute_content")
@Data
public class GoodsAttributeContentEntity  implements Serializable {

    private Integer id;
    /**
     * 属性内容
     */
    private String attributeContent;
    /**
     * 属性id
     */
    private Integer attributeContentId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
