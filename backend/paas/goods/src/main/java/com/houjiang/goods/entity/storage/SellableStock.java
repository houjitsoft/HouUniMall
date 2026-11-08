package com.houjiang.goods.entity.storage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 可售库存（原 mall 表 hou_mall_stock，由 goods 唯一写入）。
 */
@Data
@TableName("hou_mall_stock")
public class SellableStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long warehouseId;
    private Long goodsId;
    private Long skuId;
    private String goodsName;
    private String skuSpec;
    private Integer quantity;
    private Integer availableQty;
    private Integer lockedQty;
    private Integer warningQty;
    private BigDecimal costPrice;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
