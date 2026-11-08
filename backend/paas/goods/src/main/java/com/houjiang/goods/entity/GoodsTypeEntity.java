package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品类型表-要取消各种快捷定位
 * @TableName goods_type
 * @author Amos
 */
@TableName(value ="goods_type")
@Data
public class GoodsTypeEntity implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer typeId;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 排序
     */
    private Integer typeDisplayorder;

    /**
     * 草稿：只允许存在一条记录
     */
    private Boolean typeDraft;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}