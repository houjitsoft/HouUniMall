package com.houjiang.job.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏商品表
 * @TableName ${database.prefix.datav}hou_datav_goods_base
 * @author Amos
 */
@Data
public class DcenterGoodsBaseVO implements Serializable {
    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品公共表id
     */
    private Integer commonId;


}