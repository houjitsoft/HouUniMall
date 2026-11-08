package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 品牌类型表
 * @TableName goods_brand_type
 * @author Amos
 */
@TableName(value ="goods_brand_type")
@Data
public class GoodsBrandTypeEntity implements Serializable {
    /**
     * 品牌类型ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 关联的品牌ID列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Integer> brandIds;

    /**
     * 品牌数量（非数据库字段）
     */
    @TableField(exist = false)
    private Integer brandCount;

    private static final long serialVersionUID = 1L;
}
