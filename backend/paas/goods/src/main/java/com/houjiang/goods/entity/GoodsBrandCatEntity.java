package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
/**
 * @ClassName
 * @Description  商品品牌分类表
 * @Author Amos
 * @Date 2023-06-26 13:50:10
 * @Version 1.0
 * @author Amos
 */
@TableName("goods_brand_cat")
@Data
public class GoodsBrandCatEntity  implements Serializable {

    private Integer id;
    /**
     * 分类id
     */
    private Integer catId;
    /**
     * 品牌id
     */
    private Integer brandId;
    /**
     * 品牌名称
     */
    private String brandName;
    /**
     * Pc图片
     */
    private String brandPic;
    /**
     * H5图片
     */
    private String brandHf;
    /**
     * 分类名称
     */
    private String catName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
