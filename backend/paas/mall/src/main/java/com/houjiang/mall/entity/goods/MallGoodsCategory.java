package com.houjiang.mall.entity.goods;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品分类（映射 hou_mall_goods_category 表）
 *
 * 对应 BBC 原始系统的 goods_category 表结构，
 * 字段：id, create_time, update_time, delete_time, name, pid, level, image, sort, is_show
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_goods_category")
public class MallGoodsCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 创建时间（Unix 时间戳） */
    @TableField("create_time")
    private Long createTime;

    /** 更新时间（Unix 时间戳） */
    @TableField("update_time")
    private Long updateTime;

    /** 删除时间（Unix 时间戳，0=未删除） */
    @TableField("delete_time")
    private Long deleteTime;

    /** 分类名称 */
    private String name;

    /** 父级分类 ID（0=顶级） */
    private Integer pid;

    /** 层级（1=一级，2=二级...） */
    private Integer level;

    /** 分类图片路径 */
    private String image;

    /** 排序值（越小越靠前） */
    private Integer sort;

    /** 是否显示：1=显示 0=隐藏 */
    @TableField("is_show")
    private Integer isShow;
}
