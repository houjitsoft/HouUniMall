package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
 /**
 * @ClassName
 * @Description  商品属性
 * @Author Amos
 * @Date 2023-05-25 18:08:18
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_attribute")
@Data
public class GoodsAttributeEntity  implements Serializable {

    private Integer id;
    /**
     * 分类Id
     */
    private Integer catId;
    /**
     * 商品属性
     */
    private String commodityAttribute;
    /**
     * 是否必填 1是，2否
     */
    private Integer attributeStatus;


     /**
      *属性内容
      */
     @TableField(exist = false)
     private List<String> attributeContent;

     /**
      *属性内容关联id
      */
     @TableField(exist = false)
     private Integer attributeContentId;

     /**
      *属性内容
      */
     @TableField(exist = false)
     private String attriContent;

     @TableField(exist = false)
     private List<GoodsAttributeContentEntity> attrContentList;


     @TableField(exist = false)
     private static final long serialVersionUID = 1L;
}
