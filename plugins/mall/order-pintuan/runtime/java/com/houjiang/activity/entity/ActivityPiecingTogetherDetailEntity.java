package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 拼团详情
 * @TableName activity_piecing_together_detail
 * @author Amos
 */
@TableName(value = "hou_addon_activity_piecing_together_detail")
@Data
public class ActivityPiecingTogetherDetailEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer detailId;

    /**
     * 拼团共用ID，如开始时间等
     */
    private Integer pintuanId;

    /**
     * 参加拼团的商品ID
     */
    private Integer goodsId;

    /**
     * 原价格
     */
    private Double priceOri;

    /**
     * 团购价格
     */
    private Double price;

    /**
     * 单独购买价格
     */
    private Double priceOne;

    /**
     * 商品类别id
     */
    private Integer cid;

    /**
     * 商品ID
     */
    private Integer commonId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}