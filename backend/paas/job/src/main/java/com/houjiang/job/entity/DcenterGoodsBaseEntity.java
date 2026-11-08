package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏商品表
 * @TableName ${database.prefix.datav}hou_datav_goods_base
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_goods_base")
@Data
public class DcenterGoodsBaseEntity implements Serializable {
    /**
     * 商品id
     */
    @TableId(type = IdType.AUTO)
    private Integer goodsId;

    /**
     * 商品公共表id
     */
    private Integer commonId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}