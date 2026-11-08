package com.houjiang.goods.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Mall 商品收藏实体
 * 表：hou_mall_goods_collect
 * @author Amos
 */
@Data
@TableName("hou_mall_goods_collect")
public class MallGoodsCollect implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer createTime;
    private Integer updateTime;
    private Integer deleteTime;

    private Integer userId;
    private Integer shopId;
    private Integer goodsId;
    private Integer status;
}
