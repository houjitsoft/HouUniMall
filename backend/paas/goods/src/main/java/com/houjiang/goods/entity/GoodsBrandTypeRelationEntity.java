package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 品牌类型关联表
 * @TableName goods_brand_type_relation
 * @author Amos
 */
@TableName(value ="goods_brand_type_relation")
@Data
public class GoodsBrandTypeRelationEntity implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 品牌类型ID
     */
    private Integer brandTypeId;

    /**
     * 品牌ID
     */
    private Integer brandId;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
