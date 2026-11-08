package com.houjiang.goods.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Mall 商品-分类关联实体
 * 表：hou_mall_goods_category_index
 * @author Amos
 */
@Data
@TableName("hou_mall_goods_category_index")
public class MallGoodsCategoryIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer createTime;
    private Integer updateTime;
    private Integer deleteTime;

    private Integer goodsId;
    private Integer categoryId;
}
