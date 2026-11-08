package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据大屏购物车表
 * @TableName ${database.prefix.datav}hou_datav_user_cart
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_user_cart")
@Data
public class DcenterUserCartEntity implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer cartId;

    /**
     * 商品Id
     */
    private Integer commonId;

    /**
     * 插入购物车时间
     */
    private Date insertCartTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}