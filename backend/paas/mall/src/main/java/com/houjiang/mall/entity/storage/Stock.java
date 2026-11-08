package com.houjiang.mall.entity.storage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存实体类
 * 表名: hou_mall_stock
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_stock")
public class Stock implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * SKU规格
     */
    private String skuSpec;

    /**
     * 库存数量
     */
    private Integer quantity;

    /**
     * 可用数量
     */
    private Integer availableQty;

    /**
     * 锁定数量
     */
    private Integer lockedQty;

    /**
     * 预警数量
     */
    private Integer warningQty;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-否 1-是
     */
    @TableLogic
    private Integer deleted;
}
