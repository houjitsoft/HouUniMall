package com.houjiang.commons.dto.goods;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品库存表
 * @TableName goods_stock
 * @author Amos
 */
@Data
public class GoodsStockDTO implements Serializable {
    /**
     * 
     */
    private Integer id;

    /**
     * goodsId
     */
    private Integer goodsId;

    /**
     * commonId
     */
    private Integer commonId;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 库存预警值
     */
    private Integer goodsAlarm;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 修改时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}