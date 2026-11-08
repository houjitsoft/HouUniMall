package com.houjiang.job.vo;

import lombok.Data;

import java.util.Date;

/**
 * 数据大屏购物车表
 * @TableName ${database.prefix.datav}hou_datav_user_cart
 * @author Amos
 */

@Data
public class DcenterUserCartVO {
    /**
     * id
     */

    private Integer cartId;

    /**
     * 商品Id
     */
    private Integer commonId;

    /**
     * 插入购物车时间
     */
    private Date insertCartTime;


}