package com.houjiang.goods.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Mall 商品分类树实体
 * 表：hou_mall_goods_category
 * @author Amos
 */
@Data
@TableName("hou_mall_goods_category")
public class MallGoodsCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer createTime;
    private Integer updateTime;
    private Integer deleteTime;

    private String name;
    private Integer pid;
    private Integer level;
    private String image;
    private Integer sort;
    private Integer isShow;
}
